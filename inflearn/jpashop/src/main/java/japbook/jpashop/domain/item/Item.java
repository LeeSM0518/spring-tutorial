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
