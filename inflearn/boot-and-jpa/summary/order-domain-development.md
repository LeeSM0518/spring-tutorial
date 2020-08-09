# 주문 도메인 개발

**구현 기능**

* 상품 주문
* 주문 내역 조회
* 주문 취소

<br>

**순서**

* 주문 엔티티, 주문상품 엔티티 개발
* 주문 리포지토리 개발
* 주문 서비스 개발
* 주문 검색 기능 개발
* 주문 기능 테스트

<br>

## 주문, 주문상품 엔티티 개발

### 주문 엔티티 개발

```java
package japbook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

  @Id
  @GeneratedValue
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

  // == 생성 메서드 == //
  public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
    Order order = new Order();
    order.setMember(member);
    order.setDelivery(delivery);
    Arrays.stream(orderItems).forEach(order::addOrderItem);
    order.setStatus(OrderStatus.ORDER);
    order.setOrderDate(LocalDateTime.now());
    return order;
  }

  // == 비즈니스 로직 == //
  /**
   * 주문 취소
   */
  public void cancel() {
    if (delivery.getStatus() == DeliveryStatus.COMP) {
      throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
    }
    this.setStatus(OrderStatus.CANCEL);
    orderItems.forEach(OrderItem::cancel);
  }

  // == 조회 로직 == //

  /**
   * 전체 주문 가격 조회
   */
  public int getTotalPrice() {
    return orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
  }

}

```

* **getTotalPrice()** : 연관된 주문 상품들의 가격을 조회해서 더한 값을 반환한다. (실무에서는 주로 주문에 전체 주문 가격 필드를 두고 역정규화 한다.)

<br>

### 주문상품 엔티티 개발

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

  // == 생성 메서드 == //
  public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
    OrderItem orderItem = new OrderItem();
    orderItem.setItem(item);
    orderItem.setOrderPrice(orderPrice);
    orderItem.setCount(count);

    item.removeStock(count);
    return orderItem;
  }

  // == 비즈니스 로직 == //
  public void cancel() {
    getItem().addStock(count);
  }

  public int getTotalPrice() {
    return getOrderPrice() * getCount();
  }
}

```

<br>

## 주문 리포지토리 개발

```java
package japbook.jpashop.repository;

import japbook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
  
  private final EntityManager em;
  
  public void save(Order order) {
    em.persist(order);
  }
  
  public Order findOne(Long id) {
    return em.find(Order.class, id);
  }
  
//  public List<Order> findAll(OrderSearch orderSearch) {}
  
}

```

<br>

## 주문 서비스 개발

```java
package japbook.jpashop.service;

import japbook.jpashop.domain.Delivery;
import japbook.jpashop.domain.Member;
import japbook.jpashop.domain.Order;
import japbook.jpashop.domain.OrderItem;
import japbook.jpashop.domain.item.Item;
import japbook.jpashop.repository.ItemRepository;
import japbook.jpashop.repository.MemberRepository;
import japbook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final MemberRepository memberRepository;
  private final ItemRepository itemRepository;

  /**
   * 주문
   */
  @Transactional
  public Long order(Long memberId, Long itemId, int count) {

    //엔티티 조회
    Member member = memberRepository.findOne(memberId);
    Item item = itemRepository.findOne(itemId);

    //배송정보 생성
    //  CascadeType.ALL 타입을 통해서 Order을 퍼시스트하면 Delivery도 강제로 퍼시스트된다.
    Delivery delivery = new Delivery();
    delivery.setAddress(member.getAddress());

    //주문상품 생성
    //  CascadeType.ALL 타입을 통해서 Order을 퍼시스트하면 OrderItem도 강제로 퍼시스트된다.
    OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

    /**
     * 다른 개발자가
     * OrderItem을 생성할 때 아래와 같이 생성을 할 수도 있다.
     * 하지만 이처럼 여러 곳에서 생성을 하게 되면 생성자의 파라미터가
     * 늘어났을 때 변경하기가 힘들다. 즉, 유지보수가 힘들다.
     * 그래서 기본 생성자의 접근제한자를 protected로 하여
     * 기본 생성자를 호출할 수 없도록 한다.
     */
//    OrderItem orderItem1 = new OrderItem();
//    orderItem1.setCount();

    //주문 생성
    Order order = Order.createOrder(member, delivery, orderItem);

    //주문 저장
    orderRepository.save(order);

    return order.getId();
  }

  /**
   * 주문 취소
   */
  @Transactional
  public void cancelOrder(Long orderId) {
    //주문 엔티티 조회
    Order order = orderRepository.findOne(orderId);
    //주문 취소
    order.cancel();
  }

  /**
   * 검색
   */
//  public List<Order> findOrders(OrderSearch orderSearch) {
//    return orderRepository.findAll(orderSearch);
//  }

}

```

* **CacadeType.ALL**
  * `Order` 가 `Delivery` 와 `OrderItem` 을 관리하기 때문에 `CascadeType.ALL` 을 사용하여 관리한다.
  * `Delivery` 와 `OrderItem` 이 다른 곳에서 사용하면 `CacadeType.ALL` 을 사용하면 안된다.
* 주문 서비스를 만들면서 도메인 모델 패턴을 적용했는데, 이때 `Order` 와 `OrderItem` 클래스에 `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 를 사용하여 기본 생성자를 호출할 수 없도록 막았다.

<br>

### 참고

* 주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다. 
* 서비스 계층은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다. 이처럼 엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것을 **도메인 모델 패턴** (https://martinfowler.com/eaaCatalog/domainModel.html) 이라 한다.
* 반대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 대부분의 비즈니스 로직을 처리하는 것을 **트랜잭션 스크립트 패턴** (https://martinfowler.com/eaaCatalog/transactionScript.html) 이라한다.

<br>

## 주문 기능 테스트

### 테스트 요구사항

* 상품 주문이 성공해야 한다.
* 상품을 주문할 때 재고 수량을 초과하면 안 된다.
* 주문 취소가 성공해야 한다.

<br>

### 상품 주문 테스트 코드

```java
package japbook.jpashop.service;

import japbook.jpashop.domain.Member;
import japbook.jpashop.domain.Order;
import japbook.jpashop.domain.OrderStatus;
import japbook.jpashop.domain.item.Address;
import japbook.jpashop.domain.item.Book;
import japbook.jpashop.domain.item.Item;
import japbook.jpashop.exception.NotEnoughStockException;
import japbook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

  @Autowired
  EntityManager em;
  @Autowired
  OrderService orderService;
  @Autowired
  OrderRepository orderRepository;

  @Test
  public void 상품주문() throws Exception {
    // given
    Member member = createMember();
    Book book = createBook("시골 JPA", 10000, 10);
    int orderCount = 2;

    // when
    Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

    // then
    Order getOrder = orderRepository.findOne(orderId);

    assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
    assertEquals("주문한 상품 종류 수가 정확해야 한다.", 1, getOrder.getOrderItems().size());
    assertEquals("주문 가격은 가격 * 수량이다.", 10000 * orderCount, getOrder.getTotalPrice());
    assertEquals("주문 수량만큼 재고가 줄어야 한다.", 8, book.getStockQuantity());
  }

  private Book createBook(String name, int price, int stockQuantity) {
    Book book = new Book();
    book.setName(name);
    book.setPrice(price);
    book.setStockQuantity(stockQuantity);
    em.persist(book);
    return book;
  }

  private Member createMember() {
    Member member = new Member();
    member.setName("회원1");
    member.setAddress(new Address("서울", "강가", "123-123"));
    em.persist(member);
    return member;
  }

  @Test(expected = NotEnoughStockException.class)
  public void 상품주문_재고수량초과() throws Exception {
    // given
    Member member = createMember();
    Item item = createBook("시골 JPA", 10000, 10);

    int orderCount = 11;

    // when
    orderService.order(member.getId(), item.getId(), orderCount);

    // then
    fail("재고 수량 부족 예외가 발생해야 한다.");
  }

  @Test
  public void 주문취소() throws Exception {
    // given
    Member member = createMember();
    Book item = createBook("시골 JPA", 10000, 10);
    int orderCount = 2;
    Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

    // when
    orderService.cancelOrder(orderId);

    // then
    Order getOrder = orderRepository.findOne(orderId);

    assertEquals("주문 취소시 상태는 CANCEL 이다.", OrderStatus.CANCEL, getOrder.getStatus());
    assertEquals("주문이 취소된 상품은 그만큼 재고가 증가해야 한다.", 10, item.getStockQuantity());
  }

}
```

<br>

## 주문 검색 기능 개발

JPA에서 **동적 쿼리를** 어떻게 해결해야 하는가?

<br>

**주문 리포지토리**

```java
package japbook.jpashop.repository;

import japbook.jpashop.domain.Delivery;
import japbook.jpashop.domain.Member;
import japbook.jpashop.domain.Order;
import japbook.jpashop.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

  private final EntityManager em;

  public void save(Order order) {
    em.persist(order);
  }

  public Order findOne(Long id) {
    return em.find(Order.class, id);
  }

  // JPQL
  public List<Order> findAllByString(OrderSearch orderSearch) {
    String jpql = "select o From Order o join o.member m";
    boolean isFirstCondition = true;

    // 주문 상태 검색
    if (orderSearch.getOrderStatus() != null) {
      if (isFirstCondition) {
        jpql += " where";
        isFirstCondition = false;
      } else {
        jpql += " and";
      }
      jpql += " o.status = :status";
    }

    // 회원 이름 검색
    if (StringUtils.hasText(orderSearch.getMemberName())) {
      if (isFirstCondition) {
        jpql += " where";
        isFirstCondition = false;
      } else {
        jpql += " and";
      }
      jpql += " m.name like :name";
    }
    TypedQuery<Order> query = em.createQuery(jpql, Order.class)
        .setMaxResults(1000);

    if (orderSearch.getOrderStatus() != null) {
      query = query.setParameter("status", orderSearch.getOrderStatus());
    }
    if (StringUtils.hasText(orderSearch.getMemberName())) {
      query = query.setParameter("name", orderSearch.getMemberName());
    }

    return query.getResultList();
  }

  /**
   * JPA Criteria
   */
  public List<Order> findAllCriteria(OrderSearch orderSearch) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Order> cq = cb.createQuery(Order.class);
    Root<Order> o = cq.from(Order.class);
    Join<Order, Member> m = o.join("member", JoinType.INNER);

    List<Predicate> criteria = new ArrayList<>();

    // 주문 상태 검색
    if (orderSearch.getOrderStatus() != null) {
      Predicate status = cb.equal(o.get("status"),
          orderSearch.getOrderStatus());
      criteria.add(status);
    }

    // 회원 이름 검색
    if (StringUtils.hasText(orderSearch.getMemberName())) {
      Predicate name =
          cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
      criteria.add(name);
    }

    cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
    TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
    return query.getResultList();
  }

}
```