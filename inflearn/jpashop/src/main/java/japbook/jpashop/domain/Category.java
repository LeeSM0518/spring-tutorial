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
