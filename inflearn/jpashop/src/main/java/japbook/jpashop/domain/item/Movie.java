package japbook.jpashop.domain.item;

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
