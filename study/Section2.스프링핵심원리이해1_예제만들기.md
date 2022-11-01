# Section 2. 스프링 핵심 원리 이해1 - 예제 만들기

## 프로젝트 생성

- Java 11
- IntelliJ

- Gradle
- Spring Boot 2.3.x
- groupId: hello
- artifactId: core
- Dependencies: 선택하지 않는다.



### Gradle 전체 설정

- build.gradle

  ```
  plugins {
  	id 'org.springframework.boot' version '2.7.5'
  	id 'io.spring.dependency-management' version '1.0.15.RELEASE'
  	id 'java'
  }
  
  group = 'hello'
  version = '0.0.1-SNAPSHOT'
  sourceCompatibility = '11'
  
  repositories {
  	mavenCentral()
  }
  
  dependencies {
  	implementation 'org.springframework.boot:spring-boot-starter'
  	testImplementation 'org.springframework.boot:spring-boot-starter-test'
  }
  
  tasks.named('test') {
  	useJUnitPlatform()
  }
  
  ```



### Intellij Gradle 대신 자바 직접 실행

최근 IntelliJ 버전은 Gradle을 통해서 실행 하는 것이 기본 설정이다. 이렇게 하면 실행속도가 느리다. 다음과 같이 변경하면 자바로 바로 실행해서 실행속도가 더 빠르다.

- Preferences -> Build, Exectuion, Deployment -> Build Tools -> Gradle
  - Build and run using: Gradle -> Intellij IDEA
  - Runt tests using: Gradle -> Intellij IDEA





## 비즈니스 요구사항과 설계

- 회원
  - 회원을 가입하고 조회할 수 있다.
  - 회원은 일반과 VIP 두 가지 등급이 있다.
  - 회원 데이터는 자체 DB를 구축할 수 있고, 외부 시스템과 연동할 수 있다. (미확정) => 인터페이스로 구현하면 그만
- 주문과 할인 정책
  - 회원은 상품을 주문할 수 있다.
  - 회원 등급에 따라 할인 정책을 적용할 수 있다.
  - 할인 정책은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용해달라. (나중에 변경될 수 있다.)
  - 할인 정책은 변경 가능성이 높다. 회사의 기본 할인 정책을 아직 정하지 못했고, 오픈 직전까지 고민을 미루고 싶다. 최악의 경우 할인을 적용하지 않을 수도 있다. (미확정)



요구사항을 보면 회원 데이터, 할인 정책 같은 부분은 지금 결정하기 어려운 부분이다. 그렇다고 이런 정책이 결정될 때까지 개발을 무기한 기다릴 수도 없다. 따라서 인터페이스를 만들고 구현체를 언제든지 갈아끼울 수 있도록 설계하면 된다. => 객체 지향 설계 방법 

> 참고: 프로젝트 환경설정을 편리하게 하려고 스프링 부트를 사용한 것이다. 지금은 스프링 없는 순수한 자바로만 개발을 진행한다. 스프링 관련은 한참 뒤에 등장한다.



## 회원 도메인 설계

- 회원 도메인 요구사항
  - 회원을 가입하고 조회할 수 있다.
  - 회원은 일반과 VIP 두 가지 등급이 있다.
  - 회원 데이터는 자체 DB를 구축할 수 있고, 외부 시스템과 연동할 수 있다. (미확정)

- 회원 도메인 협력 관계

  ![2-1](./img/2-1.png)

- 회원 클래스 다이어그램

  ![2-2](./img/2-2.png)

- 회원 객체 다이어그램

  ![2-2](./img/2-3.png)

  - 회원 서비스: `MemberServiceImpl`

















