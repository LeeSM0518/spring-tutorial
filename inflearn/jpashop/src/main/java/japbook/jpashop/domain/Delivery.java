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
