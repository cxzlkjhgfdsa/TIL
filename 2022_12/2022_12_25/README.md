# 2022_12_25

</br>

## 자바 예외 이해

</br>

### <b>예외 계층</b>

</br>

<b>예외 계층 그림</b>

![image](https://user-images.githubusercontent.com/96561194/209470966-a4adbed3-8af2-45cc-9fb2-30656948a9a7.png)

-   예외도 객체다 => 최상위 부모는 Object
-   Throwable => 최상위 예외
-   Error : 메모리 부족이나 심각한 시스템 오류와 같이 애플리케이션에서 복구 불가능한 예외
    -   애플리케이션 개발자는 이 예외를 잡으려고 해서는 안됨
-   Exception : 체크 예외
    -   애플리케이션 로직에서 사용할 수 있는 실직적인 최상위 예외
    -   Exception과 그 하위 예외는 모두 컴파일러가 체크하는 체크예외 (RuntimeException 제외)
-   RuntimeException :언체크 예외, 런타임 예외
    -   컴파일러가 체크하지않는 언체크 예외
    -   RuntimeException과 그 자식 예외는 모두 언체크 예외
    -   주로 런타임 예외라고 부름

</b>

### <b>예외 기본 규칙</b>

</br>

<b>예외 처리</b>

![image](https://user-images.githubusercontent.com/96561194/209471641-d391a521-d832-4236-a597-d0f59d9adf1e.png)

-   5번에서 예외 처리시 애플리케이션 로직이 정상 흐름으로 동작

<b>예외 던짐</b>

![image](https://user-images.githubusercontent.com/96561194/209471667-3cc3b3be-e8ab-48cf-895d-5e5589b97446.png)

-   에외 처리 못할 시 호출한 곳으로 예외 계속던짐

</br>

<b>예외의 2가지 기본 규칙</b>

1. 예외는 잡아서 처리하거나 던져야함
2. 예외를 잡거나 던질 때 지정한 예외뿐만 아니라 그 예외의 자식도 함께 처리된다

</br>

### <b>체크 예외 기본 이해</b>

-   컴파일러가 체크하는 예외
-   체크 예외는 잡아서 처리하거나 밖으로 던지도록 선언해야함
-   [체크 예외 예시 코드](./code/CheckedTest.java)
    -   Exception을 상속받으면 체크 예외

</br>

<b>체크 예외 장단점</b>

-   장점
    -   개발자가 실수로 예외를 누락하지 않도록 컴파일러를 통해 문제를 잡아주는 훌륭한 안전 장치
-   단점
    -   실제로 개발자가 모든 체크 예외를 반드시 잡거나 처리해야 하기 때문에 너무 번거로워짐
    -   크게 신경쓰고 싶지 않은 예외까지 모두 챙겨야함

</br>

### <b>언체크 예외 기본 이해</b>

-   RuntimeException과 그 하위 예외
-   컴파일러가 체크하지 않음
-   던지는 throws 를 생략 가능 -> 이경우 자동으로 예외 던짐
-   [언체크 예외 예시](./code/UncheckedTest.java)
    -   언체크도 필요시 잡아서 처리 가능
    -   주로 생략하지만 , 중요 예외의 경우 예외를 선언해주면 해당 코드를 호출하는 개발자가 IDE를 통해 해당 예외 발생을 편리하게 인지

</br>

<b>언체크 예외 장단점</b>

-   장점
    -   신경쓰고 싶지 않은 예외 무시 가능
    -   신경 쓰고 싶지 않은 예외의 의존관계를 참조하지 않아도 됨
-   단점
    -   개발자가 실수로 누락할 수 있음

</br>

### <b>체크 예외 활용</b>

</br>

<b>기본 원칙</b>

-   기본적으로는 언체크(런타임) 예외 사용
-   체크 예외는 비즈니스 로직상 의도적으로 던지는 예외에만 사용
    -   반드시 잡아서 처리해야하는 문제
        -   계좌 이체 실패 예외
        -   결제 시 포인트 부족
        -   로그인 시 ID, PW 불일치

</br>
<b>체크 예외 문제점</b>

![image](https://user-images.githubusercontent.com/96561194/209588182-72a4b6bf-e66f-4859-bdce-41027dd24599.png)

-   [체크예외 문제점 코드](./code/CheckedAppTest.java)

<b>체크예외의 2가지 문제점</b>

1.  복구 불가능한 예외
    -   SQLException => 데이터 베이스에 무언가 문제가 있어서 발생 (문법, 서버다운 ...)
    -   대부분 복구 불가능함
    -   서비스나 컨트롤러는 해결 불가능
    -   일관성있게 공통으로 처리해야함 (ControllerAdvice)
    -   오류 로그를 남기고 개발자가 해당 오류를 빠르게 인지하는것이 중요
2.  의존 관계에 대한 문제
    -   대부분 복구 불가능 한 예외기 때문에 서비스나 컨트롤러도 throws 를 통해 예외를 던져야함
    -   서비스 , 컨트롤러에서 java.sql.SQLException 을 의존하게 됨 (JDBC 기술)
    -   JDBC -> JPA 변경시 코드를 모두 변경해야함

</br>

### <b>체크 예외 활용</b>

</br>

<b>런타임 예외 사용</b>

![image](https://user-images.githubusercontent.com/96561194/209588325-a27dbdbe-97cf-42e5-ae7f-836379b73131.png)

-   SQLException -> RuntimeSQLException으로 변환
-   런타임 예외기 때문에 서비스, 컨트롤러는 해당 예외를 처리할 수 없다면 그냥 두면 된다
-   [런타임 예외 사용 변환 코드](./code/UncheckedAppTest.java)
    -   SQLException -> RuntiomeSQLException으로 전환해서 던짐
    -   예외 전환시 기존 예외를 꼭 포함해주어야 함
    -   런타임 예외는 해당 객체가 처리할 수 없는 예외는 무시하기 때문에 체크예외처럼 의존하지 않음

<b>런타임 예외 문서화</b>

-   런타임 예외는 문서화를 잘해야한다
-   또는 코드에 throws를 남겨서 중요한 예외를 인지할 수 있게 해준다

```
/**
 * Issue a single SQL execute, typically a DDL statement.
 * @param sql static SQL to execute
 * @throws DataAccessException if there is any problem
 */
void execute(String sql) throws DataAccessException;
```

</br>

### <b>예외 포함 스택 트레이스</b>

예외를 전환할 때는 꼭 기존 예외를 포함해야한다. 그렇지 않으면 스택 트레이스를 확인할 때 심각한 문제 발생

</br>
<b>인프런 김영한님 강의 참고</b>
