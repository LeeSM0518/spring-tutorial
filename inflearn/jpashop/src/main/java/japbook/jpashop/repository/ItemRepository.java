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
