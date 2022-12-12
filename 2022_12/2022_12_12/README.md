# 2022_12_12

</br>

## 추가적인 요구 사항

<hr>

### 주문과 할인정책 (변경)

-   이전 할인 정책 : 모든 VIP 는 고정적으로 1000원 할인해준다
-   변경할 할인 정책 : 모든 VIP는 구매금액에 10% 할인해준다

</br>

### 새로운 할인 정책 적용 방법

-   RateDiscountPolicy 라는 클래스를 하나 생성한 후 다음과 같이 기존 코드를 변경

```
// private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
 private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
```

</br>

### 문제점

-   역할과 구현을 분리했는가? -> O
-   다형성을 활용하고, 인터페이스와 객체를 분리했는가? -> O
-   OCP, DIP 같은 객체지향 설계 원칙을 준수했는가? -> X
    -   DIP 원칙에 따르면 추상(인터페이스) 에만 의존하고 있어야함
    -   하지만 현재코드를 들여다보면 구현(객체)에도 의존하고있음
    ```
    private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
    private final MemberRepository memberRepository = new MemoryMemberRepository();
    ```
    -   또한 OCP 원칙에 따르면 변경하지 않고 확장할 수 있어야하는데 클라이언트 코드에서 변경이 발생함
    -   따라서 OCP 도 위반하고있다

</br>

### 클라이언트 코드를 변경해야하는 이유

-   기대했던 의존관계 </br>
    ![image](https://user-images.githubusercontent.com/96561194/207148137-d2c91d62-ba25-478b-b341-bedbea57c4d7.png)

-   실제 의존관계 </br>
    ![image](https://user-images.githubusercontent.com/96561194/207148249-a026745a-6eab-48c7-b1ac-009485c89ace.png)

</br></br>

### 해결 방안

-   클라이언트인 OrderServiceImpl 과 MemberServiceImpl 에 구현객체를 대신 생성해주고 주입하는 역할이 필요
-   애플리케이션의 전체 동작 방식을 구성하기 이해, 구현객체를 생성하고 연결을 책임지는 별도의 클래스 작성 - AppConfig

    ```
    public class AppConfig {
        public MemberService memberService() {
            return new MemberServiceImpl(new MemoryMemberRepository());
        }

        public OrderService orderService() {
            return new OrderServiceImpl(
        new MemoryMemberRepository(),
        new FixDiscountPolicy());
        }
    }
    ```

-   클라이언트에서는 생성자를 통해 주입

```
    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
```

-   MemberServiceImpl, OrderServiceImpl 의 생성자를 통해 어떤 구현 객체를 주입할지는 오직 외부 (AppConfig) 에서 결정됨
-   따라서 클라이언트 코드들은 의존관계에 대한 고민은 외부에 맡기고 오직 실행에만 집중하면 된다

### AppConfig 수정

-   현재 AppConfig 코드는 역할이 정확이 무엇인지 알 수 없음
-   따라서 역할에 따른 구현이 보이도록 리팩토링 해야한다

```
public class AppConfig {
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }
    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(),discountPolicy());
    }
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
    public DiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }
}
```

</br>

### 정리

-   현재까지의 코드는 OCP, DIP를 지키지 못한다는 문제점이 있었음
-   AppConfig를 통해 의존관계를 외부에서 컨트롤 함으로써 이런 문제점을 해결
-   기능을 확장하고 변경할 때는 오직 AppConfig 만 변경하면 됨 (클라이언트 코드 변경 필요 X)

</br>

### 현재까지의 프로젝트에서의 SOLID 원칙

1. SRP 단일 책임 원칙
    - 클라이언트 객체는 실행만을 담당
    - 구현객체를 생성하고 연결하는 것은 AppConfig 가 담당
    - 한 클래스가 하나의 책임을 갖고있기 때문에 SRP 원칙 준수
2. DIP 의존관계 역전 원칙
    - 클라이언트 객체들은 오직 추상화(인터페이스)에만 의존함
    - AppConfig가 외부에서 의존관계를 주입
    - DIP 원칙도 준수
3. OCP 확장 개방 변경 폐쇄 원칙
    - 할인 개발 정책을 바꿀 때 AppConfig 에서만 변경이 일어나고 클라이언트 코드는 변경되지 않음
    - OCP 원칙도 준수

</br>

### IoC, DI

-   제어의 역전 (Inversion of Control)

    -   현재 프로젝트에 대한 제어의 흐름은 모두 AppConfig가 관리하고있음
    -   이렇듯 프로그램의 제어의 흐름은 직접 제어하는 것이아니라 외부에서 관리하는 것을 제어의 역전 (IoC) 라고 한다

-   의존관계 주입 (Depedency Injection)
    -   정적인 의존관계
        -   현재 클라이언트 객체는 인터페이스에 의존하고있지만 어떤 구현객체가 주입될지는 알 수 없음
    -   동적인 의존관계
        -   어플리케이션 실행 시점에 구현객체를 생성하고 클라이언트에 전달해 의존관계가 연결됨
        -   클라이언트와 서버의 실제 의존관계가 연결되는것을 의존관계주입 (DI) 라고 함
    -   의존관계 주입을 사용하면 클라이언트 코드 변경 없이 호출하는 대상의 타입 인스턴스를 변경할 수 있음

### 스프링으로 전환

-   현재까지는 순수 Java 코드만으로 DI를 구현하였다
-   스프링으로 코드 변경

AppConfig

```
@Configuration
public class AppConfig {
    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }
    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }
    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
    @Bean
    public DiscountPolicy discountPolicy() {
        return new RateDiscountPolicy();
    }
}
```

MemberApp

```
// AppConfig appConfig = new AppConfig();
// MemberService memberService = appConfig.memberService();
 ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
 MemberService memberService = applicationContext.getBean("memberService", MemberService.class);
```

### 변경된 점

-   기존에는 개발자가 AppConfig 를 사용해서 직접 DI를 했지만 이제는 스프링 컨테이너를 통해서 사용
-   스프링 컨테이너는 @Configuration 이 붙은 AppConfig를 설정(구성) 정보로 사용 @Bean 이라 적힌 메소드를 모두 호출하여 반환된 객체를 스프링 컨테이너에 등록
