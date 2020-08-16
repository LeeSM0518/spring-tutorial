package io.security.basicsecurity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class SecurityController {

  @GetMapping("/")
  public String index() {
    return "home";
  }

  @GetMapping("/user")
  public String user() {
    return "user";
  }

  @GetMapping("/admin/pay")
  public String adminPay() {
    return "adminPay";
  }

  @GetMapping("/admin/**")
  public String admin() {
    return "admin";
  }

  @GetMapping("denied")
  public String denied() {
    return "Access is denied";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/test")
  public String test1(HttpSession session) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    SecurityContext context =
        (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
    Authentication authentication1 = context.getAuthentication();

    System.out.println("======== 1 ==========");
    System.out.println(authentication);
    System.out.println("======== 2 ==========");
    System.out.println(authentication1);

    return "test";
  }

  @GetMapping("/test2")
  public String test2() {
    new Thread(() -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      System.out.println("========= 3 =========");
      System.out.println(authentication); // null 이다
      // 자식 스레드에도 동일한 SecurityContext를 유지하려면
      //  SecurityConfig 에서 SecurityContextHolder.setStrategyName(MODE_INHERITTABLETHREADLOCAL) 을
      //  호출해야 한다.
    }).start();
    return "test2";
  }

}
