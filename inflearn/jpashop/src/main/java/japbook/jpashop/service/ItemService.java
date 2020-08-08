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
