# 2022_12_24

</br>

## 스프링과 문제해결 - 트랜잭션

</br>

### 문제점들

</br>
<b>애플리 케이션 구조</b>

![image](https://user-images.githubusercontent.com/96561194/209420175-ecaca1d3-ff88-49a5-8c95-3ba7122172de.png)

-   **프레젠테이셔 계층**
    -   UI와 관련된 처리 담당
    -   사용자 요청 검증
    -   주사용기술 : 서블릿 HTTP 같은 웹 기술, 스프링 MVC
-   **서비스 계층**
    -   비즈니스 로직 담당
    -   주 사용 기술: 가급적 특정 기술에 의존하지 않고, 순수 자바 코드로 작성
-   **데이터 접근 계층**
    -   실제 데이터베이스에 접근하는 코드
    -   주 사용 기술: JDBC, JPA, Mongo...

**순수 서비스 계층**

-   가장 중요한 계층은 비즈니스 로직이 들어있는 서비스 계층
-   UI, 데이터 저장기술은 변경할 수 있음
-   비즈니스 로직은 최대한 변경없이 유지되어야함
    -   이렇게 유지되게 하려면 서비스 계층을 특정 기술에 종속적이지 않게 개발해야함
    -   계층을 나눈 이유도 서비스 계층을 최대한 순수하게 유지하기 위한 목적
-   서비스 계층이 특정 기술에 종속되지 않아야 유지보수, 테스트가 쉬워짐

**문제점들**

최근 코드 -> [MemberServiceV2](../2022_12_23/code/MemberServiceV2.java)

-   javax.sql.DataSource, java.sql.Connection, java.sql.SQLException 등이 전부 JDBC 기술에 의존함
-   향후 JDBC에서 JPA로 바꾸어 사용하게 된다면 서비스 코드도 모두 함께 변경해야함
-   핵심 비즈니스 로직과 JDBC 기술이 섞여있어 유지보수가 어렵다

</br>

**문제 정리**

-   트랜잭션 문제
-   예외 누수 문제
-   JDBC 반복 문제

</br>

**트랜잭션 문제**

-   JDBC 구현 기술이 서비스 계층에 누수되는 문제
    -   서비스 계층이 순수해야함 (변화에 대응)
        -   데이터 접근 계층이 JDBC 코드를 몰아 넣은 것이 이유
    -   서비스 계층은 특정 기술에 종속되지 않아야함
        -   JDBC 에 의존하고있기 때문에 문제
-   트랜잭션 동기화 문제
    -   같은 트랜잭션을 유지하기 위해 커넥션을 파라미터로 넘겨야함
    -   유지해야하는 트랜잭션과, 유지하지 않아도 되는 트랜잭션을 분리해야함 (현재는 분리가 되지 않음)
-   트랜잭션 적용 반복 문제
    -   트랜잭션 적용코드에 반복이 많음 (try, catch, finally)

**예외 누수**

-   데이터 접근 계층의JDBC 구현 기술 예외가 서비스 계층으로 전파됨 (SQLException)
-   SQLException 은 JDBC 전용 기술이기 때문에 JPA로 변경시 그에 맞는 예외로 변경해야함

**JDBC 반복문제**

-   유사한 코드 반복이 너무 많음
    -   try, catch, finall....
    -   커넥션 열고 PreparedStatement ...

</br>

### <b>트랜잭션 추상화</b>

![image](https://user-images.githubusercontent.com/96561194/209420592-fb0340aa-c4bf-4300-8812-e6286a5c5d5a.png)

-   위와 같은 문제를 해결하기 위해 트랜잭션을 추상화 함
-   서비스 는 이제 특정 기술에 의존하는 것이 아닌 TxManager라는 추상화된 인터페이스에 의존하게됨
-   원하는 구현체를 DI를 통해 주입하면 됨
-   클라이언트인 서비스는 인터페이스에 의존하고 DI를 사용한 덕분에 OCP원칙을 지키게 되었음

</br>

### <b>트랜잭션 동기화</b>

![image](https://user-images.githubusercontent.com/96561194/209420696-eba97c5e-73f0-4764-9e9a-53e6f9826dbe.png)

-   스프링은 트랜잭션 동기화 매니저를 제공함
-   트랜잭션 동기화 매니져는 쓰레드 로컬을 사용해 커넥션을 동기화해줌
-   쓰레드 로컬을 사용하기 때문에 멀티쓰레드 상황에 안전하게 커넥션을 동기화 할 수 있다
-   커넥션이 필요하면 이제 트랜잭션 동기화 매니져를 통해 커넥션을 획득하면 된다 (파라미터 필요 X)

</br>

### </b>트랜잭션 문제해결-실습</b>

-   [리포지토리](./code/MemberRepositoryV3.java)
    -   DateSourceUtils.getConnetion() 동작방버
        -   트랜잭션 동기화 매니져가 관리하는 커넥션이 있을 시 해당 커넥션 반환
        -   없을 시 새로운 커넥션 생성해서 반환
    -   DataSource.releaseConnection()
        -   트랜잭션을 사용하기 위해 동기화된 커넥션은 커넥션을 닫지 않고 그대로 유지
        -   아니면 그냥 커넥션 닫아버림
-   [비즈니스로직](./code/MemberServiceV3_1.java)
    -   private final PlatformTransactionManager transectionManager
        -   트랜잭션 매니져를 주입받음
        -   JDBC 를 사용하기 때문에 JDBC 구현체를 주입
        -   롤백, 커밋 모두 가능
-   [테스트코드](./code/MemberServiceV3_1Test.java)

**정리**

-   트랜잭션 추상화 덕분에 서비스 코드는 더이상 JDBC에 의존하지 않음
    -   이후 JDBC에서 JPA 코드로 변경해도 서비스코드는 유지 가능
    -   SQLException 은 아직 문제가되므로 예외문제해결 필요함
-   트랜잭션 동기화 매니져 덕분에 커넥션을 파라미터로 넘기지 않아도 됨

### <b>트랜잭션 템플릿</b>

-   트랜잭션을 사용하는 로직을 살펴보면 같은 패턴이 반복되는 것을 확인할 수 있음
-   이 문제를 해결하기 위해 템플릿 콜백 패턴을 활용한다
-   [템플릿 활용 서비스 로직](./code/MemberServiceV3_2.java)
-   [템플릿 활용 테스트](./code/MemberServiceV3_2Test.java)
    -   트랜잭션 시작, 커밋 , 롤백 패턴 모두 제거
    -   트랜잭션 템플릿 기본 동작
        -   비즈니스 로직 정상 수행되면 커밋
        -   언체크 예외가 발생하면 롤백, 그외 경우 커밋

<b>문제</b>

-   서비스 로직은 가급적 핵심 비즈니스 로직만 있어야한다
-   트랜잭션 기술을 사용하려면 어쩔 수 없이 트랜잭션 코드가 나와야함

</br>

### <b>트랜잭션 AOP</b>

-   서비스 계층에 순수한 비즈니스 로직만 남길 수 있게 하기 위해 등장
-   스프링 AOP 를 통해 프록시를 도입하면 위의 문제가 해결됨

<b>프록시 도입 후</b>

![image](https://user-images.githubusercontent.com/96561194/209470057-271a2bc1-ec6d-48f7-98d1-5a58a6a3bba7.png)

-   프록시 도입 전 : 서비스나 비즈니스 로직과 트랜잭션 처리 로직이 함께 섞여있었음
-   프록시 도입 후 : 트랜잭션 프록시가 트랜잭션 처리 로직을 모두 가져간다, 트랜잭션을 시작한 후에 실제 서비스를 대신 호출해준다

<b>참고</b>

-   스프링 AOP를 적용하기 위해서는 어드바이저, 포인트컷, 어드바이스가 필요하다
-   현재 프로젝트는 스프링부트를 사용하기때문에 자동으로 스프링 컨테이너에 등록된다

</br>
<b>트랜잭션 AOP 적용 </b>

-   [AOP적용 비즈니스로직](./code/MemberServiceV3_3.java)
-   [AOP적용 테스트](./code/MemberServiceV3_3Test.java)
    -   @TestConfiguration : 테스트 안에서 내부 설정 클래스를 사용할 때 스프링 부트가 자동으로 만들어주는 빈들에 추가로 필요한 스프링 빈 등록
    -   @SpringBootTest : 테스트시 스프링 부트를 통해 컨테이너를 생성 (스프링 AOP를 적용하기 위함)

<b>트랜잭션 AOP 적용 전체 흐름</b>

![image](https://user-images.githubusercontent.com/96561194/209470323-befe4f14-e09d-4c3f-ba4a-4884b4bda65b.png)

</br>

### </b>스프링 부트 자동 리소스 등록</b>

<b>데이터 소스 - 자동 등록</b>

-   스프링 부트는 데이터소스(DataSource)를 스프링 빈에 자동으로 등록
-   자동으로 등록되는 스프링 빈 이름 : dataSource
-   개발자가 직접 데이터소스를 빈으로 등록시 스프링 부트는 데이터소스를 자동으로 등록하지 않음

application.properties <- 설정해야함

```
spring.datasource.url=jdbc:h2:tcp://localhost/~/test
spring.datasource.username=sa
spring.datasource.password=""
```

<b>트랜잭션 매니저 - 자동등록 </b>

-   스프링 부트는 적절한 트랜잭션 매니저를 자동으로 스프링 빈에 등록
-   자동으로 등록되는 이름 transcationManager
-   개발자가 직접 등록시 자동으로 등록하지 않음
