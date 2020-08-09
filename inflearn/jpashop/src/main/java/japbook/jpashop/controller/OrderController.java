package japbook.jpashop.controller;

import japbook.jpashop.domain.Member;
import japbook.jpashop.domain.Order;
import japbook.jpashop.domain.item.Item;
import japbook.jpashop.repository.OrderSearch;
import japbook.jpashop.service.ItemService;
import japbook.jpashop.service.MemberService;
import japbook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final MemberService memberService;
  private final ItemService itemService;

  @GetMapping("/order")
  public String createForm(Model model) {
    List<Member> members = memberService.findMembers();
    List<Item> items = itemService.findItems();

    model.addAttribute("members", members);
    model.addAttribute("items", items);

    return "order/orderForm";
  }

  @PostMapping("/order")
  // @RequestParam : form 형식으로부터 넘어온 값
  public String order(@RequestParam("memberId") Long memberId,
                      @RequestParam("itemId") Long itemId,
                      @RequestParam("count") int count) {
    orderService.order(memberId, itemId, count);
    return "redirect:/orders";
  }

  @GetMapping("/orders")
  public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
    List<Order> orders = orderService.findOrders(orderSearch);
    model.addAttribute("orders", orders);
//    model.addAttribute("orderSearch", orderSearch); 이 코드가 생략이되서 작성되어 있는 것이다.
    return "order/orderList";
  }

  @PostMapping("/orders/{orderId}/cancel")
  public String cancelOrder(@PathVariable("orderId") Long orderId) {
    orderService.cancelOrder(orderId);
    return "redirect:/orders";
  }

}
