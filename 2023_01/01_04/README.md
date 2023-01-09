# 2023_01_04

</br>

### 웹 계층 개발

html 관련 코드는 전부 제외함

</br>
<b>홈 컨트롤러 등록</b>

-   [홈 컨트롤러](./code/HomeController.java)

</br>
<b>회원 등록, 수정</b>

-   폼 객체를 사용해서 화면 계층과 서비스 계층을 명확하게 분리
-   [회원 등록 폼 객체](./code/MemberForm.java)
-   [회원 컨트롤러](./code/MemberController.java)
    -   회원 등록 폼이동은 GET
    -   회원 등록은 POST
    -   수정 폼은 GET
    -   수정은 POST

</br>
<b>상품 등록, 수정</b>

-   [상품 컨트롤러](./code/ItemController.java)
-   [상품 서비스](./code/ItemService.java)

<b> 변경감지와 Merge </b>

준영속 엔티티를 수정하는 2가지 방법

1. 변경감지
2. 병합 (Merge)

</br>
<b>변경감지</b> (추천하는 방법)

```
@Transactional
void update(Item itemParam) { //itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
    Item findItem = em.find(Item.class, itemParam.getId()); //같은 엔티티를조회한다.
    findItem.setPrice(itemParam.getPrice()); //데이터를 수정한다.
}
```

-   영속성 커텍스트 엔티티를 다시 조회한 후 데이터를 수정하는 방법
-   트랜잭션 안에서 엔티티를 다시 조회, 변경할 값 선택 -> 트랜잭션 커밋 시점에 변경 감지(Dirty Checking)
-   위 동작에서 UpdateQuery가 실행됨

</br>
<b>병합 (Merge)</b>

```
@Transactional
void update(Item itemParam) { //itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
    Item mergeItem = em.merge(itemParam);
}
```

![image](https://user-images.githubusercontent.com/96561194/211201489-46a1b702-9600-4ed9-a7c7-596613208668.png)

<b>중요</b>

-   병합과 변경감지의 동작방식은 거의 유사
-   다만 변경감지는 원하는 속성만 선택해서 변경할 수 있지만 병합은 불가능 (전부 변경됨)
-   병합을 사용할 시 변경값이 없다면 null로 변경될 수 있음
-   따라서 병합보다는 변경감지를 사용하는 것이 좋음

</br>
<b>상품 리포지토리의 저장 메소드 분석</b>

```
public void save(Item item) {
    if (item.getId() == null) {
        em.persist(item);
    } else {
        em.merge(item);
    }
 }

```

-   식별자 값이 없으면 persist (영속화)(insert)
-   있으면 merge (수정)(update)
-   저장과 수정을 구분하지 않기때문에 로직이 단순해짐

</br>
<b>상품 주문</b>
</br>

-   [주문 컨트롤러](./code/OrderController.java)
-   [주문 서비스](./code/OrderService.java)
-   [주문 리포지토리](./code/OrderRepository.java)

</br>
<b>인프런 김영한님 강의 참고</b>
