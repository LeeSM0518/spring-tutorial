package japbook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

  @GetMapping("hello")
  // Model 에 데이터를 실어서 View 에 넘긴다.
  public String hello(Model model){
    model.addAttribute("data", "hello!!!");
    return "hello"; // "hello" 는 View의 이름이다.
  }

}
