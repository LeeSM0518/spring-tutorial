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
