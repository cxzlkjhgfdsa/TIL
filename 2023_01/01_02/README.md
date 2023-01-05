# 2023_01_02

</br>

### <b> 회원 도메인 개발 </b>

</br>
<b>회원 리포지토리 코드</b>

-   [회원 리포지토리](./code/MemberRepository.java)
    -   @Repository : 스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 변환
    -   @PersistenceContext : 엔티티 메니져(EntityManager)주입

</br>
<b>회원 서비스 코드</b>

-   [회원 서비스](./code/MemberService.java)
    -   @Transactional : 트랙잭션, 영속성 컨텍스트
        -   readOnly = true : 데이터의 변경이 없는 읽기 전용 메소드에 사용, 영속성 컨텍스트를 플러시 하지 않으므로 약간의 성능 향상(읽기전용에는 모두 적용하는 것이 좋다)
        -   데이터베이스 드라이버가 (readOnly)지원하면 DB에서 성능 향상
    -   @Autowired
        -   생성자 Injection 많이 사용, 생성자가 하나면 생략 가능하다

<b>참고</b>

-   아이디 중복 검증 로직을 사용하여도, 멀티쓰레드 상황을 고려하여 실무에서는 DB 칼럼에 유니크 제약조건을 걸어주는 것이 좋음
-   스프링 필드 주입 대신 생성자 주입을 사용하는 것이 좋음
    -   변경 불가능한 안전한 객체 생성 가능
    -   생성자가 하나면 @Autowired 생략 가능
    -   final 키워드 추가시 컴파일 시점에 memberRepository를 설정하지 않는 오류 체크 가능
    -   lombok 의 @RequiredArgsConstructor 를 사용하면 생성자 주입 을 알아서 해줌
    -   스프링 데이터 JPA 사용시 EnitiyManager 또한 주입 가능하다 (@RequiredArgsConstructor)

</br>

<b>회원 가입 테스트 코드 </b>

-   [회원 가입 테스트 코드](./code/MemberServiceTest.java)
    -   @RunWith(SpringRunner.class) : 스프링과 테스트 통합
    -   @SpringBootTest : 스프링 부트 띄우고 테스트 (이것이 없으면 @Autowired 전부 실패)
    -   회원가입이 되었는지, 중복 회원가입이 불가능한지 체크

</br>
<b>테스크 케이스를 위한 설정</b>

-   테스트 케이스는 격리된 환경에서 실행하고, 끝나면 데이터를 초기화 하는 것이 좋다
-   그런 면에서 메모리 DB를 사용하는 것이 가장 이상적
-   테스트케이스를 위한 설정 파일과, 일반적으로 애플리케이션을 실행하는 환경은 보통 다르므로 설정파일 다르게 사용하는것이 좋음
    -   빈 application.yml 사용하면 됨 -> 알아서 메모리 db로 연결해줌

</br>

### <b>상품 도메인 개발 </b>

</br>
<b>상품 엔티티 코드(비즈니스 로직 추가)</b>

-   [상품 엔티티](./code/Item.java)
-   [상품 개수 부족 예외 추가](./code/NotEnoughStockException.java)
    -   <b>비즈니스 로직</b>
        -   addStock() : 파라미터로 넘어온 수만큼 재고를 늘린다
        -   removeStock() : 파라미터로 넘어온 수만큼 재고를 줄인다, 재고 부족시 예외 발생

</br>
<b>상품 리포지토리 코드</b>

-   [상품 리포지토리](./code/ItemRepository.java)
    -   id가 없다면 persist (신규 저장)
    -   id가 있다면 merge (업데이트)

</br>
<b>상품 서비스 코드</b>

-   [상품 서비스](./code/ItemService.java)

</br>
<b>출처 : 인프런 김영한님 강의 참고</b>
