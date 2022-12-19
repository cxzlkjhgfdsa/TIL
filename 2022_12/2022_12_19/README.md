# 2022_12_16

</br>

## 의존관계 자동 주입-2

</br>

### 생성자 주입

과거에는 수정자 주입과 필드 주입을 많이 사용했지만, 최근에는 스프링을 포함한 DI 프레임워크 대부분이 생성자 주입을 권장한다

<b> 생성자 주입 권장 이유 </b>

-   대부분의 의존관계 주입은 한번 일어나면 애플리케이션 종료 시점까지 의존관계를변경할 일이 없다 , 오히려 변경되면 안된다 (불변)
-   수정자 주입을 사용하면, set 메소드를 public으로 열어두어야 한다
-   불변하게 설계할 수 있다

</br>

<b> final 키워드 </b>

생성자 주입을 사용하면 필드에 final 키워드를 사용할 수 있다. </br>
그렇기 때문에 생성자에서 혹시 값이 설정되지 않는 오류를 컴파일 시점에서 막아준다 </br>
컴파일 오류는 가장 빠르고 좋은 오류기 때문에 유지보수 측면에서 좋다

<details>
    <summary> 컴파일 오류 예시</summary>
    
    @Component
    public class OrderServiceImpl implements OrderService {
        private final MemberRepository memberRepository;
        private final DiscountPolicy discountPolicy;
        @Autowired
        public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
            this.memberRepository = memberRepository;
        }
        //...
    }

</details>

</br>

### 롬북

개발을 시작하면, 대부분이 다 불변이기 때문에 final 키워드를 사용하게 된다 </br>
final 코드를 사용하면 코드의 양이 늘어나기 때문에 다음과 같은 방법이 나왔다

<b> 롬북 </b>

-   롬북 라이브러리가 제공하는 @RequiredArgsConstructor 기능을 사용하면 final 붙은 필드를 모아서 생성자 자동생성

<details>
    <summary> 롬북 결과 코드</summary>
    
    @Component
    @RequiredArgsConstructor
    public class OrderServiceImpl implements OrderService {
        private final MemberRepository memberRepository;
        private final DiscountPolicy discountPolicy;
    }

</details>

</br>

### 조회 빈이 2개 이상일 때

-   @Autowired는 타입으로 조회함
-   타입으로조회하면 선택된 빈이 2개 이상일 때 문제가발생함

<b> 해결 방법 </b>

1.  @Autowired 필드 명 매칭
2.  @Qualifier
3.  @Primary

</br>

<b> 1. 필드 명 매칭 </b>

```
@Autowired
private DiscountPolicy rateDiscountPolicy
```

이 방법은 OCP, DIP 원칙을 지키지 못하기 때문에 권장하지 않음

</br>

<b> 2. @Qualifier </b>

@Qualifier 은 추가 구분자를 붙여주는 방법이다. 빈 이름 변경하는 것 아님

빈 등록

```
@Component
@Qualifier("mainDiscountPolicy")
public class RateDiscountPolicy implements DiscountPolicy(){}
```

생성자 주입

```
@Autowired
public OrderServiceImpl(MemberRepository memberRepository, @Qualifier("mainDiscountPolicy") DiscountPolicy)
```

@Qualifier 는 Qualifier를 찾는 용도로만 사용하는 것이 좋다

</br>

<b>3. Primary </b>

@Primary는 우선순위를 정하는 방법 @Autowired 시 여러개의 빈이 매칭되면 @Primary 가 우선권을 가진다

```
@Component
@Primary
pubic class RateDiscountPolicy implements DiscountPolicy{}

@Compoment
pubic class FixDiscountPolicy implements DiscountPolicy{}
```

이렇게 작성하면 @Primary 가 붙은 RateDiscountPolicy가 우선권을 가지고 주입된다

</br>

### Annotation 직접 생성하기

@Qualifier 사용시 문자열에 오류가 났을 때 컴파일에서 체크가 안된다 </br>
그렇기 때문에 Annotation을 만들어 사용하는 방법이 있다

<details>
    <summary> Annotation 생성 예시</summary> 
    
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    @Qualifier("mainDiscountPolicy")
    public @interface MainDiscountPolicy {

    }

</details>
<details>
    <summary> Annotation 생성자 주입 예시</summary> 
    
    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, @MainDiscountPolicy DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    
</details>

이렇게 하면 컴파일에서 문자 오류를 체크할 수 있다

</br>

### 조회한 빈이 모두 필요할 때

의도적으로 해당 타입의 빈이 모두 필요할 때가 있다</br>
이를 해결하기 위해 List , Map을 사용할 수 있다

<details>
    <summary> 사용 예시 코드 </summary> 
    
    public class AllBeanTest {

    @Test
    void findAllBean(){
        ApplicationContext ac = new AnnotationConfigApplicationContext(AutoAppConfig.class, DiscountService.class);

        DiscountService discountService = ac.getBean(DiscountService.class);
        Member member = new Member(1L, "userA", Grade.VIP);
        int discountPrice = discountService.discount(member, 10000, "fixDiscountPolicy");

        Assertions.assertEquals(discountPrice, 1000);

        int rateDiscountPrice = discountService.discount(member, 20000, "rateDiscountPolicy");

        Assertions.assertEquals(rateDiscountPrice, 2000);

    }

    static class DiscountService{
        private final Map<String, DiscountPolicy> policyMap;
        private final List<DiscountPolicy> policies;

        @Autowired
        public DiscountService(Map<String, DiscountPolicy> policyMap, List<DiscountPolicy> policies) {
            this.policyMap = policyMap;
            this.policies = policies;
            System.out.println("policyMap = " + policyMap);
            System.out.println("policies = " + policies);
        }

        public int discount(Member member, int price, String discountCode) {
            DiscountPolicy discountPolicy = policyMap.get(discountCode);
            return discountPolicy.discount(member, price);
        }
    }

}

</details>

-   Map<String, DiscountPolicy> -> map의 key에 스프링 빈 이름을 넣어주고, 그 값으로 DiscountPolicy 타입으로 조회된 모든 스프링 빈을 담아줌
-   List<DiscountPolicy> -> DiscountPilicy타입으로 조회한 모든 스프링 빈을 담아준다
-   해당하는 타입의 스프링 빈이 없다면, 빈 컬렉션이나 Map을 주입한다

</br>

### 자동, 수동의 올바른 실무 운영 기준

<b> 편리한 자동 기능을 기본으로 사용하자 </b>

-   스프링은 나오고 시간이 갈수록 점점 자동을 선호하고있다
-   @Component 뿐만 아니라 @Controller, @Service, @Repository 처럼 계층에 맞추어 일반적인애플리케이션 로직을 자동으로 스캔할 수 있도록 지원한다
-   결정적으로 자동으로 빈 등록을 해도 OCP, DIP를 지킬 수 있다

<b> 수동 빈 등록을 사용해야 할 때 </b>

애플리케이션은 크게 업무 로직과 기술 로직으로 나눌 수 있다

1.  업무 로직 빈 : 웹을 지원하는 컨트롤러, 핵심 비즈니스 로직이 있는 서비스, 데이터 계층 로직을 처리하는 리포지토리 등이 모두 업무 로직, 추가나 변경 될 수 있다
2.  기술 지원 빈 : 기술적인 문제나 공통 관심사 (AOP) 를 처리할 때 주로 사용한다. 데이터베이스 연결이나, 공통 로그 처리등 업무 로직을 지원하기 위한 기술들

-   업무로직은 가급적 자동 빈 등록을 사용하는 것이 좋음
-   기술 지원 로직은 보통 어플리케이션 전반에 걸쳐서 광범위하게 영향을 미침
-   업무지원 로직은 문제 발생점이 명확하지만 기술지원 로직은 그러지 못함
-   따라서 기술지원 로직은 수동 빈 등록을 통해 명확하게 드러내는 것이 좋음

</br>

또한 비즈니스 로직 중 다형성을 적극 활용할 때 사용해도 좋음

-   DiscountPolicy가 Map에 주입을 받을 때 어떤 빈들이 주입될지 각 빈의 이름은 무엇인지 여러명에서 개발할 때는 알기 어려움
-   따라서 이런 경우 수동 빈 등록을 하거나 자동으로 등록할 경우 특정 패키지에 묶어두는 것이 좋음
