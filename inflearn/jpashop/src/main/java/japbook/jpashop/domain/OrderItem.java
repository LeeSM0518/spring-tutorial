package japbook.jpashop.domain;

import japbook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
/**
 * 기본 생성자의 접근 제한자를 PROTECTED로 설정
 * 왜냐하면, createOrderItem을 만들어 놨으므로
 * 기본 생성자 말고 createOrderItem만 쓰도록 제한하기 위해서
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
