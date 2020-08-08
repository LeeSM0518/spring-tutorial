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
