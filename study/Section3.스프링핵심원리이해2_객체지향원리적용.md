# Section 3. 스프링 핵심 원리 이해2 - 객체 지향 원리 적용

## 새로운 할인 정책 개발

- 악덕 기획자: 서비스 오픈 직전에 할인 정책을 지금처럼 고정 금액 할인이 아니라 좀 더 합리적인 주문 금액당 할인하는 정률% 할인으로 변경하고 싶어요. 예를 들어서 기존 정책은 VIP가 10000원을 주문하든 20000원을 주문하든 항상 1000원을 할인했는데, 이번에 새로 나온 정책은 10%로 지정해두면 고객이 10000원 주문시 1000원을 할인해주고, 20000원 주문시에 2000원을 할인해주는 거에요!
- 순진 개발자: 제가 처음부터 고정 금액 할인은 아니라고 했잖아요.
- 악덕 기획자: 애자일 소프트웨어 개발 선언 몰라요? "계획을 따르기보다 변화에 대응하기를"
- 순진 개발자: ... (하지만 난 유연한 설계가 가능하도록 객체지향 설계 원칙을 준수했지 후후)

> 참고: 애자일 소프트웨어 개발 선언 https://agilemanifesto.org/iso/ko/manifesto.html

이번에는 주문한 금액의 %를 할인해주는 새로운 정률 할인 정책을 추가하자.

- RateDiscountPolicy 추가

  ``` java
  package hello.core.discount;
  
  import hello.core.member.Grade;
  import hello.core.member.Member;
  
  public class RateDiscountPolicy implements DiscountPolicy {
      
      private int discountPercent = 10; // 10% 할인
      
      @Override
      public int discount(Member member, int price) {
          if (member.getGrade() == Grade.VIP) {
              return price * discountPercent / 100;
          } else {
              return 0;
          }
      }
  }
  
  ```

- 테스트 작성

  ``` java
  package hello.core.discount;
  
  import static org.assertj.core.api.Assertions.assertThat;
  
  import hello.core.member.Grade;
  import hello.core.member.Member;
  import org.junit.jupiter.api.DisplayName;
  import org.junit.jupiter.api.Test;
  
  class RateDiscountPolicyTest {
      RateDiscountPolicy discountPolicy = new RateDiscountPolicy();
  
      @Test
      @DisplayName("VIP는 10% 할인이 적용되어야 한다.")
      void vip_o() {
          // given
          Member member = new Member(1L, "memberVIP", Grade.VIP);
          // when
          int discount = discountPolicy.discount(member, 10000);
          // then
          assertThat(discount).isEqualTo(1000);
      }
  
      @Test
      @DisplayName("VIP가 아니면 할인이 적용되지 않아야 한다.")
      void vip_x() {
          // given
          Member member = new Member(2L, "memberBASIC", Grade.BASIC);
          // when
          int discount = discountPolicy.discount(member, 10000);
          // then
          assertThat(discount).isEqualTo(0);
      }
  }
  ```



### 새로운 할인 정책 적용과 문제점

- 할인 정책을 변경하려면 클라이언트인 `OrderServiceImpl` 코드를 고쳐야 한다.

  ``` java
  public class OrderServiceImpl implements OrderService {
    // private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
  }
  ```

- 문제점 발견

  - 역할과 구현을 충실하게 분리했다 :white_check_mark:
  - 다형성도 활용하고, 인터페이스와 구현 객체를 분리했다 :white_check_mark:
  - OCP, DIP 같은 객체지향 설계 원칙을 충실히 준수했다 :red_circle: => 그렇게 보이지만 사실은 아니다.
  - DIP: 주문서비스 클라이언트(`OrderServiceImpl`)는 `DiscountPolicy` 인터페이스에 의존하면서 DIP를 지킨 것 같은데?
    - 클래스 의존관계를 분석해보자. 추상(인터페이스) 뿐만 아니라 구현체 클래스에도 의존하고 있다.
      - 추상(인터페이스) 의존: `DiscountPolicy`
      - 구현체 클래스: `FixDiscountPolicy`, `RateDiscountPolicy`
  - OCP: 변경하지 않고 확장할 수 있다고 했는데!
    - 지금 코드는 기능을 확장해서 변경하면, 클라잉너트 코드에 영향을 준다! 따라서 OCP를 위반한다.



### 왜 클라이언트 코드를 변경해야 할까?

클래스 다이어그램으로 의존관계를 분석해보자.

- 기대했던 의존관계

  ![3-1](./img/3-1.png)

  - 지금까지 단순히 `DiscountPolicy` 인터페이스만 의존한다고 생각했다.

- 실제 의존관계

  ![3-2](./img/3-2.png)

  - 잘보면 클라이언트인 `OrderServiceImpl`이 `DiscountPolicy` 인터페이스 뿐만 아니라 `FixDiscountPolicy`인 구체 클래스도 함께 의존하고 있다. 실제 코드를 보면 의존하고 있다! => **DIP 위반**

- 정책 변경

  ![3-3](./img/3-3.png)

  - **중요!** : 그래서 `FixDiscountPolicy`를 `RateDiscountPolicy` 로 변경하는 순간 `OrderServiceImpl` 의 소스코드도 함께 변경해야 한다! => **OCP 위반**



### 어떻게 문제를 해결할 수 있을까?

- 클라이언트 코드인 `OrderServiceImpl`은 `DiscountPolicy`의 인터페이스 뿐만 아니라 구체 클래스도 함께 의존한다.
- 그래서 구체 클래스를 변경할 때 클라이언트 코드도 함께 변경해야 한다.
- DIP 위반 -> 추상에만 의존하도록 변경(인터페이스에만 의존)
- DIP를 위반하지 않도록 인터페이스에만 의존하도록 의존관계를 변경하면 된다.



### 인터페이스에만 의존하도록 설계를 변경하자

![3-4](./img/3-4.png)

``` java
public class OrderServiceImpl implements OrderService {
  // private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
  private DiscountPolicy discountPolicy;
}
```

- 인터페이스에만 의존하도록 설계와 코드를 변경했다.
- 그런데 구현체가 없는데 어떻게 코드를 실행할 수 있을까?
- 실제 실행을 해보면 NPE가 발생한다.
- **해결방안**
  - <u>이 문제를 해결하려면 누군가가 클라이언트인 `OrderServiceImpl`에 `DiscountPolicy`의 구현 객체를 대신 생성하고 주입해주어야 한다.</u>



## 관심사의 분리

- 애플리케이션을 하나의 공연이라 생각해보자. 각각의 인터페이스를 배역(배우 역할)이라 생각하자. 그런데! 실제 배역 맞는 배우를 선택하는 것은 누가 하는가?
- 로미오와 줄리엣 공연을 하면 로미오 역할을 누가 할지 줄리엣 역할을 누가 할지는 배우들이 정하는게 아니다. 이전 코드는 마치 로미오 역할(인터페이스)을 하는 레오나르도 디카프리오(구현체, 배우)가 줄리엣 역할(인터페이스)을 하는 여자 주인공(구현체, 배우)을 직접 초빙하는 것과 같다. 디카프리오는 공연도 해야하고 동시에 여자 주인공도 공연에 직접 초빙해야 하는 다양한 책임을 가지고 있다.



### 관심사를 분리하자

- 배우는 본인의 역할인 배역을 수행하는 것에만 집중해야 한다.
- 디카프리오는 어떤 여자 주인공이 선택되더라도 똑같이 공연을 할 수 있어야 한다.
- 공연을 구성하고, 담당 배우를 섭외하고, 역할에 맞는 배우를 지정하는 책임을 담당하는 별도의 공연 기획자가 나올 시점이다.
- 공연 기획자를 만들고, 배우와 공연 기획자의 책임을 확실히 분리하자.



### AppConfig 등장

- 애플리케이션의 전체 동작 방식을 구성(config)하기 위해, 구현 객체를 생성하고, 연결하는 책임을 가지는 별도의 설정 클래스를 만들자.

- `AppConfig`

  ``` java
  package hello.core;
  
  import hello.core.discount.FixDiscountPolicy;
  import hello.core.member.MemberService;
  import hello.core.member.MemberServiceImpl;
  import hello.core.member.MemoryMemberRepository;
  import hello.core.order.OrderService;
  import hello.core.order.OrderServiceImpl;
  
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

- AppConfig는 애플리케이션의 실제 동작에 필요한 구현 객체를 생성한다.
  - `MemoryserviceImpl`
  - `MemoryMemberRepository`
  - `OrderServiceImpl`
  - `FixDiscountPolicy`
- AppConfig는 생성한 객체 인스턴스의 참조(레퍼런스)를 생성자를 통해서 주입(연결)해준다.
  - `MemberServiceImpl` -> `MemoryMemberRepository`
  - `OrdrServiceImpl` -> `MemoryMemberRepository`, `FixDiscountPolicy`

- MemberServiceImpl - 생성자 주입

  ``` java
  package hello.core.member;
  
  public class MemberServiceImpl implements MemberService {
  
      private final MemberRepository memberRepository;
  
      public MemberServiceImpl(MemberRepository memberRepository) {
          this.memberRepository = memberRepository;
      }
  
      @Override
      public void join(Member member) {
          memberRepository.save(member);
      }
  
      @Override
      public Member findMember(Long memberId) {
          return memberRepository.findById(memberId);
      }
  }
  ```

  - 설계 변경으로 `MemberServiceImpl` 은 `MemoryMemberRepository` 를 의존하지 않는다!
  - 단지 `MemberRepository` 인터페이스만 의존한다.
  - `MemberServiceImpl` 입장에서 생성자를 통해 어떤 구현 객체가 들어올지(주입될지)는 알 수 없다.
  - `MemberServiceImpl` 의 생성자를 통해서 어떤 구현 객체를 주입할지는 오직 외부(`AppConfig`) 에서 결정된다.
  - `MemberSErviceImpl` 은 이제부터 의존관계에 대한 고민은 외부에 맡기고 실행에만 집중하면 된다.

- 그림 - 클래스 다이어그램

  ![3-5](./img/3-5.png)

  - 객체의 생성과 연결은 `AppConfig` 가 담당한다.
  - DIP 완성: `MemberServiceImpl` 은 `MemberRepository` 인 추상에만 의존하면 된다. 이제 구체 클래스를 몰라도 된다.
  - 관심사의 분리: 객체를 생성하고 연결하는 역할과 실행하는 역할이 명확히 분리되었다.



### 회원 객체 인스턴스 다이어그램

![3-6](./img/3-6.png)

- `AppConfig` 객체는 `memoryMemberRepository` 객체를 생성하고 그 참조값을 `memberServiceImpl` 을 생성하면서 생성자로 전달한다.

- 클라이언트인 `memberServiceImpl` 입장에서 보면 의존관계를 마치 외부에서 주입해주는 것 같다고해서 DI(Dependency Injection) 우리말로 의존관계 주입 또는 의존성 주입이라 한다.

- OrderServiceImpl - 생성자 주입

  ``` java
  package hello.core.order;
  
  import hello.core.discount.DiscountPolicy;
  import hello.core.member.Member;
  import hello.core.member.MemberRepository;
  
  public class OrderServiceImpl implements OrderService {
  
      private final MemberRepository memberRepository;
      private final DiscountPolicy discountPolicy;
  
      public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
          this.memberRepository = memberRepository;
          this.discountPolicy = discountPolicy;
      }
  
  
      @Override
      public Order createOrder(Long memberId, String itemName, int itemPrice) {
          Member member = memberRepository.findById(memberId);
          int discountPrice = discountPolicy.discount(member, itemPrice);
  
          return new Order(memberId, itemName, itemPrice, discountPrice);
      }
  }
  ```

  - 설계 변경으로 `OrderServiceImpl` 은 `FixDiscountPolicy` 를 의존하지 않는다!
  - 단지 `DiscountPolicy` 인터페이스만 의존한다.
  - `OrderServiceImpl` 입장에서 생성자를 통해 어떤 구현 객체가 들어올지(주입될지)는 알 수 없다.
  - `OrderServiceImpl` 의 생성자를 통해서 어떤 구현 객체를 주입할지는 오직 외부(`AppConfig`) 에서 결정한다.
  - `OrderServiceImpl` 은 이제부터 실행에만 집중하면 된다.
  - `OrderServiceImpl` 에는 `MemorymemberRepository`, `FixDiscountPolicy` 객체의 의존관계가 주입된다.



### AppConfig 실행

- 사용 클래스 - MemberApp

  ``` java
  package hello.core;
  
  import hello.core.member.Grade;
  import hello.core.member.Member;
  import hello.core.member.MemberService;
  
  public class MemberApp {
      public static void main(String[] args) {
          AppConfig appConfig = new AppConfig();
          MemberService memberService = appConfig.memberService();
  
          Member member = new Member(1L, "memberA", Grade.VIP);
          memberService.join(member);
  
          Member findMember = memberService.findMember(1L);
          System.out.println("new member = " + member.getName());
          System.out.println("find Member = " + findMember.getName());
  
      }
  }
  ```

- 사용 클래스 - OrderApp

  ``` java
  package hello.core;
  
  import hello.core.member.Grade;
  import hello.core.member.Member;
  import hello.core.member.MemberService;
  import hello.core.order.Order;
  import hello.core.order.OrderService;
  
  public class OrderApp {
  
      public static void main(String[] args) {
          AppConfig appConfig = new AppConfig();
          MemberService memberService = appConfig.memberService();
          OrderService orderService = appConfig.orderService();
  
          long memberId = 1L;
  
          Member member = new Member(memberId, "memberA", Grade.VIP);
          memberService.join(member);
  
          Order order = orderService.createOrder(memberId, "itemA", 10000);
  
          System.out.println("order = " + order);
      }
  }
  
  ```

- AppConfig를 통해서 관심사를 확실하게 분리했다.
- 배역, 배우를 생각해보자.
- AppConfig는 공연 기획자다.
- AppConfig는 구체 클래스를 선택한다. 배역에 맞는 담당 배우를 선택한다. 애플리케이션이 어떻게 동작해야 할지 전체 구성을 책임진다.
- 이제 각 배우들은 담당 기능을 실행하는 책임만 지면 된다.
- `OrderServiceImpl` 은 기능을 실행하는 책임만 지면 된다.





