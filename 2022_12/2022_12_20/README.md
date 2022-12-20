# 2022_12_20

</br>

## 빈 생명주기 콜백

</br>

### <b> 빈 생명주기 콜백 시작 </b>

-   스프링 빈은 간단하게 다음과 같은 라이프사이클을 가진다
-   객체 생성 -> 의존관계 주입
-   스프링은 의존관계 주입이 완료되면 스프링 빈에게 콜백메소드를 통해 초기화 시점을 알려주는 다양한 기능 제공
-   스프링은 컨테이너가 종료되기 직전에 소멸 콜백도 제공
-   <b> 스프링 빈의 이벤트 라이프 사이클</b>
    -   스프링 컨테이너 생성 -> 스프링 빈 생성 -> 의존관계 주입 -> 초기화 콜백 -> 사용 -> 소멸전 콜백 -> 스프링 종료
    -   초기화 콜백 : 빈이 생성되고, 빈의 의존관계 주입이 완료된 후 호출
    -   소멸전 콜백 : 빈이 소멸되기 직전에 호출

</br>

<b>참고: 객체의 생성과 초기화 분리 </b>

-   생성자는 필수정보(파라미터)를 받고 메모리를 할당해서 객체를 생성함
-   초기화는 생성된 값을 활용해서 외부 커넥션을 연결하는 무거운 동작을 수행함
-   결론적으로 생성자와 초기화를 분리하는 것이 유지보수 관점에서 좋음

### <b> 빈 생명주기 콜백 사용법 </b>

1.  인터페이스 InitializingBean, DisposableBean
    -   InitializingBean, DisposableBean 인터페이스를 상속받아 사용하는 것
    -   스프링 전용 인터페이스

<b>단점</b>

-   스프링 전용 인터페이스에 의존하게됨
-   초기화, 소멸 메소드의 이름 변경 불가능
-   외부 라이브러리에 적용 불가능

</br>

2. 빈등록 초기화, 소멸 메소드 지정
    - 설정 정보에 @Bean(initMethod="init", destroyMethod="close") 처럼 초기화, 소멸 메소드를 지정

<b>특징</b>

-   메서드 이름을 자유롭게 할 수 있음
-   스프링 코드에 의존하지 않음
-   외부라이브러리 적용 가능
-   destroyMethod 를 default 값으로 할 시 종료메소드를 추론해서 적용해줌

</br>

3.  Annotation @PostConstruct, @PreDestroy
    -   어노테이션으로 초기화, 소멸 메소드 지정

<b>특징</b>

-   최신 스프링에서 권장하는 방법
-   매우 편리함
-   자바 표준 기술이라 스프링에 의존하지 않음
-   컴포넌트 스캔과 잘 어올림
-   외부 라이브러리에 적용 불가능

</br>

<b>결론</b>

-   어노테이션 방식을 사용하자
-   외부 라이브러리에 초기화, 종료메소드를 적용해야 할때만 빈 설정 방식을 사용

<br>

## 빈 스코프

</br>

### 빈스코프란?

빈이 존재할 수 있는 범위를 뜻함

<b> 스프링 스코프 </b>

1.  싱글톤 : 기본스코프, 스프링 컨테이너 시작과 종료까지 유지되는 가장 넓은 범위의 스코프
2.  프로토타입 : 빈의 생성과 의존관계 주입까지만 관리하는 매우 짧은 범위의스코프
3.  웹 관련 스코프
    -   request : 웹 요청이 들어오고 나갈때까지 유지
    -   session : 웹 세션이 생성되고 종료될 때 까지 유지
    -   application : 웹의 서블릿 컨텍스트와 같은 범위로 유지

</br>

### <b>프로토 타입 스코프 </b>

-   싱글톤과 다르게 스프링 컨테이너 조회시 항상 새로운 인스턴스를 생성해서 반환
-   클라이언트에 빈을 반환한 후에 관리하지않아 종료 메소드 호출 불가능
-   클라이언트 측에서 관리를 담당하게됨

</br>

### <b>프로토 타입 스코프 - 싱글톤 스코프 동시 사용 문제점 </b>

프로토타입은 스프링 빈 조회시 학상 새로운 인스턴스를 반환해야하는데 싱글톤 스코프와 함께 사용시 의도대로 동작하지 않을 떄가 있다

<b>예제 코드</b>

<details>
    <summary> 싱글톤-프로토타입 동시 사용 예제</summary>
    
    public class SingletonWithPrototypeTest1 {
    @Test
    void singletonClientUsePrototype() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(ClientBean.class, PrototypeBean.class);
        ClientBean clientBean1 = ac.getBean(ClientBean.class);
        int count1 = clientBean1.logic();
        assertThat(count1).isEqualTo(1);
        ClientBean clientBean2 = ac.getBean(ClientBean.class);
        int count2 = clientBean2.logic();
        assertThat(count2).isEqualTo(2);
        }

    static class ClientBean {
        private final PrototypeBean prototypeBean;

        @Autowired
        public ClientBean(PrototypeBean prototypeBean) {
            this.prototypeBean = prototypeBean;
        }

        public int logic() {
            prototypeBean.addCount();
            int count = prototypeBean.getCount();
            return count;
        }
    }

    @Scope("prototype")
    static class PrototypeBean {
        private int count = 0;
        public void addCount() {
        count++;
        }
        public int getCount() {
            return count;
        }
        @PostConstruct
        public void init() {
            System.out.println("PrototypeBean.init " + this);
        }
        @PreDestroy
        public void destroy() {
            System.out.println("PrototypeBean.destroy");
        }
    }

}

</details>

-   스프링은 일반적으로 싱글톤 빈을 사용하므로, 싱글톤 빈이 프로토타입 빈을 사용하게됨
-   싱글톤빈은 생성 시점에 의존관계를 주입하기 때문에 프로토타입빈이 새로 생성되는 것이 아니라 같은 것을 반환함

</b>

### <b>프로토타입, 싱글톤 동시사용 해결책 </b>

1.  <b>스프링 컨테이너에 요청 </b>

    -   싱글톤 빈이 프로토타입을 사용할 때 마다 스프링 컨테이너에 새로 요청
    -   스프링에 종속적인 코드가 되고 단위 테스트가 어려워짐

2.  <b>ObjectProvider</b>
    -   직접 필요한 의존관계를 찾아주는 것을 DL(Dependency Lookup) 이라고함
    -   getObject() 호출 시 내부에서 스프링 컨테이너를 통해 해당 빈을 찾아 반환(DL)
    -   스프링에 종속적이게 되지만, 기능이 단순하기 때문에 단위테스트 , mock코드 생성 쉬워짐

<details>
    <summary> ObjectProvider 예제</summary>
    
    @Autowired
    private ObjectProvider<PrototypeBean> prototypeBeanProvider;

    public int logic() {
        PrototypeBean prototypeBean = prototypeBeanProvider.getObject();
        prototypeBean.addCount();
        int count = prototypeBean.getCount();
        return count;
    }

</details>

3.  JSR-330 Provider
    -   JSR 330 이라는 자바 표준을 이용한 방법
    -   ObjectProvider와 기능은 비슷함

</br>

<b>참고 : 실무에서의 선택</b>

-   ObjectProvider가 편의기능은 더 많이제공
-   스프링 외에 다른 컨테이너에도 사용할 것이면 자바 표준을 사용하고
-   그 외에는 ObjectProvider 사용 권장

</br>

### <b> 웹 스코프 </b>

<b>특징</b>

-   웹 환경에서만 동작
-   종료시점가지 관리하기 떄문에 종료메소드 사용 가능

</b>종류</b>

1.  request : HTTP 요청 하나가 들어오고 나갈 때 까지 유지, HTTP 요청마다 별도의 빈 인스턴스 생성
2.  session : HTTP Session과 동일한 생명주기를 가지는 스코프
3.  application : 서블릿 컨텍스트와 동일한 생명주기
4.  websocket : 웹 소켓과 동일한 생명주기

<b> Request 스코프 문제 </b>

-   일반적으로 사용시 객체가 생성되는 시점에 request가 존재하지 않아 오류 생김
-   다음과 같은 해결방법 존재
    -   Provider
    -   Proxy

</br>

<b> 스코프와 Provider </b>

-   ObjectProvider 덕분에 getObject() 호출 시점까지 request scope 빈의 생성을 지연할 수 있음
-   getObject() 를 LogDemoController, LogDemoService 에서 각각 한번씩 따로 요청해도 같은 HTTP 요청이면 같은 스프링 빈이 반환됨

<details>
    <summary> 스코프 ObjectProvider 예제</summary>
    
    @Controller
    @RequiredArgsConstructor
    public class LogDemoController {
        private final LogDemoService logDemoService;
        private final ObjectProvider<MyLogger> myLoggerProvider;

        @RequestMapping("log-demo")
        @ResponseBody
        public String logDemo(HttpServletRequest request) {
            String requestURL = request.getRequestURL().toString();
            MyLogger myLogger = myLoggerProvider.getObject();
            myLogger.setRequestURL(requestURL);
            myLogger.log("controller test");
            logDemoService.logic("testId");
            return "OK";
        }
    }

    @Service
    @RequiredArgsConstructor
        public class LogDemoService {
        private final ObjectProvider<MyLogger> myLoggerProvider;
        public void logic(String id) {
            MyLogger myLogger = myLoggerProvider.getObject();
            myLogger.log("service id = " + id);
        }
    }

</details>

</br>

<b>스코프와 프록시 </b>

```
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyLogger {
}
```

-   인터페이스가 아닌 클래스 -> TARGET_CLASS
-   인터페이스 -> INTERFACES
-   가짜 프록시 클래스를 만들어두고 HTTP Request와 관계없이 가짜 프록시 클래스를 다른 빈에 미리 주입 가능
-   CGLIB 라는 라이브러리를 사용해 내 클래스를 상속받은 가짜 프록시 객체를 만듬
-   의존관계 주입도 이 가짜 프록시 객체가 주입됨
-   가짜 프록시 객체는 내부에 진짜 myLogger을 찾는 방법을 알고있다
-   원본 클래스를 상속받았기 때문에 클라이언트 입장에서는 원본인지 아닌지 모름 (다형성)

</br>
<b>주의점</b>

-   마치 싱글톤을 사용하는 것 같지만 다르게 동작하기 때문에 주의해서 사용
-   이런 특별한 스코프는 꼭 필요한 곳에서 최소화하여 사용, 무분별하게 사용시 유지보수에 문제 생김
