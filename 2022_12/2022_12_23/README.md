# 2022_12_23

</br>

## 트랜젝션

</br>

### 트랜젝션 개념

데이터를 저장할 때 데이터베이스를 사용하는 이유는 트랜잭션이라는 개념을 지원하기 때문ㅇ

**트랜잭션**

-   데이터베이스에서 하나의 거래를 안전하게 처리하도록 보장해주는 것을 뜻함
-   5000원 계좌이체
    1.  A의 잔고를 5000원 감소
    2.  B의 잔고를 5000원 증가
    -   1번 성공후 2번에서 문제 터지면 A잔고에서 5000원 만 증발하는 심각한 문제가 발생함
    -   이러한 문제를 방지하기 위해 하나의 거래를 묶어서 처리해주는 트랜잭션이 등장
    -   DB 정상반영하게 해주는 Commit과 문제가 생겼을 때 되돌아가는 Rollback 지원\*\*

</br>

### <b>트랜잭션 ACID </b>

-   원자성 (Atomicity)
    -   트랜잭션 내에서 실행한 작업들은 마치 하나의 작업처럼 모두 성공하거나 실패해야함
-   일관성 (Consistency)
    -   모든 트랜잭션은 일관성있는 데이터베이스를 유지해야함 (ex-> DB에서 정한 무결성 제약조건을 항상 만족)
-   격리성 (Isolation)
    -   동시에 실행되는 트랜잭션들이 서로에게 영향을 미치지 않도록 격리한다
    -   동시에 같은 데이터를 수정하지 못하도록 해야한다
    -   격리성은 성능 이슈로 인해 트랜잭션 격리수준을 선택할 수 있다
-   지속성 (Durability)
    -   트랜잭션을 성공적으로 끝내면 그 결과가 항상 기록되어야함
    -   중간에 시스템 문제가 발생해도 데이터베이스 로그 등을 사용해서 성공한 트랜잭션 내용을 복구해야함

</br>

### <b>데이터베이스 연결 구조와 DB 세션</b>

**데이터베이스 연결 구조**

![image](https://user-images.githubusercontent.com/96561194/209260178-5a868250-7e75-480e-8fbb-02af0bde13d3.png)

-   클라이언트가 데이터베이스 서버에 연결을 요청하고 커넥션을 맺을 때 DB는 서버 내부에 세션이라는 것을 만듬
-   클라이언트를 통해 SQL을 전달하면 커넥션에 연결된 세션이 SQL을 실행함
-   세션은 트랜잭션을 시작하고, 커밋 또는 롤백을 통해 트랜잭션을 종료함
-   사용자가 커넥션을 닫거나, DBA가 세션을 강제로 종료하면 세션은 종료됨

</br>

### <b>트랜잭션 개념 </b>

**트랜잭션 사용법**

-   데이터 변경쿼리를 실행하고 그 결과를 반영하려면 commit, 반영하고 싶지 않으면 rollback 호출
-   커밋을 호출하기 전까지는 임시로 데이터를 저장하는 것
    -   해당 트랜젝션을 시작한 세션(사용자) 에게만 변경 데이터가 보이고 다른 사용자에게는 보이지 않음
    -   해당 트랜잭션을 시작한 세션(사용자) 가 commit할 시 다른 사용자에게 보임
    -   rollback할시 해당 세션(사용자) 에게도 보이지 않음
-   기본은 자동 커밋으로 되어있다 트랜잭션을 사용하기 위해서는 수동 커밋으로 바꿔야함

```
set autocommit false; //수동 커밋 모드 설정
```

</br>

### <b>DB 락 </b>

세션 1이 트랜잭션을 시작하고 데이터를 수정하는 동안 아직 커밋을 수행하지 않았을 때, </br>
세션 2가 동시에 같은 데이터를 수정하게 되면 트랜잭션에 원자성을 깨뜨리게 됨 </br>
이런 문제들을 해결하기 위해 트랜잭션을 시작하고 끝날때까지 해당 데이터를 수정할 수 없게하는 DB락이 나오게됨 </br>

<b>동작 원리</b>

1.  세션 1이 트랜잭션을 시작할 때 해당 데이터의 락을 가져옴
2.  다른 세션에서 해당 데이터에 접근할 때 락이 없다면 수정 할 수 없음
3.  세션 1이 모든 작업을 끝내고 commit or Rollback 하였을 때 락을 다시 돌려놓음
4.  해당 데이터에 릭이 있다면 다른 세션에서 해당 데이터 수정 가능

</br>

<b>세션 락 타임아웃 </b>

-   한 세션이 데이터에 접근할 때 그 데이터에 락이 없다고 생각해보자
-   락이 다시 돌아올 때까지 무작정 기다릴 수 없기 때문에 SET LOCK_TIMEOUT <milliseconds> 형식으로 락 타임아웃을 설정
-   ex) SET LOCK_TIMEOUT 10000 -> 10초 동안 세션이 락을 얻지 못한다면 타임아웃 오류가 발생한다

<br>

### <b>DB 락 - 조회 </b>

-   일반적인 조회는 락을 사용하지않음
-   데이터베이스 마다 상이하지만, 보통 조회는 락을 획득하지 않고 바로 데이터를 조회할 수 있음

<b>조회와 락</b>

-   데이터를 조회할 때도 락을 획득하고 싶을 때가 있음
-   이럴때는 select from update 구문을 사용하면 됨

```
set autocommit false;
select * from member where member_id="memberA" for update ;
```

</br>

### <b>트랜잭션 적용</b>

<b> 트랜잭션 없이 구현 </b>

-   [비즈니스로직](./code/MemberServiceV1.java)
-   [테스트코드](./code/MemberServiceV1Test.java)

트랜잭션을 적용하지 않아 이체중 예외가 발생하면 memberA 의 돈만 2천원 빠져나가게됨

</br>

### <b>트랜잭션 적용2</b>

<b> 트랜잭션을 사용해서 문제점 해결 </b>

-   트랜잭션은 비즈니스 로직이 있는 서비스 계층에서 시작해야함
-   비즈니스 로직이 잘못되면 해당 비즈니스 로직으로 인해 문제가 되는 부분을 함께 롤백해야 하기 때문ㅇ
-   트랜잭션을 시작하려면 커넥션이 필요, 서비스 계층에서 커넥션 생성, 커밋 이후 종료까지 해야함
-   애플리케이션이 DB 트랜잭션을 사용하려면 트랜잭션을 사용하는 동안 같은 커넥션을 유지해야 한다 -> 같은 세션을 사용해야 하기 때문
-   이문제를 해결하기 위한 가장 단순한 방법은 파라미터로 전달해서 같은 커넥션이 유지되도록 하는 것

</br>
<b>트랜잭션-파라미터</b>

-   [리포지토리](./code/MemberRepositoryV2.java)
-   [비즈니스로직](./code/MemberServiceV2.java)
-   [테스트코드](./code/MemberServiceV2Test.java)

트랜잭션을 적용해서 이체중 예외가 발생해도 다시 원상복귀 되는것을 확인할 수 있음

<b> 남은 문제 </b>

-   애플리케이션에서 DB 트랜잭션을 적용하려면 서비스 계층이 매우 복잡해짐
-   커넥션을 유지하는 코드를 변경하는것도 어려움

<details>
    <summary>기존 예외 포함</summary>
    
    13:10:45.626 [Test worker] INFO hello.jdbc.exception.basic.UncheckedAppTest - 
    ex
    hello.jdbc.exception.basic.UncheckedAppTest$RuntimeSQLException: 
    java.sql.SQLException: ex
    at 
    hello.jdbc.exception.basic.UncheckedAppTest$Repository.call(UncheckedAppTest.ja
    va:61)
    at 
    hello.jdbc.exception.basic.UncheckedAppTest$Service.logic(UncheckedAppTest.java
    :45)
    at 
    hello.jdbc.exception.basic.UncheckedAppTest$Controller.request(UncheckedAppTest
    .java:35)
    at 
    hello.jdbc.exception.basic.UncheckedAppTest.printEx(UncheckedAppTest.java:24)
    Caused by: java.sql.SQLException: ex
    at 
    hello.jdbc.exception.basic.UncheckedAppTest$Repository.runSQL(UncheckedAppTest.
    java:66)
    at 
    hello.jdbc.exception.basic.UncheckedAppTest$Repository.call(UncheckedAppTest.ja
    va:59)

</details>

<details>
    <summary>기존 예외 미포함</summary>
    
    [Test worker] INFO hello.jdbc.exception.basic.UncheckedAppTest - ex
    hello.jdbc.exception.basic.UncheckedAppTest$RuntimeSQLException: null
    at 
    hello.jdbc.exception.basic.UncheckedAppTest$Repository.call(UncheckedAppTest.ja
    va:61)
    at 
    hello.jdbc.exception.basic.UncheckedAppTest$Service.logic(UncheckedAppTest.java
    :45)
    
</details>

-   예외를 포함하지 않으면 기존에 발생한 SQLException 과 스택 트레이스를 확인할 수 없다
-   DB에서 발생한 예외를 확인할 수 없는 심각한 문제이다

</br>
<b>인프런 김영한님 강의 참고</b>
