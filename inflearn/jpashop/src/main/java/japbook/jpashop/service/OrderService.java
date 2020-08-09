package japbook.jpashop.service;

import japbook.jpashop.domain.Delivery;
import japbook.jpashop.domain.Member;
import japbook.jpashop.domain.Order;
import japbook.jpashop.domain.OrderItem;
import japbook.jpashop.domain.item.Item;
import japbook.jpashop.repository.ItemRepository;
import japbook.jpashop.repository.MemberRepository;
import japbook.jpashop.repository.OrderRepository;
import japbook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final MemberRepository memberRepository;
  private final ItemRepository itemRepository;

  /**
   * 주문
   */
  @Transactional
  public Long order(Long memberId, Long itemId, int count) {

    //엔티티 조회
    Member member = memberRepository.findOne(memberId);
    Item item = itemRepository.findOne(itemId);

    //배송정보 생성
    //  CascadeType.ALL 타입을 통해서 Order을 퍼시스트하면 Delivery도 강제로 퍼시스트된다.
    Delivery delivery = new Delivery();
    delivery.setAddress(member.getAddress());

    //주문상품 생성
    //  CascadeType.ALL 타입을 통해서 Order을 퍼시스트하면 OrderItem도 강제로 퍼시스트된다.
    OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

    /**
     * 다른 개발자가
     * OrderItem을 생성할 때 아래와 같이 생성을 할 수도 있다.
     * 하지만 이처럼 여러 곳에서 생성을 하게 되면 생성자의 파라미터가
     * 늘어났을 때 변경하기가 힘들다. 즉, 유지보수가 힘들다.
     * 그래서 기본 생성자의 접근제한자를 protected로 하여
     * 기본 생성자를 호출할 수 없도록 한다.
     */
//    OrderItem orderItem1 = new OrderItem();
//    orderItem1.setCount();

    //주문 생성
    Order order = Order.createOrder(member, delivery, orderItem);

    //주문 저장
    orderRepository.save(order);

    return order.getId();
  }

  /**
   * 주문 취소
   */
  @Transactional
  public void cancelOrder(Long orderId) {
    //주문 엔티티 조회
    Order order = orderRepository.findOne(orderId);
    //주문 취소
    order.cancel();
  }

  /**
   * 검색
   */
  public List<Order> findOrders(OrderSearch orderSearch) {
    return orderRepository.findAllCriteria(orderSearch);
  }

}
