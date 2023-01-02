# 2022_12_28

</br>

## 도메인 분석 설계 예시, 엔티티 설계 주의점

</br>

### <b>도메인 분석 설계</b>

<b>요구사항 분석</b>

-   회원 기능
    -   회원 등록
    -   회원 조회
-   상품 기능
    -   상품 등록
    -   상품 수정
    -   상품 조회
-   주문 기능
    -   상품 주문
    -   주문 내역 조회
    -   주문 취소
-   기타 요구사항
    -   상품은 재고 관리가 피룡하다
    -   상품의 종류는 도서, 음반, 영화가 있다
    -   상품을 카테고리로 구분할 수 있다

</br>

<b>회원 테이블 분석</b>

![image](https://user-images.githubusercontent.com/96561194/210069043-17efbb97-a303-4818-9c47-50dc7e2ecd23.png)

</br>

<b>연관관계 매핑 분석</b>

-   회원과 주문

    -   1 : N, N: 1의 양방향 관계, 따라서 연관관계의 주인을 정해야함
    -   <b> 연관관계의 주인은 외래키가 있는 쪽을 연관관계의 주인으로 정하는 것이 좋음 </b>
    -   Order.member 를 ORDERS.MEMBER_ID 외래키와 매핑

-   주문상품과 주문

    -   N : 1 양방향 관계
    -   외래키가 있는 주문상품이 연관관계의 주인
    -   OrderItem.order 를 ORDER_ITEM.ORDER_ID 외래키와 매핑

-   주문상품과 상품
    -   N : 1 양방향 관계
    -   OrderItem.item 을 ORDER_ITEM.ITEM_ID 외래키와 매핑
-   주문과 배송
    -   1 : 1 양방향 관계
    -   Order.delivery 를 ORDERS.DELIVERY_ID 외래키와 매핑
-   카테고리와 상품
    -   M : N 연관관계
    -   @ManyToMany 를 사용해 매핑
    -   실무에서는 사용하지 말것

### <b> 엔티티 클래스 개발 </b>

-   [회원 엔티티](./code/Member.java)
    -   엔티티의 식별자는 Id 사용 PK 컬럼명은 member_id 사용
    -   엔티티는 타입 (Member)이 있으므로 id 필드만으로 쉽게 구분 가능
    -   id 명은 무엇을 사용해도 관계 없으나 <b>"일관성"</b> 이 중요
-   [주문 엔티티](./code/Order.java)
-   [주문 상태(Enum)](./code/OrderStatus.java)
-   [주문 상품 엔티티](./code/OrderItem.java)
-   [상품 엔티티](./code/Item.java)
    -   실제로는 구현체를 사용하기 때문에 추상화 클래스 사용
-   [상품-도서 엔티티](./code/Book.java)
-   [상품-음반 엔티티](./code/Album.java)
-   [상품-영화 엔티티](./code/Movie.java)
-   [배송 엔티티](./code/Delivery.java)
-   [배송 상태(Enum)](./code/DeliveryStatus.java)
-   [카테고리 엔티티](./code/Category.java)
    -   @ManyToMany 는 편리한것 같지만 중간테이블 (CATEGORY_ITEM)에 컬럼 추가가 어렵고 세밀한 쿼리를 실행하기 어렵다
    -   실무에서는 사용하는데 한계가 있음
    -   되도록이면 중간 엔티티 CategoryItem 을 만들고 @ManyToOne, @OnToMany로 매핑해서 사용하는것이 좋다
-   [주소 값 타입(Embeded)](./code/Address.java)
    -   값 타입은 변경 불가능하게 설계해야함
    -   @Setter를 제거하고 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스로 생성
    -   JPA 스펙상 엔티티나 임베디드 타입(@Embeddable)은 자바 기본 생성자를 public 또는 protected로 설정해야함 (protected가 그나마 안전)

### <b> 엔티티 설계시 주의점 </b>

1. <b>엔티티에는 가급적 Setter를 사용하지 말자</b>

    - Setter가 모두 열려있다면 변경포인트가 너무 많아서 유지보수가 매우 어렵다, 리팩토링을 통해 제거해야함

2. <b>모든 연관관계는 지연로딩으로 설정!</b>

    - 즉시로딩(EAGER)은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어려움
    - 특히 JPQL 실행시 N+1 문제가 자주 발생함 (order 가 호출되면 order와 연관관계를 맺고있는 모든 테이블을 호출함)
    - 실무에서 모든 연관관계는 지연로딩 (LAZY)로 설정해야한다
    - @XToOne(OneToOne, ManyToOne)은 기본이 EAGER이므로 필히 LAZY로 설정

3. <b> 컬렉션은 필드에서 초기화 하자</b>

    - 컬렉션은 필드에서 초기화 하는 것이 안전하다
        - null 문제에서 안전함
    - 하이버네이트는 엔티티를 영속화 할 때, 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경함
    - 만약 getOrders() 처럼 임의의 메서드에서 컬렉션을 잘못 생성하면 하이버네이트 매커니즘에 문제가 발생할 수 있음

4. <b>테이블, 칼럼명 생성 전략</b>
    - 스프링 부트 신규 설정 (엔티티(필드) -> 테이블(컬럼))
        1. 카멜 케이스 -> 언더스코어 (memberPoint -> member_point)
        2. .(점) -> \_(언더스코어)
        3. 대문자 -> 소문자
    - 논리명 생성
        - 명시적으로 컬럼, 테이븖여을 직접 적지 않으면 ImplicitNamingStrategy 사용 (논리명 적용)
    - 물리명 적용
        - 모든 논리명에 적용됨, 실제 테이블에 적용
        - username -> usernm 등으로 회사 관례에 따라 바꿀 수 있다

</br>
<b>인프런 김영한님 강의 참고</b>
