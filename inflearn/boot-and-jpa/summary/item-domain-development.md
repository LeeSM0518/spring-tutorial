# 상품 도메인 개발

**구현기능**

* 상품 등록
* 상품 목록 조회
* 상품 수정

<br>

**순서**

* 상품 엔티티 개발(비즈니스 로직 추가)
* 상품 리포지토리 개발
* 상품 서비스 개발, 상품 기능 테스트

<br>

## 상품 엔티티 개발(비즈니스 로직 추가)

**상품 엔티티**

 ```java
package japbook.jpashop.domain.item;

import japbook.jpashop.domain.Category;
import japbook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
// @Setter setter를 제거하고 핵심 비지니스 로직을 사용하여 데이터를 수정한다.
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

  // == 비즈니스 로직 == // (이로써 응집도가 올라간다)

  /**
   * stock 증가
   */
  public void addStock(int quantity) {
    this.stockQuantity += quantity;
  }

  /**
   * stock 감소
   */
  public void removeStock(int quantity) {
    int restStock = this.stockQuantity - quantity;
    if (restStock < 0) {
      throw new NotEnoughStockException("need more stock");
    }
    this.stockQuantity = restStock;
  }

}

 ```

<br>

**예외 추가**

```java
package japbook.jpashop.exception;

public class NotEnoughStockException extends RuntimeException {

  public NotEnoughStockException() {
    super();
  }

  public NotEnoughStockException(String message) {
    super(message);
  }

  public NotEnoughStockException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotEnoughStockException(Throwable cause) {
    super(cause);
  }

}

```

<br>

**비즈니스 로직 분석**

* `addStock()` : 파라미터로 넘어온 수만큼 재고를 늘린다.
* `removeStock()` : 파라미터로 넘어온 수만큼 재고를 줄인다. 
  * 만약 재고가 부족하면 예외가 발생한다. 주로 상품을 주문할 때 사용한다.

<br>

## 상품 리포지토리 개발

```java
package japbook.jpashop.repository;

import japbook.jpashop.domain.item.Item;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Getter @Setter
@Repository
@RequiredArgsConstructor
public class ItemRepository {

  private final EntityManager em;

  public void save(Item item) {
    if (item.getId() == null) {
      em.persist(item); // item을 등록
    } else {
      em.merge(item);   // 업데이트와 비슷
    }
  }

  public Item findOne(Long id) {
    return em.find(Item.class, id);
  }

  public List<Item> findAll() {
    return em.createQuery("select i from Item i", Item.class)
        .getResultList();
  }

}

```

* **save()**
  * `id` 가 없으면 신규로 보고 `persist()`
  * `id` 가 있으면 이미 데이터베이스에 저장된 엔티티를 수정한다고 보고, `merge()` 를 실행

<br>

## 상품 서비스 개발

```java
package japbook.jpashop.service;

import japbook.jpashop.domain.item.Item;
import japbook.jpashop.repository.ItemRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Getter @Setter
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;

  @Transactional
  public void saveItem(Item item) {
    itemRepository.save(item);
  }
  
  public List<Item> findItems() {
    return itemRepository.findAll();
  }
  
  public Item findOne(Long itemId) {
    return itemRepository.findOne(itemId);
  }

}

```