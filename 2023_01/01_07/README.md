# 2023_01_07

</br>

## API 개발 고급

-   [샘플 데이터 입력](./code/initDb.java)

</br>

### <b>지연 로딩과 조회 성능 최적화</b>

-   [주문조회 전체코드]()
    </br>

<b>간단한 주문 조회 V1</b>

```
@GetMapping("api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all){
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }
```

-   엔티티를 직접 노출하는 것은 좋지 않다 -> 고쳐야함
-   order->member, order_address 는 지연로딩이다. 따라서 실제 엔티티 대신 프록시 존재
-   jackson 라이브러리는 프록시 객체를 어떻게 json으로 생성하는지 모름 -> 예외 발생

<b>주의</b>

-   엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭 한곳을 @JsonIgnore 처리해야함
-   그렇지 않으면 양쪽을 서로 호출하면서 무한 루프가 걸림

<b>주문 조회 V2 : Entity -> Dto</b>

```
 @GetMapping("api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }
```

-   엔티티를 DTO로 변환하는 일반적인 방법
-   쿼리가 총 1 + N + N 번 실행된다
    -   order 조회 1번 (order 조회 결과 수가 N)
    -   order -> member 지연 로딩 조회 N번
    -   order -> delivery 지연 로딩 조회 N번

</br>
<b>주문 조회 V3 : DTO + 패치조인 </b>

```
 @GetMapping("api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }
// OrderRepository- 추가코드
 public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                " select o from Order o " +
                        " join fetch o.member" +
                        " join fetch  o.delivery ", Order.class
        ).getResultList();
    }

```

-   fetch 을 사용해 쿼리 1번에 조회
-   페치 조인으로 order -> member, order -> delivery는 이미 조회 된 상태이므로 지연로딩 X

</br>
<b>주문 조회 V4 : JPA에서 Dto 직접 조회</b>

```
    @GetMapping("api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

// OrderSimpleRepository
public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.simpleQuery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"+
                        " from Order o"+
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class
        ).getResultList();
    }

```

-   일반적으로 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회
-   new 명령어를 사용해서 JPQL의 결과를 DTO로 즉시 반환
-   SELECT 절에서 원하는 데이터를 직접 선택하므로 네트웍 용량 최적화 (그렇게 많이 차이 안남)
-   리포지토리 재사용성 떨어짐

</br>
<b>쿼리 방식 선택 순서</b>

1. 우선 엔티티를 DTO로 변환하는 방법을 선택
2. 필요하면 패치조인으로 성능 최적화 -> 여기서 대부분 해결됨
3. 그래도 안되면 DTO로 직접 조회하는 방법 선택
4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용
