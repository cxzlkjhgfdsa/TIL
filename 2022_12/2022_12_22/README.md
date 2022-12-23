# 2022_12_22

</br>

## 커넥션 풀 , 데이터소스

</br>

### <b>커넥션 풀 등장이유</b>

-   데이터베이스는 커넥션으 획득할 때 복잡한 여러 과정글을 거침
-   그렇기 때문에 커넥션을 새로 만드는 것은 시간이 오래걸림
-   DB는 물론이고 애플리케이션 서버에서도 TCP/IP커넥션을 새로 생성하기 위한 리소스를 매번 사용해야함
-   고객이 어플리케이션을 사용할 때 SQL 처리시간 뿐만아니라 , 커넥션 재생성 시간추가되기 때문에 응답속도가 늦어짐

**이런 문제들을 해결하기 위해 커넥션을 미리 생성해두고 사용하는 커넥션 풀이라는 것이 등장함**

</br>

**커넥션 풀 초기화**

![image](https://user-images.githubusercontent.com/96561194/209254467-5d8d2a6a-3eeb-4b76-a116-f97de84489b9.png)

-   애플리케이션을 실행하는 시점에 커넥션풀은 필요한 만큼 커넥션을 미리 확보해서 풀에 보관한다
-   기본값은 10개

**커넥션 풀의 연결 상태**

![image](https://user-images.githubusercontent.com/96561194/209254564-4617a8b1-629d-4e77-9ea8-ae73b9abfaa5.png)

-   커넥션 풀에 들어 있는 커넥션은 TCP/IP로 DB커넥션이 연결되어있는 상태
-   언제든지 즉시 SQL을 DB에 전달 가능

**커넥션 풀 사용**

![image](https://user-images.githubusercontent.com/96561194/209254743-b56ee7d8-eec3-45c7-9a0a-21472357fec0.png)
![image](https://user-images.githubusercontent.com/96561194/209254768-8e7a1812-8b48-417b-8d84-05cedf85e94d.png)

-   애플리케이션 로직은 이제 커넥션 풀에 있는 커넥션을 가져다씀
-   커넥션을 모두 사용하고나면 종료하는것이 아니라 다시 커넥션 풀에 반환함

**정리**

-   커넥션 풀은 얻는 이점이 매우 크기 때문에 실무에서는 항상 기본으로 사용
-   직접 구현하는 것 보다, 사용이 편리하고 성능이 뛰어난 오픈소스 커넥션풀 사용을 추천 (HikariCP)

</br>

### <b>DataSource 이해 </b>

-   DriverManager 를 통해 커넥션을 획득하다가 커넥션 풀로 변경시 애플리케이션 코드를 변경해야함 (의존관계가 변경되기 때문ㅇ)
-   커넥션풀 -> 커넥션 풀로 변경시에도 같은 문제가 발생 (HikariCP -> commons-dbcp2)
-   이런 문제 때문에 커넥션을 획득하는 방법을 추상화한 DataSource 인터페이스 등장

![image](https://user-images.githubusercontent.com/96561194/209255798-e5c69a4a-41ba-4396-b16e-3e7ca22c2fcd.png)

**DriverManager 사용**

```
    @Test
    void driverManager() throws SQLException {
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }

```

<b>DataSource 사용</b>

```
void dataSourceDriverManager() throws SQLException {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(URL,USERNAME, PASSWORD);
    useDataSource(dataSource);
}
private void useDataSource(DataSource dataSource) throws SQLException {
    Connection con1 = dataSource.getConnection();
    Connection con2 = dataSource.getConnection();
    log.info("connection={}, class={}", con1, con1.getClass());
    log.info("connection={}, class={}", con2, con2.getClass());
}
```

-   DriverManager은 커넥션을 획득할 때마다 URL, USERNAME, PASSWORD 같은 파라미터를 계속 전달해야함
-   DataSource는 객체를생성할 때만 파라미터를 넘겨두고, 커넥션을 획득할 때는 단순히 dataSource.getConnection()만 호출

</br>

**설정과 사용의 분리**

-   설정
    -   DataSource 를 만들고 필요한 속성들을 사용해서 URL, USERNAME, PASSWORD 같은 부분을 입력하는 것
    -   설정과 관련된 속성들은 한 곳에 있는 것이 향후 변경에 유연하게 대처 가능
-   사용
    -   설정은 신경쓰지않고, DataSource의 getConnection() 만 호출해서 사용하면 됨
-   설정과 사용을 분리해야하는 이유
    -   Repository 는 DataSource만 의존하고 속성을 몰라도 됨
    -   애플리케이션을 개발해보면 설정은 한 곳에서 하지만, 사용은 수 많은 곳에서 사용하게됨
    -   객체를 설정하는 부분과, 사용하는 부분을 명확히 분리할 수 있음

**DataSource사용예제**

```
    @Slf4j
    public class MemberRepositoryV1 {
        private final DataSource dataSource;

        public MemberRepositoryV1(DataSource dataSource) {
            this.dataSource = dataSource;
        }
        //save()...
        //findById()...
        //update()....
        //delete()....
        private void close(Connection con, Statement stmt, ResultSet rs) {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(stmt);
            JdbcUtils.closeConnection(con);
        }
        private Connection getConnection() throws SQLException {
            Connection con = dataSource.getConnection();
            log.info("get connection={}, class={}", con, con.getClass());
            return con;
        }
    }
```

-   DataSource 의존관계 주입
    -   외부에서 DataSource를 주입받아서 사용하기 때문에 직접만든 DBConnectionUtil을 사용하지 않아도 된다
    -   DataSource는 표준 인터페이스기 때문에 DriverManagerSouce->HikariDataSouce로 변경되어도 코드 변경 X
-   JdbcUtils 편의 메소드
    -   스프링은 JDBC를 편리하게 다룰 수 있는 JdbcUtils 라는 편의 메소드를 제공함
    -   JdbcUtils 사용하면 커넥션을 좀 더 편리하게 닫을 수 있다

</br>

**DriverManagerDataSource사용로그**

```
get connection=conn0: url=jdbc:h2:.. user=SA class=class
org.h2.jdbc.JdbcConnection
get connection=conn1: url=jdbc:h2:.. user=SA class=class
org.h2.jdbc.JdbcConnection
get connection=conn2: url=jdbc:h2:.. user=SA class=class
org.h2.jdbc.JdbcConnection
get connection=conn3: url=jdbc:h2:.. user=SA class=class
org.h2.jdbc.JdbcConnection
get connection=conn4: url=jdbc:h2:.. user=SA class=class
org.h2.jdbc.JdbcConnection
get connection=conn5: url=jdbc:h2:.. user=SA class=class
org.h2.jdbc.JdbcConnection
```

-   conn0~5 까지 항상 새로운 커넥션이 생성되어서 사용되는 것을 확인할 수 있음

**HikariDataSource사용로그**

```
get connection=HikariProxyConnection@xxxxxxxx1 wrapping conn0: url=jdbc:h2:...
user=SA
get connection=HikariProxyConnection@xxxxxxxx2 wrapping conn0: url=jdbc:h2:...
user=SA
get connection=HikariProxyConnection@xxxxxxxx3 wrapping conn0: url=jdbc:h2:...
user=SA
get connection=HikariProxyConnection@xxxxxxxx4 wrapping conn0: url=jdbc:h2:...
user=SA
get connection=HikariProxyConnection@xxxxxxxx5 wrapping conn0: url=jdbc:h2:...
user=SA
get connection=HikariProxyConnection@xxxxxxxx6 wrapping conn0: url=jdbc:h2:...
user=SA
```

-   커넥션 풀 사용시 conn0이 재사용되는 것을 확인할 수 있다
-   테스트가 순서대로 실행되기 때문에 conn0을 사용하고 돌려주고 하는것을 반복함
-   동시에 여러요청이 들어오면 커넥션 풀을 다양하게 가져감

**DI**

-   DriverManagerDataSource -> HikariDataSource 로 변경해도 MemberRepositoryV1 코드는 전혀 변경하지 않아도된다
-   MemberRepositoryV1 는 DataSource 인터페이스에만 의존하기 때문
-   DI+OCP를 준수한다
