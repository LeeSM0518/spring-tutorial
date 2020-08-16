package securitytutorial.tutorial.controller.user;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import securitytutorial.tutorial.domain.entity.Account;
import securitytutorial.tutorial.domain.dto.AccountDto;
import securitytutorial.tutorial.service.UserService;

import java.security.Principal;

@Controller
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @GetMapping("/users")
  public String createUser() {
    return "user/login/register";
  }

  @PostMapping("/users")
  public String createUser(AccountDto accountDto) {

    ModelMapper modelMapper = new ModelMapper();
    Account account = modelMapper.map(accountDto, Account.class);
    account.setPassword(passwordEncoder.encode(account.getPassword()));
    userService.createUser(account);

    return "redirect:/";
  }

  @GetMapping(value = "/mypage")
  public String myPage(@AuthenticationPrincipal Account account,
                       Authentication authentication,
                       Principal principal) throws Exception {
    return "user/mypage";
  }

  @GetMapping("/order")
  public String order() {
    userService.order();
    return "user/mypage";
  }

}
