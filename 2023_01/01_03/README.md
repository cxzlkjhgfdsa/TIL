# 2023_01_03

</br>

### <b> 주문 도메인 개발</b>

</br>
<b>주문 엔티티 코드</b>

-   [주문 엔티티](./code/Order.java)
    -   createOrder() : 주문 엔티티를 생성할 때 사용한다. -> setter 를 지우고 메소드를 통해 생성하는 것이 Best
    -   cancel() : 주문 취소시 사용한다. 주문 상태를 취소로 변경, 주문상품에 주문 취소 알림, 이미 배송완료 상품일 시 예외 발생
    -   getTotalPrice() : 주문 시 사용한 전체 주문 가격 조회 (실무에서는 주문에 전체 주문 가격 필드를 두고 역 정규화 한다)

</br>
<b>주문상품 엔티티 코드</b>

-   [주문 상품 엔티티](./code/OrderItem.java)
    -   createOrderItem() : 주문 상품 엔티티 생성
    -   cancel() : 취소한 주문 수량만큼 재고 증가 (addStock 호출)

</br>
<b>주문 리포지토리 코드</b>

-   [주문 리포지토리](./code/OrderRepository.java)

</br>
<b>주문 서비스 코드</b>

-   [주문 서비스](./code/OrderService.java)
    -   order() : 실제 주문 엔티티 생성 후 저장
    -   cancelOrder() : 주문 식별자로 엔티티 조회 후 주문 취소 요청
    -   findOrders() : OrderSearch 라는 검색 조건을 가진 객체로 주문 엔티티 검색

<br>
<b>참고</b>

-   주문 서비스의 주문과 주문 취소 메소드를 보면 비즈니스 로직 대부분이 엔티티에 존재
-   서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다
-   엔티티가 비즈니스 로직을 가지고 객체지향 특성을 적극 활용하는 것을 도메인 모델 패턴이라고 한다 (반대는 트랜잭션 스크립트 패턴)

</br>
<b>주문 테스트</b>

-   [주문 테스트](./code/OrderServiceTest.java)
    -   주문성공, 주문취소, 재고수량 초과 테스트

</br>
<b>주문 검색 기능 개발</b>(QueryDSL을 사용해야 하는 이유)

JPA 에서는 <b>동적 쿼리</b>를 어떻게 해결해야 하는가?

-   [검색 클래스](./code/OrderSearch.java)

1. <b>JPQL로 처리</b>

```
public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }
```

-   JPQL 쿼리를 문자로 생성시 매우 번거롭고, 실수로 인한 버그가 충분히 발생 가능하다

2. <b>JPA Criteria로 처리</b>

```
public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" +
                            orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }
```

-   JPA 표준 스펙이지만 실무에서 사용하기 너무 복잡하다 -> 다른 개발자가 봤을 때 못알아볼 가능성 높음, 유지보수성 바닥침
-   QueryDSL을 사용해야 하는 이유

3. <b>QueryDSL 사용</b>

```
public List<Order> findAll(OrderSearch ordersearch){

    QOrder order = QOrder.order;
    QMember member = QMember.member

    return query
        .select(order)
        .from(order)
        .join(order.member, member)
        .where(statusEq(orderSearch.getOrderStatus()),
            nameLike(orderSearch.getMemberName()))
        .Limit(1000)
        .fetch();
}

private BooleanExpression statusEq(OrderStatus statusCond){
    if(statusCond == null){
        return null;
    }
    return order.status.eq(statusCond);
}

private BooleanExpression nameLike(String nameCond){
    if(!StringUtils.hasText(nameCond)){
        return null;
    }
}

```

-   코드가 알아보기 굉장히 간편하다 (유지보수성 매우좋음)

</br>
<b>출처 : 인프런 김영한님 강의 참고</b>
