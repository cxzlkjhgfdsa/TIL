# 2022_12_27

</br>

## JPA, DB 설정 동작 확인

</br>

### <b>JPA, DB 설정</b>

</br>
main/resources/application.yml

```
spring:
    datasource:
        url: jdbc:h2:tcp://localhost/~/jpashop
        username: sa
        password:
        driver-class-name: org.h2.Driver
    jpa:
        hibernate:
        ddl-auto: create
    properties:
        hibernate:
            # show_sql: true
            format_sql: true
logging.level:
    org.hibernate.SQL: debug
```

-   spring.jpa.hibername.ddl-auto: create
    -   이 옵션은 애플리케이션 실행 시점에 테이블을 drop 하고, 다시 생성한다

<b>참고</b>

-   모든 로그 출력은 가급적 로거를 통해 남겨야한다
-   show_sql : 옵션은 System.out 에 하이버네이트 실행 SQL을 남긴다
-   org.hibernate.SQL : 옵션은 logger를 통해 하이버네이트 실행 SQL을 남긴다
-   yml 파일은 (스페이스)2칸 으로 계층을 만들기 때문에 띄어쓰기에 특별히 주의해야한다

</br>

### <b> JPA 정상 동작 확인</b>

<b>회원 엔티티</b>

```
@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    private Long id;
    private String username;

}
```

</br>
<b>회원 리포지토리 </b>

```
@Repository
public class MemberRepository {
    @PersistenceContext
    EntityManager em;
    public Long save(Member member) {
        em.persist(member);
        return member.getId();
    }
    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
```

</br>
<b>테스트</b>

```
@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(false)
    public void testMember() {
        Member member = new Member();
        member.setUsername("memberA");
        Long savedId = memberRepository.save(member);
        Member findMember = memberRepository.find(savedId);
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());

        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername())
        ;
        Assertions.assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성
        보장
    }

}
```

<b>JPA 기본 안듣고와서 적어보는 것</b>

-   어노테이션
    -   @Id : 데이터베이스 테이블의 기본 키 (PK)와 객체의 필드를 매핑시켜주는 어노테이션
        -   @Id만 사용할 경우 기본 키를 직접 할당해 주어야 한다
        -   기본 키를 직접 할당하는 대신 데이터베이스가 생성해주는 값을 사용하려면 @GenerateValue를 사용하면 된다
    -   @GenerateValue : 기본 키를 자동으로 생성해주는 어노테이션
    -   @SequenceGenerator : 시퀀스를 사용해서 기본 키를 생성한다 -> 시퀀스를 지원하는 DB에서 사용가능(오라클, H2, PostgreSQL)
    -   @Transactional
        -   트랜잭셔널이 테스트와 같이 붙어있으면 테스트를 마친 뒤 자동으로 롤백한다 (테스트를 수월하게 하기 위함)
-   그외
    -   findMember == member 인 이유
    -   같은 트랜잭션 안에서는 같은 영속성 컨택스트가 보장되기 때문에 idx 값이 똑같으면 같은 엔티티를 반환한다
    -   finc(saveId) 할때, save(member)로 이미 영속성 컨텍스트에 저장되어있기 때문에 영속성 컨텍스트에서 find해서 가져온다

</br>
<b>영속성 컨텍스트</b>

-   엔티티를 영구 저장하는 환경
-   애플리케이션과 데이터베이스 사이에서 객체를 보관하는 가상의 데이터베이스 같은 열할
-   엔티티 매니저를 통해 엔티티를 저장하거나 조회하면 엔티티 매니저는 영속성 컨텍스트에 엔티티를 보관하고 관리함
-   <b>엔티티 생명주기</b>
    -   비영속 : 영속성 컨텍스트와 전혀 관계가 없는 상태
    ```
    Member member = new Member();
    ```
    -   영속 : 엔티티 매니저를 통해서 엔티티를 영속성 컨텍스트에 저장한 상태, 영속성 컨텍스트에 의해 관리됨
    ```
    em.persist(member);
    ```
    -   준영속 : 영속성 컨텍스트가 관리하던 영속상태의 엔티티를 더이상 관리하지 않으면 준영속상태
    ```
    // 엔티티를 영속성 컨텍스트에서 분리해 준영속 상태로 만든다.
    em.detach(member);
    // 영속성 콘텍스트를 비워도 관리되던 엔티티는 준영속 상태가 된다.
    em.claer();
    // 영속성 콘텍스트를 종료해도 관리되던 엔티티는 준영속 상태가 된다.
    em.close();
    ```
        -   1차 캐시, 쓰기 지연, 변경 감지, 지연 로딩을 포함한 영속성 컨텍스트가 제공하는 어떠한 기능도 동작하지 않는다
        -   식별자 값을 가지고 있다
    -   삭제 : 엔티티를 영속성 컨텍스트와 데이터베이스에서 삭제한다.
    ```
    em.remove(member);
    ```

</br>
<b>쿼리 파라미터 로그 남기기</b>

```
implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.6'
```

build.gradle에 다음과 같이 추가

-   쿼리파라미터 로그는 개발단계에서는 편하게 사용해도 되나, 운영단계에서는 테스트 해보고 사용해야한다

</br>
<b>인프런 김영한님 강의 참고</b>
