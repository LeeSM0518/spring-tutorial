# 로그아웃 및 화면 보안 처리

* **로그아웃 방법**
  * \<form> 태그를 사용해서 POST 요청
  * \<a> 태그를 사용해서 GET으로 요청 - **SecurityContextLogoutHandler** 활용

* **인증 여부에 따라 로그인/로그아웃 표현**

  * \<li **sec:authorize="isAnonymous()"** > \<a th:href="@{/login}"> 로그인 \</a>\</li>
  * \<li **sec:authorize="isAuthenticated()"** > \<a th:href="@{/logout}"> 로그아웃 \</a>\</li>

* **예시**

  ```java
  @GetMapping(value = "/logout")
  public String logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
    return "redirexct:/login";
  }
  ```

<br>

## 실제 적용 코드

* **LoginController**

  ```java
  @Controller
  public class LoginController {
  
    @GetMapping("/login")
    public String login() {
      return "login";
    }
  
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
  
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
  
      if (authentication != null) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
      }
  
      return "redirect:/login";
    }
  
  }
  ```

