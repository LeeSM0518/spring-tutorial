package controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import spring.DuplicateMemberDaoException;
import spring.MemberRegisterService;
import spring.RegisterRequest;

@Controller
public class RegisterController {

  private MemberRegisterService memberRegisterService;

  public void setMemberRegisterService(MemberRegisterService memberRegisterService) {
    this.memberRegisterService = memberRegisterService;
  }

  @RequestMapping("/register/step1")
  public String handleStep1() {
    return "register/step1";
  }

  @PostMapping("/register/step2")
  public String handleStep2(
      @RequestParam(value = "agree", defaultValue = "false") Boolean agree,
      Model model) {
    if (!agree) {
      return "register/step1";
    }
    model.addAttribute("registerRequest", new RegisterRequest());
    return "register/step2";
  }

  @GetMapping("/register/step2")
  public String handleStep2Get() {
    return "redirect:/register/step1";
  }

  @PostMapping("/register/step3")// 스프링 MVC가 handleStep3() 메서드를 호출할 때
  //  커맨드 객체와 연결된 Errors 객체를 생성해서 파라미터로 전달한다.
  public String handleStep3(RegisterRequest regReq, Errors errors) {
    // 커맨드 객체의 값이 올바른지 검사하고 그 결과를 Errors 객체에 담는다.
    new RegisterRequestValidator().validate(regReq, errors);
    // 에러가 존재하는지 검사
    if (errors.hasErrors())
      return "register/step2";
    try {
      memberRegisterService.regist(regReq);
      return "register/step3";
    } catch (DuplicateMemberDaoException ex) {
      // 동일한 이메일을 가진 회원 데이터가 이미 존재시 발생
      // 이메일 중복 에러를 추가하기 위해 "email" 프로퍼티의
      //  에러 코드로 "duplicate" 추가
      errors.rejectValue("email", "duplicate");
      return "register/step2";
    }
  }

}
