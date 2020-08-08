# 엔티티 클래스 개발

* 예제에서는 설명을 쉽게하기 위해 Getter, Setter를 모두 만들어 놓는데, 실무에서는 **가급적 Getter는 열어두고, Setter는 꼭 필요한 경우에만 사용하는 것을 추천**
* 엔티티를 변경할 때는 Setter 대신에 **변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야 한다.**

<br>

### 회원 엔티티

```java
package japbook.jpashop.domain;

import japbook.jpashop.domain.item.Address;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

  @Id @GeneratedValue
  @Column(name = "member_id")
  private Long id;

  private String name;

  // JPA 내장 타입
  @Embedded
  private Address address;

  // Member 랑 Order 가 일대다 관계이기 때문에 아래와 같은
  //   연관 관계 에노테이션 사용
  @OneToMany(mappedBy = "member") // Order 테이블에 있는 member 필드에 의해 매핑 됨을 의미 (읽기 전용)
  private List<Order> orders = new ArrayList<>();

}
```

* Member도 Order를 가지고 있고 Order도 Member를 가지고 있다. 즉, 양방향 연관관계 이다.
* 여기서 중요한 것은 한 곳이 바뀌면 객체는 두 개여서 둘 다 바뀌여야 하는데, 테이블한 하나이기 때문에 문제가 생긴다.
* 그래서, 연관관계의 주인을 선택해줘야 한다.
  * 연관관계의 주인을 Foreign key를 갖고 있는 엔티티로 한다.

 <br>

### 주문 엔티티

```java
package japbook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

  @Id @GeneratedValue
  @Column(name = "order_id")
  private Long id;

  // Order 랑 Membre 는 다대일 관계이기 때문에 아래와 같은
  //   연관 관계 에노테이션 사용
  @ManyToOne(fetch = LAZY) // 연관 관계의 주인은 이와 같이 옵션이 없다.
  @JoinColumn(name = "member_id") // foreign key = member_id 를 의미
  private Member member;

  // cascade : 데이터 변경에 대해서 전파한다.
  @OneToMany(mappedBy = "order", cascade = ALL)
  private List<OrderItem> orderItems = new ArrayList<>();

  // Order 쪽에서 DB 접근이 많으므로 Order가 연관 관계의 주인이 된다.
  @OneToOne(fetch = LAZY, cascade = ALL) // 일대일 관계
  @JoinColumn(name = "delivery_id")
  private Delivery delivery;

  private LocalDateTime orderDate; // 주문시간

  @Enumerated(EnumType.STRING)
  private OrderStatus status; // 주문상태 [ORDER, CANCEL]

  // == 연관관계 메서드 (양방향 관계일 때 만든다) == //
  public void setMember(Member member) {
    this.member = member;
    member.getOrders().add(this);
  }

  public void addOrderItem(OrderItem orderItem) {
    orderItems.add(orderItem);
    orderItem.setOrder(this);
  }

  public void setDelivery(Delivery delivery) {
    this.delivery = delivery;
    delivery.setOrder(this);
  }

}
```

<br>

### 배송 엔티티

```java
package japbook.jpashop.domain;

import japbook.jpashop.domain.item.Address;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Getter @Setter
@Entity
public class Delivery {

  @Id @GeneratedValue
  @Column(name = "delivery_id")
  private Long id;

  @OneToOne(mappedBy = "delivery", fetch = LAZY)
  private Order order;

  @Embedded
  private Address address;

  // EnumType 주의할 점
  //  * EnumType.ORDINAL : READY = 1, COMP = 2 이와 같이 DB에 구성됨 그러므로 절대 사용하지 말것!!!!
  //  * EnumType.STRING  : 문자열로 값 관리
  @Enumerated(EnumType.STRING)
  private DeliveryStatus status; // READY, COMP

}
```

<br>

### 주문상태

```java
package japbook.jpashop.domain;

public enum  OrderStatus {

  ORDER, CANCEL

}
```

<br>

### 주문상품 엔티티

```java
package japbook.jpashop.domain;

import japbook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
public class OrderItem {

  @Id
  @GeneratedValue
  @Column(name = "order_item_id")
  private Long id;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "item_id")
  private Item item;

  @ManyToOne(fetch = LAZY)
  // 양방향 연관관계이고 Order가 주인이므로 Order의 매핑될 곳을 알려줘야 한다.
  @JoinColumn(name = "order_id")
  private Order order;

  private int orderPrice;  // 주문 가격
  private int count;       // 주문 수량

}
```

<br>

### 상품 엔티티

```java
package japbook.jpashop.domain.item;

import japbook.jpashop.domain.Category;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
// 상속관계 전략을 정해줘야 한다. 우리는 싱글 테이블 전략을 사용할 것이므로 아래와 같이 작성
//  JOINED          : 정교화된 스타일
//  SINGLE_TABLE    : 자식 테이블들을 한 테이블에 전부 넣는 방법
//  TABLE_PER_CLASS : 자식 테이블들을 전부 따로 만드는 방법
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// 상속관계 매핑을 위해 부모 클래스에 선언한다.
//  하위 클래스를 구분하는 용도의 컬럼이다.
@DiscriminatorColumn(name = "dtype")
public abstract class Item {

  @Id
  @GeneratedValue
  @Column(name = "item_id")
  private Long id;

  private String name;
  private int price;
  private int stockQuantity;

  @ManyToMany(mappedBy = "items")
  private List<Category> categories = new ArrayList<>();

}

```

<br>

### 상품 - 도서 엔티티

```java
package japbook.jpashop.domain.item;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

@Entity
// 싱글 테이블 이므로 DB가 값을 넣을 때 어떤 값을 넣을지 알려줘야 한다.
//  엔티티를 저장할 때 슈퍼타입의 구분 컬럼에 저장할 값을 지정한다.
@DiscriminatorValue("B")
@Getter @Setter
public class Book extends Item {

  private String author;
  private String isbn;

}

```

<br>

### 상품 - 음반 엔티티

```java
package japbook.jpashop.domain.item;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@DiscriminatorValue("A")
@Entity
public class Album extends Item {

  private String artist;
  private String etc;

}
```

<br>

### 상품 - 영화 엔티티

```java
package japbook.jpashop.domain;

import japbook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("M")
@Getter @Setter
public class Movie extends Item {

  private String director;
  private String actor;

}
```

<br>

### 배송 엔티티

```java
package japbook.jpashop.domain;

import japbook.jpashop.domain.item.Address;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Getter @Setter
@Entity
public class Delivery {

  @Id @GeneratedValue
  @Column(name = "delivery_id")
  private Long id;

  @OneToOne(mappedBy = "delivery", fetch = LAZY)
  private Order order;

  @Embedded
  private Address address;

  // EnumType 주의할 점
  //  * EnumType.ORDINAL : READY = 1, COMP = 2 이와 같이 DB에 구성됨 그러므로 절대 사용하지 말것!!!!
  //  * EnumType.STRING  : 문자열로 값 관리
  @Enumerated(EnumType.STRING)
  private DeliveryStatus status; // READY, COMP

}

```

<br>

### 배송 상태

```java
package japbook.jpashop.domain;

public enum  DeliveryStatus {

  READY, COMP

}
```

<br>

### 카테고리 엔티티

```java
package japbook.jpashop.domain;

import japbook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Getter
@Setter
@Entity
public class Category {

  @Id
  @GeneratedValue
  @Column(name = "cateogory_id")
  private Long id;

  private String name;

  @ManyToMany
  // 다대다 관계는 중간 관계를 테이블로 매핑해줘야 한다.
  @JoinTable(name = "category_item",
      joinColumns = @JoinColumn(name = "category_id"),   // 중간 테이블의 조인 컬럼
      inverseJoinColumns = @JoinColumn(name = "item_id")) // category_item 테이블의 item 쪽 id
  private List<Item> items = new ArrayList<>();

  // 카테고리는 계층적 구조를 갖고 있어서 자신의 부모나 자식을 보고싶기 때문에,
  //  아래와 같은 필드들을 추가해 샐프 양방향 연관관계를 성립시켜준다.

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "parent_id")
  private Category parent;

  @OneToMany(mappedBy = "parent")
  private List<Category> child = new ArrayList<>();

  // 연관 관계 메서드
  public void addChildCategory(Category child) {
    this.child.add(child);
    child.setParent(this);
  }

}

```

<br>

### 주소 값 타입

```java
package japbook.jpashop.domain.item;

import lombok.Getter;

import javax.persistence.Embeddable;

// 값 타입
// JPA 내장 타입이기 때문에 아래와 같은 에노테이션 사용
@Embeddable
@Getter
public class Address {

  private String city;
  private String street;
  private String zipcode;

  // JPA 스펙상 엔티티나 임베디드 타입(@Embeddable)은
  //  자바 기본 생성자를 public 또는 protected 로 설정해야 한다. (JPA가 리플랙션을 사용할 수 있도록)
  //  protected 로 설정하는 것이 안전.
  protected Address() {
  }

  // 값 타입은 변경 불가능하게 설계해야 한다.
  //  그래서 @Setter 를 제거하고 아래와 같이
  //  생성자만 구현되어 있다.
  public Address(String city, String street, String zipcode) {
    this.city = city;
    this.street = street;
    this.zipcode = zipcode;
  }

}

```

<br>

# 엔티티 설계시 주의점

## 모든 연관관계는 지연로딩으로 설정!!!

* 즉시로딩( `EAGER` )은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다. 특히, JPQL을 사용할 때 `N+1` 문제가 자주 발생한다.
  * 즉시로딩은 어떤 테이블을 조회할 때, 연관된 테이블들을 모두 같이 조회하는것
* **실무에서 모든 연관관계는 지연로딩( `LAZY` )으로 설정해야 한다.**
* 연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용한다.
* @XToOne(OneToOne, ManyToOne) 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.

<br>

## 컬렉션은 필드에서 초기화하자.

컬렉션은 필드에서 바로 초기화 하는 것이 안전하다.

* `null` 문제에서 안전하다.

* 하이버네이트는 엔티티를 영속할 때, 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다. 만약 `getOrders()` 처럼 임의의 메서드에서 컬렉션을 잘못 생성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다. 따라서 필드레벨에서 생성하는 것이 가장 안전하고, 코드도 간결하다.

* **예제 코드**

  ```java
  Member member = new Member();
  System.out.println(member.getOrders().getClass());
  em.persist(team); // 퍼시스트로 영속하다. => JPA 입장에서 DB에 저장하겠다고 선언.
  System.out.println(member.getOrders().getClass());
  ```

  * **em.persist(team)**
    * 퍼시스트로 영속한다는 의미이다. 즉, JPA 입장에서  DB에 저장하겠다는 선언이다.
    * 그러면, JPA는 영속 컨텍스트 관리를 해야한다.

  실행 결과

  ```
  class java.util.ArrayList
  class org.hibernate.collection.internal.PersistentBag
  ```

  * 하이버네이트가 ArrayList를 추적해야하기 때문에, 한번 감싸서 관리하게 된다.

* 즉, 엔티티의 컬렉션 필드는 하이버네이트가 관리하므로 절대로 함부로 수정하지 말 것!!

<br>

## 테이블, 컬럼명 생성 전략

스프링 부트에서 하이버네이트 기본 매핑 전략을 변경해서 실제 테이블 필드명은 다르다.

* 엔티티를 생성하고 `@Table` 어노테이션으로 테이블명을 안알려주거나, `@Column` 어노테이션으로 컬럼명을 안알려주었을 때, `SpringPhysicalNamingStrategy` 가 작동 (엔티티(필드) => 테이블(컬럼))
  * 카멜 케이스 => 언더스코어 (memberPoint => member_point)
  * .(점) => _(언더스코어)
  * 대문자 => 소문자

<br>

**적용 2 단계**

**논리명** : 테이블, 컬럼 명이 적혀 있지 않을 때의 이름

**물리명** : 테이블, 컬럼 명이 적혀 있던 적혀 있지 않던 해당 이름

1. **논리명 생성** : 명시적으로 컬럼, 테이블명을 직접 적지 않으면 ImplicitNamingStrategy 사용

   * `spring.jpa.hibernate.naming.implicit-strategy` : 테이블이나, 컬럼명을 명시하지 않을 때 논리명 적용.

2. **물리명 적용** : 모든 명명 규칙

   * `spring.jpa.hibernate.naming.physical-strategy` : 모든 논리명에 적용됨, 실제 테이블에 적용

     (username => usernm 등으로 회사 룰로 바꿀 수 있다.)