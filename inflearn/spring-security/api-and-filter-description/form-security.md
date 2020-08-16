# Form 인증

## 이론

![image](https://user-images.githubusercontent.com/43431081/89963552-e4c20080-dc82-11ea-8922-951f5c8e788a.png)

![image](https://user-images.githubusercontent.com/43431081/89963773-7df11700-dc83-11ea-809b-eaccbf41b81d.png)

<br>

## 커스텀 보안 구성

* **SecurityConfig**

  ```java
  package io.security.basicsecurity;
  
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
  import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
  
  @Configuration
  @EnableWebSecurity // 웹 보안 활성화
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    @Override
    // 인증, 인가 API 설정 및 보안 옵션 설정 가능
    protected void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
          .anyRequest().authenticated();   // 어떠한 요청에도 인증이 필요
  
      http
          .formLogin()
          .loginPage("/loginPage")
          .defaultSuccessUrl("/")
          .failureUrl("/login")
          .usernameParameter("userId")
          .passwordParameter("passwd")
          .loginProcessingUrl("/login_proc")
          .successHandler((request, response, authentication) -> {
            System.out.println("authentication: " + authentication.getName());
            response.sendRedirect("/");
          })
          .failureHandler((request, response, exception) -> {
            System.out.println("exception: " + exception.getMessage());
            response.sendRedirect("/login");
          }).permitAll();
    }
  
  }
  ```

* **SecurityController**

  ```java
  package io.security.basicsecurity;
  
  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RestController;
  
  @RestController
  public class SecurityController {
  
    @GetMapping("/")
    public String index() {
      return "home";
    }
  
    @GetMapping("loginPage")
    public String loginPage() {
      return "loginPage";
    }
  
  }
  
  ```