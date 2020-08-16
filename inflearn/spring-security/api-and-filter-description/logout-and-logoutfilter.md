# Logout 처리, LogoutFilter

![image](https://user-images.githubusercontent.com/43431081/89965684-52246000-dc88-11ea-8888-6074ead27f7a.png)

![image](https://user-images.githubusercontent.com/43431081/89965738-74b67900-dc88-11ea-9b8f-ae06bc554cbe.png)

<br>

## Logout 인증 설정

* **SecurityConfig**

  ```java
  package io.security.basicsecurity;
  
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
  import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
  
  import javax.servlet.http.HttpSession;
  
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
  //        .loginPage("/loginPage")
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
  
      http
          .logout()
          .logoutUrl("/logout")
          .logoutSuccessUrl("/login")
          .addLogoutHandler(((request, response, authentication) -> {
            HttpSession session = request.getSession();
            session.invalidate();
          }))
          .logoutSuccessHandler(((request, response, authentication) -> {
            response.sendRedirect("/login");
          }))
          .deleteCookies("remember-me");
  
    }
  
  }
  ```

  <br>

  ## Logout Filter

  ![image](https://user-images.githubusercontent.com/43431081/89966148-70d72680-dc89-11ea-9f8f-a7e79ef66d48.png)