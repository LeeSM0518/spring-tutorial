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
