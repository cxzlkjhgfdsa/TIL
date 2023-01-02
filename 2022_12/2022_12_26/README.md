# 2022_12_26

</br>

## 예외처리, 반복 - 스프링

</br>

### <b>예외와 인터페이스</b>

<b>체크 예외와 인터페이스</b>

-   SQLException과 같은 특정 구현 기술에 종속적인 체크 예외를 포함하게 되면 인터페이스도 해당 예외를 포함해야함
-   인터페이스가 JDBC 에 종속적이게됨
-   인터페이스의 목적에 크게 위배됨

</br>

<b>런타임 예외와 인터페이스</b>

-   런타임 예외는 따로 선언하지 않아도 되기 때문에 인터페이스가 특정 기술에 종속적일 필요가 없다

</br>

<b>런타임 예외 적용</b>

-   [MemberRepo인터페이스](./code/MemberRepository.java)
-   [MyDBException 런타임예외](./code/MyDbException.java)
-   [런타임 예외 적용 MemberRepo](./code/MemberRepositoryV4_1.java)
-   [런타임 예외 적용 Service](./code/MemberServiceV4.java)
-   [런타임 예외 적용 ServiceTest](./code/MemberServiceV4Test.java)

</br>

<b>정리</b>

-   체크예외를 런타임 예외로 변환하면서 인터페이스와 서비스 계층의 순수성을 유지할 수 있게 됨
-   JDBC에서 다른 구현 기술로 변경하여도 서비스 계층의 코드를 변경하지 않고 유지할 수 있음
-   MyDbException이라는 예외만 넘어오기 때문에 예외를 구분할 수 없음
-   특정상황에서 예외를 잡아서 복구하기 위해서는 방법이 필요함

</br>

### <b>데이터 접근 예외 직접 만들기</b>

-   데이터 베이스 오류에 따라 특정 예외는 복구하고 싶을 수 있다
-   EX) 회원가입시 DB에 이미 같은 ID가 있으면 ID 뒤에 숫자를 붙여서 새로운 ID를 만들어야 하는 경우
-   데이터베이스가 오류코드를 반환하면 오류코드를 받은 JDBC는 SQLException을 던진다 이 SQLException 에는 errorCode라는 것이 들어있음

</br>
<b>데이터베이스 오류 코드 그림</b>

![image](https://user-images.githubusercontent.com/96561194/209631305-aeb6c441-999e-4b45-ab61-74100441b244.png)

-   데이터베이스마다 반환하는 오류코드가 다르다
-   SQL의 오류코드를 활용하기 위해서는 SQLException에 의존하게돔 (서비스 계층 순수성 무너짐)

</br>
<b>문제 해결</b>

-   [필요한 예외 생성](./code/MyDuplicateKeyException.java)
    -   데이터가 중복된 경우에만 던짐
    -   직접 만든 것이기 때문에 JDBC, JPA 같은 기술에 종속적이지 않음
-   [테스트](./code/ExTranslatorV1Test.java)

결과

```
Service - saveId=myId
Service - 키 중복, 복구 시도
Service - retryId=myId492
```

같은 ID를 저장했지만 , 중간에 예외를 잡아서 복구한 것을 확인할 수 있음

</br>
<b>문제점</b>

-   SQLErrorCode는 각각 데이터베이스 마다 다름
    -   데이터베이스가 변경되면 에러코드를 모두 변경해야함
-   데이터베이스가 전달하는 오류는 수백 가지가 넘음 (상황에 맞는 예외를 다 만드는것은 너무 힘듬)

</br>

### <b>스프링 예외 추상화</b>

</br>
<b>스프링 데이터 접근 예외 계층</b>

![image](https://user-images.githubusercontent.com/96561194/209632188-e66b57a3-0378-43a8-a05e-fdc2ff181b09.png)

-   스프링은 데이터 접근 게층에 대한 수십 가지 예외를 정리해서 일관된 예외 계층을 제공
-   각각의 예외는 특정 기술에 종속적이지 않다
-   예외의 최고 상위는 DataAccessException
    -   Transient는 일시적이라는 뜻 , 동일한 SQL을 다시 시도했을 때 성공할 가능성 있음
        -   쿼리 타임아웃, 락과 관련된 경우
    -   NonTransient는 일시적이지 않다는 뜻 , 같은 SQL을 반복해도 실패하는 경우
        -   SQL 문법오류, DB 제약조건 위배 등

</br>

<br>스프링 제공하는 예외 변환기 사용<br>

스프링은 데이터베이스에서 발생하는 오류 코드를 스프링이 정의한 예외로 자동으로 변환해주는 변환기를 제공

-   [예외 변환기 예제](./code/SpringExceptionTraslatorTest.java)
    -   SQL문법이 잘못되니 BadGrammarException을 반환하는 것을 확인할 수 있음

</br>
<b>정리</b>

-   스프링은 데이터 접근 계층에 대한 일관된 예외 추상화를 제공한다
-   스프링은 예외 변환기를 통해서 SQLExeption 의 ErrorCode에 맞는 적절한 스프링 데이터를 접근 예외로 변환해준다
-   특정 기술에 종속적이지 않게 되어 JDBC -> JPA 로 구현 기술을 변경하여도 예외로 인한 변경을 최소화 가능하다
-   스프링에 대한 종속성은 발생한다
-   [스프링 예외 추상화 적용](./code/MemberRepositoryV4_2.java)
-   [스프링 예외 추상화 테스트](./code/MemberServiceV4Test.java)

</br>

### <b>JdbcTemplate</b>

</br>
<b>JDBC 반복 문제</b>

-   커넥션 조회, 커넥션 동기화
-   PreparedStatement 생성 및 파라미터 바인딩
-   쿼리 실행
-   결과 바인딩
-   예외 발생 시 스프링 예외 변환기 실행
-   리소스 종료

상당히 많은 반복이 일어남 -> 처리하기 위해 JdbcTemplate 활용

-   [JdbcTemplate적용](./code/MemberRepositoryV5.java)
    -   코드가 상당히 간단해짐
    -   트랜잭션을 위한 커넥션 동기화, 스프링 예외 변환기 자동으로 실행

</br>

### <b>정리</b>

-   서비스 계층의 순수성
    -   트랜잭션 추상화 + 트랜잭션 AOP 덕분에 서비스 계층의 순수성을 최대한 유지하면서 서비스 계층에서 트랜잭션을 사용 가능
    -   스프링이 제공하는 예외 추상화와 예외 변환기 덕분에 , 데이터 접근 기술이 변경되어도 서비스 계층의 순수성을 유지 가능
    -   서비스 계층이 리포지토리 인터페이스에 의존한 덕분에 향후 리포지토리가 다른 구현기술로 변경되어도 서비스계층은 순수성 유직 가능
-   리포지토리에서 JDBC 를 사용하는 반복 코드가 JdbcTemplate으로 대부분 제거되었다.

</br>
<b>인프런 김영한님 강의 참고</b>
