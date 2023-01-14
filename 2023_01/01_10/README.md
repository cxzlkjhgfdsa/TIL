# 2023_01_10

</br>

## API 개발 고급

</br>

### 컬렉션 조회 최적화

-   [전체코드](./code/OrderApiController.java)

</br>
<b>주문 조회 V1 : 엔티티 직접 노출</b>

```
@GetMapping("api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for(Order order : all){
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o->o.getItem().getName());
        }
        return all;
    }
```

-   엔티티를 직접 노출하므로 좋지 못함

</br>
<b>주문 조회 V2: 엔티티를 DTO로 반환</b>

```
  @GetMapping("api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return  collect;
    }
```

-   지연로딩으로 인한 너무 많은 SQL 실행
-   SQL 실행
    -   order 1번
    -   member, address N번(order 조회 수 만큼)
    -   orderItem N번(order 조회 수 만큼)
    -   Item N번 (orderItem 조회 수 만큼)

</br>
<b>주문 조회 V3: 패치조인 </b>

```
 @GetMapping("api/v3/orders")
    public List<OrderDto> ordersV3(){
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }

public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o"+
                        " join fetch o.member m"+
                        " join fetch o.delivery d"+
                        " join fetch o.orderItems oi"+
                        " join fetch oi.item i", Order.class
        ).getResultList();
    }
```

-   패치 조인으로 SQL이 1번만 실행됨
-   distinct를 사용한 이유는 one to many 조인이 있으므로 데이터베이스 row가 증가하기 때문 (중복제거 -> SQL Distinct가 못하는 기능 함)
-   단점 : 페이징 불가능

</br>
<b>주문 조회 V3.1 : 페이징과 한계돌파</b>

-   컬렉션을 패치 조회하면 페이징 불가능
    -   컬렉션을 패치조인하면 일대다 조인이 발생하므로 데이터가 예측할 수 없이 증가한다
    -   페이징은 일대다에서 일(1)을 기준으로 페이징 하는 것이 목적인데 N 기준으로 생성됨
    -

<b>한계 돌파</b>

-   먼저 X to One 관계를 모두 페치조인 한다
-   컬렉션은 지연 로딩으로 조회한다
-   지연 로딩 성능 최적화를 위해 @BatchSize를 적용한다
    -   글로벌 : hibernate.default_batch_fetch_size
    -   개별 : @BatchSize
    -   컬렉션, 프록시 객체 한꺼번에 설정한 사이즈만큼 IN쿼리로 조회

```
 @GetMapping("api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit)
    {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }

public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                " select o from Order o " +
                        " join fetch o.member" +
                        " join fetch  o.delivery ", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

jpa:
  hibername:
    default_batch_fetch_size: 100
```

-   쿼리 호출 수가 !+N -> 1+1로 최적화 된다
-   조인보다 DB 데이터 전송량이 최적화 된다(OrderItem 만큼 중복해서 조회)
-   패치 조인 방식과 비교해서 쿼리 호출수가 약간 증가하지만, DB데이터 전송량이 감소한다
-   컬렉션 패치 조인은 페이징이 불가능하지만 이 방법은 페이징 가능
