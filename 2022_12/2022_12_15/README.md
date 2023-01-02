# 2022_12_15

</br>

## 컴포넌트 스캔

</br>

### 컴포넌트 스캔과 의존관계 자동주입

-   현재까지는 자바코드의 @Bean 이나 XML의 &lt;bean> 을 통해서 설정 정보에 등록할 빈을 직접 나열했다
-   현재 우리 프로젝트처럼 설정정보가 몇 개 없을 때에는 문제가 없지만 100, 200 .. 등등 설정정보가 많아지면 누락할 수 있는 문제가 발생한다
-   그렇기 때문에 스프링은 설정정보가 없어도 자동으로 스프링 빈을 등록하는 컴포넌트 스캔이라는 기능을 제공한다
-   또한 의존관계도 자동으로 주입시켜주는 @Autowired 라는 기능도 제공한다

</br>

<b> 컴포넌트 스캔 테스트 </b>

1.  AutoAppConfig 클래스를 작성한다
2.  MemoryMemberRepository , RateDiscountPolicy 에 @Component를 추가한다
3.  MemberServiceImpl, OrderServiceImpl 에 @Component 와 @Autowired 를 추가한다
4.  테스트 코드를 통해 자동으로 클래스가 등록되었는지 확인한다

<details>
    <summary>&nbsp; AutoAppConfig</summary>
    
    @Configuration
    @ComponentScan(
        excludeFilters =  @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
    )
    public class AutoAppConfig {
    }

</details>

<details>
    <summary>&nbsp; 추가 코드 </summary>
    
    //1. MemoryMemberRepository
    @Component
    public class MemoryMemberRepository implements MemberRepository {}

    //2. RateDiscountPolicy
    @Component
    public class RateDiscountPolicy implements DiscountPolicy {}

    //3. MemberServiceImpl
    @Component
    public class MemberServiceImpl implements MemberService {
        private final MemberRepository memberRepository;

        @Autowired
        public MemberServiceImpl(MemberRepository memberRepository) {
            this.memberRepository = memberRepository;
        }
    }

    //4. OrderServiceImpl
    @Component
    public class OrderServiceImpl implements OrderService{

        private  final MemberRepository memberRepository;
        private  final DiscountPolicy discountPolicy;

        @Autowired
        public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
            this.memberRepository = memberRepository;
            this.discountPolicy = discountPolicy;
        }
    }

</details>

<details>
    <summary>&nbsp; 테스트코드</summary>
    
    public class AutoAppConfigTest {

        @Test
        void basicScan(){
            AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AutoAppConfig.class);

            MemberService memberService = ac.getBean(MemberService.class);
            Assertions.assertInstanceOf(MemberService.class, memberService);
        }
    }

</details>

-   테스트를 실행시켜보면 정상적으로 동작되는 것을 알 수 있다
-   로그를 자세히 보면 @Component 를 붙여준 클래스들이 모두 잘 등록되어있는 것을 볼 수 있다

### 1. @ComponentScan

-   @ComponentScan은 @Component 가 붙은 모든 클래스를 스프링 빈으로 등록한다
-   이때 스프링 빈의 기본 이름은 클래스명을 사용하되 맨 앞글자만 소문자로 변경한다
    -   빈 이름은 직접 지정할 수 도 있다 -> @Component("memberService2")

</br>

### 2. @Autowired

-   생성자에 @Autowired를 지정하면, 스프링 컨테이너가 자동으로 해당 스프링 빈을 찾아서 주입한다
-   이때 기본적으로 타입이 같은 빈을 찾아 주입한다
    -   getBean(MemberRepository.class)와 유사하다
-   생성자에 파라미터가 많더라도 다 찾아서 주입해준다

</br>

### 탐색 위치와 기본 스캔 대상

-   모든 자바 클래스르 컴포넌트 스캔하면 시간이 오래걸리기 때문에 필요한 위치부터 탐색하도록 지정할 수 있다

```
@ComponentScan(
    basePackage = "hello.core"
)
```

-   basePackage -> 탐색을 시작할 위치를 지정 , 이 패키지를 포함해 하위 패키지를 모두 탐색한다
-   basePackageClasses -> 지정한 클래스의 패키지를 탐색 시작 위치로 지정
-   아무것도 설정하지 않으면 @ComponentScan 이 붙은 설정정보 클래스의 패키지가 시작 위치가 됨
-   통상적으로 설정정보 클래스를 프로젝트 최상단에 놓고 basePackage를 지정하지 않는다

</br>

### 컴포넌트 스캔 기본 대상

컴포넌트 스캔은 @Component 뿐만 아니라 다음과 같은 내용도 추가로 대상에 포함한다

-   @Component : 컴포넌트 스캔에 사용
-   @Contoroller : 스프링 MVC 컨트롤러에 사용
-   @Service : 스프링 비즈니스 로직에서 사용
-   @Repository : 스프링 데이터 접근 계층에서 사용
-   @Configuration : 스프링 설정 정보에서 사용

</br>

### 필터

-   includeFilter -> 컴포넌트 스캔 대상을 추가로 지정
-   excludeFilter -> 컴포넌트 스캔에서 제외할 대상을 지정

</br>

### 중복 등록과 충돌

스프링 빈 등록방식에는 자동 빈 등록과 수동 빈 등록이 있다
컴포넌트 스캔에서 같은 빈 이름을 등록하면 어떻게 되는가?

1. 자동 빈 등록 vs 자동 빈 등록
    - 컴포넌트 스캔에 의해 자동으로 스프링 빈이 등록되는데 그 이름이 같은 경우 예외가 발생한다
2. 자동 빈 등록 vs 수동 빈 등록
    - 수동 빈 등록이 우선권을 가지면 overriding 해버린다
    - 이경우 치명적인 문제가 발생할 수 있기 때문에 최근 스프링 부트에서는 오류가 발생하도록 바뀌었다

</br>
<b>인프런 김영한님 강의 참고</b>s
