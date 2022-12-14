# 2022_12_14

</br>

## 싱글톤 컨테이너

</br>

### 웹 애플리케이션과 싱글톤

-   스프링은 대부분 웹 어플리케이션 개발을 위해 사용된다
-   웹 애플리케이션은 보통 여러 고객이 동시에 요청을 한다
-   현재까지 우리가 만들었던 순수한 DI 컨테이너인 AppConfig는 요청을 할 때 마다 객체를 새로 생성한다 ->메모리 낭비 심함
-   이에 해당 객체가 딱 1개만 생성되고 공유할 수 있게 만드는 패턴이 등장했다 -> 싱글톤 패턴

<details>
    <summary>&nbsp; 새로운 객체 생성 실험 코드 </summary>
    
    
    public class SingletonTest {
        @Test
        @DisplayName("스프링 없는 순수한 DI 컨테이너")
        void pureContainer() {
            AppConfig appConfig = new AppConfig();
            //1. 조회: 호출할 때 마다 객체를 생성
            MemberService memberService1 = appConfig.memberService();
            //2. 조회: 호출할 때 마다 객체를 생성
            MemberService memberService2 = appConfig.memberService();
            //참조값이 다른 것을 확인
            System.out.println("memberService1 = " + memberService1);
            System.out.println("memberService2 = " + memberService2);
            //memberService1 != memberService2
            assertThat(memberService1).isNotSameAs(memberService2);
        }
    }

</details>

</br>

### 싱글톤 패턴

-   클래스의 인스턴스가 딱 1개만 생성되는 것을 보장하는 디자인 패턴
-   객체 인스턴스가 2개 이상 생성되지 못하게 막아야함
    -   private 생성자를 통해 외부에서 임의로 new 키워드를 사용하지 못하게 막아야함
    -   오직 getInstance() 메서드를 통해서만 객체를 조회할 수 있고, 이 메소드 호출시 항상 같은 객체를 반환한다

<details>
    <summary>&nbsp; 싱글톤 패턴 예제 코드</summary>
    
    
    public class SingletonService {
        //1. static 영역에 객체를 딱 1개만 생성해둔다.
        private static final SingletonService instance = new SingletonService();
        //2. public으로 열어서 객체 인스터스가 필요하면 이 static 메서드를 통해서만 조회하도록 허용한다.
        public static SingletonService getInstance() {
            return instance;
        }  
        //3. 생성자를 private으로 선언해서 외부에서 new 키워드를 사용한 객체 생성을 못하게 막는다.
        private SingletonService() {
        }
        public void logic() {
            System.out.println("싱글톤 객체 로직 호출");
        }
    }

</details>

<details>
    <summary>&nbsp; 싱글톤 패턴 적용 테스트 코드</summary>
    
    
    @Test
    @DisplayName("싱글톤 패턴을 적용한 객체 사용")
    void singletonServiceTest(){
        SingletonService singletonService1 = SingletonService.getInstance();
        SingletonService singletonService2 = SingletonService.getInstance();

        //두 객체가 같은지 확인
        System.out.println(singletonService1);
        System.out.println(singletonService2);

        assertSame(singletonService1, singletonService2);
    }

</details>

-   호출할 때 마다 같은 객체가 반환되는 것을 알 수 있다

</br>

<b> 싱글톤 패턴의 문제점 </b>

1.  싱글톤 패턴을 구현하는 코드 자체가 길다
2.  의존관계상 클라리언트가 구체 클래스에 의존하게 된다 -> DIP 위반
3.  클라이언트가 구체 클래스에 의존해서 OCP 원칙을 위반하게 될 가능성이 높다
4.  내부 속성을 변경하거나 초기화 하기 어렵다
5.  private 생성자로 자식 클래스를 만들기 어렵다
6.  테스트하기 어렵다

</br>

<b> 이러한 문제점들을 해결하기 위해서 싱글톤 컨테이너가 등장한다 </b>

</br>

### 싱글톤 컨테이너

-   스프링 컨테이너는 싱글톤 패턴을 적용하지 않아도, 객체 인스턴스를 싱글톤으로 관리한다
-   스프링 컨테이너는 싱글톤 컨테이너 역할을 하며 이렇게 싱글톤 객체를 생성하고 관리하는 기능을 싱글톤 레지스트리라 한다
-   스프링 컨테이너의 이런 기능들 덕에 싱글톤 패턴의 단점을 해결하면서 객체를 싱글톤으로 유지할 수 있다
    -   싱글톤을 위한 긴 길이의 코드 필요 X
    -   DIP, OCP, 테스트, private 생성자로부터 자유롭게 싱글톤을 사용할 수 있음

<details>
    <summary>&nbsp; 스프링 컨테이너를 통한 싱글톤 패턴 적용 테스트 코드</summary>

    @Test
    @DisplayName("스프링 컨테이너와 싱글톤")
    void SpringContainer(){
        //AppConfig appConfig = new AppConfig();

        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        //1. 조회 : 호출할 때 마다 객체를 생성
        MemberService memberService1 = ac.getBean("memberService", MemberService.class);

        //2. 조회 : 호출할 때 마닥 객체를 생성
        MemberService memberService2 = ac.getBean("memberService", MemberService.class);

        //참조값이 다른것을 확인
        System.out.println("1 = " + memberService1);
        System.out.println("2 = " + memberService2);

        //memberService1 != memberService2
        Assertions.assertSame(memberService2, memberService1);
    }

</details>

</br>

### 싱글톤 방식의 주의점

-   싱글톤 방식은 객체를 하나만 생성해 공유하기 때문에 상태를 유지(stateful)하게 설계하면 안된다
-   꼭 무상태(stateless)로 설계해야 한다
    -   특정 클라이언트에 의존적인 필트 X
    -   특정 클라이언트가 값을 변경할 수 있는 필드 존재 X
    -   가급적 읽기만 가능해야함
    -   필드 대신 공유되지 않는 지역변수 , 파라미터 등을 이용해야함
-   만약 이 원칙을 지키지 않는다면 정말 큰 문제가 발생할 수 있다
    </br>

<details>
    <summary>&nbsp; 상태를 유지할 경우 문제점 코드 </summary>

    public class StatefulService {
        private int price; //상태를 유지하는 필드
         public void order(String name, int price) {
            System.out.println("name = " + name + " price = " + price);
            this.price = price; //여기가 문제!
        }
        public int getPrice() {
            return price;
        }
    }

</details>

<details>
    <summary>&nbsp; 상태를 유지할 경우 문제점 테스트 코드 </summary>

    class StatefulServiceTest {

    @Test
    void statefulServiceSingleton(){
        ApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);
        StatefulService statefulService1 = ac.getBean(StatefulService.class);
        StatefulService statefulService2 = ac.getBean(StatefulService.class);

        //ThreadA : A사용자 10000원 주문
        int userAPrice = statefulService1.order("userA", 10000);
        //ThreadB : B사용자 20000원 주문
        int userBPrice = statefulService2.order("userB", 20000);

        //int price = statefulService1.getPrice();
        System.out.println("price = "+userAPrice);

        assertEquals(userAPrice , 20000);
    }

    static class TestConfig{

        @Bean
        public StatefulService statefulService(){
            return new StatefulService();
        }

    }

}

</details>

-   사용자 A 의 주문금액은 10000원이 나와야 하는데 위 코드에서는 20000이라는 결과가 나온다
-   이렇듯 공유필드는 정말 조심해야한다

</br>

### @Configuration과 싱글톤

-   그런데 AppConfig 클래스를 다시 들여다보면 이런 의문이 생긴다
-   memberService 빈을 만드는 코드도 new MemoryMemberRepository를 호출
-   orderService 빈을 만드는 코드도 new MemoryMemberRepository를 호출
-   이렇게되면 결국 2개의 MemoryMemberRepository가 생성되면서 싱글톤이 깨지는 것이 아닌가?
-   실제로 그런지 실험해보자

<details>
    <summary>&nbsp; 검증 용도의 코드 추가 </summary>

    //MemberServiceImpl
    public class MemberServiceImpl implements MemberService {
        private final MemberRepository memberRepository;
        //테스트 용도
        public MemberRepository getMemberRepository() {
            return memberRepository;
        }
    }
    //OrderServiceImpl
    public class OrderServiceImpl implements OrderService {
        private final MemberRepository memberRepository;
        //테스트 용도
        public MemberRepository getMemberRepository() {
            return memberRepository;
        }
    }

</details>

<details>
    <summary>&nbsp; 테스트코드 </summary>

    @Test
        void configurationTest(){
            ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

            MemberServiceImpl memberService = ac.getBean("memberService", MemberServiceImpl.class);
            OrderServiceImpl orderService = ac.getBean("orderService", OrderServiceImpl.class);
            MemberRepository memberRepository = ac.getBean("memberRepository", MemberRepository.class);

            MemberRepository memberRepository1 = memberService.getMemberRepository();
            MemberRepository memberRepository2 = orderService.getMemberRepository();

            System.out.println("memberService -> memberRepository = " + memberRepository1);
            System.out.println("orderService -> memberRepository = " + memberRepository2);
            System.out.println("memberRepository =" + memberRepository);

            Assertions.assertEquals(memberRepository1, memberRepository2);
            Assertions.assertEquals(memberRepository2, memberRepository);
        }

</details>

<b> 문제점 발생 </b>

-   스프링 버전이 달라져서 인지 다른 객체들이 출력됐다
-   따라서 autowired를 통해 의존성을 주입해주어 같은 객체들을 반환하도록 변경함

<details>
    <summary>&nbsp; AppCofing 변경 </summary>

    @Configuration
    public class AppConfig {

        @Autowired
        MemberRepository memberRepository;

        @Bean
        public MemberService memberService(){
            System.out.println("call AppConfig.memberService");
            return new MemberServiceImpl(memberRepository);
        }
        @Bean
        public static MemberRepository memberRepository() {
            System.out.println("call AppConfig.getMemberRepository");
            return new MemoryMemberRepository();
        }
        @Bean
        public OrderService orderService(){
            System.out.println("call AppConfig.orderService");
            return new OrderServiceImpl(memberRepository, getDiscountPolicy());
        }
        @Bean
        private static DiscountPolicy getDiscountPolicy() {
            return new RateDiscountPolicy();
        }

    }

</details>

-   AppConfig의 호출코드들도 모두 각각 1번씩만 호출된다
-   모두 같은 객체를 공유하고 있는 것을 확인할 수 있다
-   이렇게 될 수 있는 이유는 모두 @Configuration 덕분이다
-   또한 @Configuration 없이 @Bean만 사용해도 스프링 빈으로 등록은 되나 싱글톤은 보장되지 않는다
