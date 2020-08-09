package japbook.jpashop.service;

import japbook.jpashop.domain.item.Book;
import japbook.jpashop.domain.item.Item;
import japbook.jpashop.repository.ItemRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;

  @Transactional
  public void saveItem(Item item) {
    itemRepository.save(item);
  }

  // 변경 감지를 이용한 데이터 변경
  //  setter를 쓰지 않고 최대한 엔티티에 데이터 수정 메서드를 만든다.
  @Transactional
  public void updateItem(Long itemId, String name, int price, int stockQuantity) {
    Item findItem = itemRepository.findOne(itemId);
//    findItem.setPrice(price);
//    findItem.setName(name);
//    findItem.setStockQuantity(stockQuantity);

    // UpdateItemDto 를 만들어서 관리하면 더욱 유지보수하기 좋다.
    findItem.change(name, price, stockQuantity);
  }

  public List<Item> findItems() {
    return itemRepository.findAll();
  }

  public Item findOne(Long itemId) {
    return itemRepository.findOne(itemId);
  }

}
