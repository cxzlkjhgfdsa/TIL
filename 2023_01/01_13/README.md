# 2023_01_13

</br>

## API 개발 고급

</br>

### 컬렉션 조회 최적화

-   [전체코드](./code/OrderApiController.java)

</br>
<b>V4 : JPA 에서 DTO로 직접 조회</b>

```
 @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }

```

-   Query 루트 1번, 컬렉션 N번 실행
-   ToOne(N:1, 1:1) 관계들을 먼저 조회하고 ToMany(1:N)관계는 각각 별도로 처리한다

</br>
<b>V5: JPA 에서 DTO 직접 조회</b>

```
@GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5(){
        return orderQueryRepository.findOrderByDto_optimization();
    }


    public List<OrderQueryDto> findOrderByDto_optimization() {
        List<OrderQueryDto> result = findOrders();

        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());

        List<OrderItemQueryDto> orderItems = findOrderItems(orderIds);

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }


    private List<OrderItemQueryDto> findOrderItems(List<Long> orderIds) {

        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i"+
                        " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
    }
```

-   Query -> 루트 1번, 컬렉션 1번
-   ToOne 관계들을 먼저 조회하고, 여기서 ㅇ더은 식별자 orederId로 ToMany 관계인 OrderItem을 한꺼번에 조회
-   MAP을 사용해서 매칭성능 향상

</br>
</b>V6 : 플랫 데이터 최적화

```

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6(){

        List<OrderFlatDto> flats = orderQueryRepository.findOrderByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress()))
                .collect(toList());
    }

        public List<OrderFlatDto> findOrderByDto_flat() {
        return em.createQuery(
                "select new "+
                        " jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o"+
                        " join o.member m"+
                        " join o.delivery d" +
                        " join o.orderItems oi"+
                        " join oi.item i", OrderFlatDto.class
        ).getResultList();
    }
```

-   Query : 1번
-   단점
    -   쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에따라 V5보다 느릴 수 있음
    -   애플리케이션에서 추가작업이 많다
    -   페이징 불가능

</br>

### APU 개발 고급 정리

<b>권장순서</b>

1. 엔티티 조회 방식으로 접근
    - 패치조인으로 쿼리 수를 최적화
    - 컬렉션 죄적화
        - 페이징 필요 : BatchSize 최적화
        - 페치조인 사용
2. 엔티티 조회 해결 안될 시 DTO 사용
3. DTO 조회 방식으로도 안되면 NativeSQL or JdbcTemplate

### OISV 성능 최적화

-   Open Session in View

<b>OSIV ON</b>

![image](https://user-images.githubusercontent.com/96561194/212458217-f414f5a9-8ea5-4589-ba1b-972a225e1b60.png)

-   지연로딩은 영속성 컨텍스트가 살아있어야 가능함
-   지금까지 컨트롤러에서 지연로딩이 가능했던 이유는 OSIV가 ON되어있었기 때문
-   너무 오랜시간동안 데이터베이스 커넥션 리소스를 사용하는 문제가 있음

</br>
<b>OSIV OFF</b>

![image](https://user-images.githubusercontent.com/96561194/212458261-6a41bb9f-aaa0-476a-87e4-cfed5ff0e892.png)

-   OSIV를 끄면 트랜잭션을 종료할 때 영속성 컨텍스트를 닫고, 데이터 베이스 커넥션도 반환한다, 따라서 커넥션 리소스 낭비X
-   지연로딩을 모두 트랜잭션 안에서 처리해야함

<b>커멘드와 쿼리 분리</b>

-   실무에서 OSIV를 끈 상태로 복잡성을 관리하는 좋은방법은 Command 와 Query를 분리하는 것
-   OrderService
    -   OrderService : 핵심 비즈니스 로직
    -   OrderQueryService : 화면이나 API에 맞춘 서비스 (주로 읽기 전용 트랜잭션)
