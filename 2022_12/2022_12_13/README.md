# 2022_12_13

</br>

## 스프링 컨테이너와 스프링 빈

</br>

### 스프링 컨테이너 생성

```
ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
```

-   ApplicationContext 는 스프링컨테이너라 한다
-   ApplicationContext 는 인터페이스다
-   스프링컨테이너는 XML 기반으로 만들 수 있고, Annotation 기반의 자바 설정 클래스로 만들 수 있다
-   이전에 AppConfig를 사용했던 방식이 Annotation 기반의 자바 설정 클래스로 스프링 컨테이너를 만든 것
-   AnnotationConfigApplicationContext 는 ApplicationContext 인터페이스의 구현체이다

### 스프링 컨테이너의 생성 과정

1. 스프링 컨테이너 생성
    - 스프링 컨테이너를 생성할 때는 구성정보를 지정해주어야 함
    - new AnnotationConfigApplicationContext(AppConfig.class)
    - 위 코드에서는 AppConfig.class 가 구성정보이다
    - 스프링 컨테이너에 다음과 같은 스프링 빈 저장소가 생성된다
      </br>
      </br>
      ![image](https://user-images.githubusercontent.com/96561194/207427189-39d6482c-509a-41ac-bbd3-765b040b0216.png)

</br>

2. 스프링 빈 등록
    - 스프링 컨테이너는 파라미터로 넘어온 설정 클래스 정보(AppConfig.class)를 사용해서 스프링 빈을 등록한다
    - 이때 빈이름은 메서드이름을 사용하며 직접 부여할 수 도 있다
    - 빈 이름은 항상 중복되지 않게 해주어야 오류가 나지 않는다
      </br>
      </br>
      ![image](https://user-images.githubusercontent.com/96561194/207427701-e3f0ce3f-41e8-4217-9c37-65e62fb7554f.png)

</br>

3. 스프링 빈 의존관계 설정
    - 스프링 컨테이너는 설정 정보를 참고하여 의존관계를 주입한다 (DI)
      </br>
      </br>
      ![image](https://user-images.githubusercontent.com/96561194/207428009-2daf8b26-6722-40d1-bbf4-6b25e9325426.png)
    - 스프링은 빈을 생성하고 의존관계를 주입하는 단계가 나누어져있다
    - 하지만 이렇게 자바 코드로 스프링 빈을 등록하면 생성자를 호출하면서 의존관계 주입도 한번에 처리된다

</br>

## 스프링 컨테이너 빈 조회하기

</br>

### 컨테이너에 등록된 모든 빈 조회하기

-   모든 빈 출력하기
    -   실행하면 스프링에 등록된 모든 빈 정보를 출력할 수 있음
    -   ac.getBeanDefinitionNames() -> 스프링에 등록된 모든 빈 이름을 조회
    -   ac.getBean() -> 빈 이름으로 객체(인스턴스)를 조회한다
-   애플리케이션 빈 조회
    -   스프링내부에서 사용하는 빈은 getRole() 로 구분가능
    -   사용자 정의 빈 -> ROLE_APPLICATION , 스프링 내부 빈 -> ROLE_INFRASTRUNCTURE

코드 : [모든 빈 조회](./beanfind/ApplicationContextInfoTest.java)

</br>

### 스프링 빈 조회 기본

-   스프링 컨테이너에서 스프링 빈을 찾는 가장 기본적인 방법은 ac.getBean(빈이름, 타입) or ac.getBean(타입)
-   조회대상 스프링 빈이 없다면 Exception이 발생한다
-   구체타입으로도 조회가 가능하나 권장하지 않음 (유연성 떨어짐)

코드 : [기본 빈 조회](./beanfind/ApplicationContextBasicFindTest.java)

### 스프링 빈 조회

-   동일한 타입이 둘 이상일때
    -   타입으로 조회시 같은 타입의 스프링 빈이 둘 이상이면 오류발생 이 때는 빈 이름을 지정해주면됨
    -   ac.getBeansOfType() 을 사용하면 해당 타입의 모든 빈 조회 가능
    -   코드 : [동일한 타입](./beanfind/ApplicationContextSameBeanFindTest.java)
-   상속관계
    -   부모타입으로 조회하면 자식타입도 같이 조회됨
    -   자바객체의 최고 부모인 Object로 조회하면 모든 스프링 빈 조회가능
    -   코드 : [상속 관계](./beanfind/ApplicationContextExtendsFindTest.java)

</br>

### BeanFactory 와 ApplicationContext

-   BeanFactory
    -   스프링 컨테이너 최상위 인터페이스
    -   스프링 빈을 관리하고 조회하는 역할
    -   getBean () 제공
-   ApplicationContext
    -   BeanFactory의 모든 기능을 상속받아서 제공
    -   다음과 같은 부가기능등을 제공  
        </br>
        </br>

![image](https://user-images.githubusercontent.com/96561194/207430508-e62477bd-a602-4f5c-bef1-adafb1b9a946.png)

-   메세지 소스를 활용한 국제화 기능
-   환경변수
-   애플리케이션 이벤트
-   편리한 리소스 조회
    </br>

참고 : 스프링 빈을 XML로 설정하는 방법 -> [XML 설정](./xml/XmlAppContext.java)

### BeanDefinition

-   스프링의 다양한 형태의 설정정보는 BeanDefinition 을 추상화해서 사용한다
-   BeanDefinition을 직접 생성해 스프링 컨테이너에 등록 가능하나 사용할일은 거의 없다
    참고 코드 : [BeanDefinition](./beandefinition/BeanDefinitionTest.java)

</br>
<b>인프런 김영한님 강의 참고</b>
