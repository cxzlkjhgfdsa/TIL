# 2022_12_16

</br>

## 의존관계 자동 주입

</br>

### 의존관계 자동 주입 방법

</br>

<b>생성자 주입</b>

-   생성자를 통해서 의존관계를 주입하는 방법
-   생성자 호출 시점에서 단 1번만 호출되는것이 보장된다
-   불편, 필수 의존관계에 사용된다
-   생성자가 단 1개만 존재한다면 @Autowired를 생략해도 된다

<details>
    <summary>&nbsp; 생성자 주입 예시</summary>
    
    @Component
    public class OrderServiceImpl implements OrderService {
        private final MemberRepository memberRepository;
        private final DiscountPolicy discountPolicy;
        @Autowired
        public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
            this.memberRepository = memberRepository;
            this.discountPolicy = discountPolicy;
        }
    }

</details>

</br>

<b>수정자 주입</b>

-   setter라 불리는 필드의 값을 변경하는 수정자 메서드를 통한 의존관계 주입방법
-   선택, 변경 가능성이 있는 의존관계에 사용

<details>
    <summary>&nbsp; 수정자 주입 예시</summary>
    
    @Component
    public class OrderServiceImpl implements OrderService {
        private MemberRepository memberRepository;
         private DiscountPolicy discountPolicy;
        @Autowired
        public void setMemberRepository(MemberRepository memberRepository) {
            this.memberRepository = memberRepository;
        }
         @Autowired
        public void setDiscountPolicy(DiscountPolicy discountPolicy) {
            this.discountPolicy = discountPolicy;
        }
    }

</details>

</br>

<b>필드 주입</b>

-   코드가 간결하다
-   외부에서 변경이 불가능해 테스트하기 힘들다는 치명적인 단점 존재
-   DI 프레임워크가 없다면 아무것도 할 수 없다
-   사용하지 않는 것을 권장함
    -   애플리케이션의 실제 코드와 관계없는 테스트 코드에서는 사용 가능
    -   스프링 설정을 목적으로하는 @Configuration 같은 곳에서만 특별한 용도로 사용

</br>

<b>일반 메서드 주입</b>

-   일반 메소드를 통해 의존관계 주입
-   한번에 여러 필드를 주입받을 수 있다
-   잘 사용하지 않는다

</br>

### 옵션처리

기본적으로 주입할 스프링 빈이 없다면 @Autowired 가 동작하지 않음
허나 주입할 스프링 빈이 없더라고 동작해야하는 경우가 존재함
그래서 다음과 같은 방법들이 존재한다

1.  @Autowired(required=false) -> 자동 주입할 대상이 없으면 수정자 메서드 자체가 호출안됨
2.  @Nullable -> 자동 주입할 대상이 없다면 null 처리함
3.  Optional<> -> 자동 주입할 대상이 없다면 Optional.empty가 입력된다

<details>
    <summary>&nbsp; 옵션처리 테스트</summary>
    
    //호출 안됨
    @Autowired(required = false)
    public void setNoBean1(Member member) {
        System.out.println("setNoBean1 = " + member);
    }
    //null 호출
    @Autowired
    public void setNoBean2(@Nullable Member member) {
        System.out.println("setNoBean2 = " + member);
    }
    //Optional.empty 호출
    @Autowired(required = false)
    public void setNoBean3(Optional<Member> member) {
        System.out.println("setNoBean3 = " + member);
    }

</details>

<b>출력 결과</b>

```
setNoBean2 = null
setNoBean3 = Optional.empty
```
