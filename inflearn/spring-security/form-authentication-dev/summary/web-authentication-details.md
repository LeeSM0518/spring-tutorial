# WebAuthenticationDetails & AuthenticationDetailsSource

* **WebAuthenticationDetails**
  * ID, PW 이외의 데이터들도 같이 인증을 처리하도록 하는 클래스
* **AuthenticationDetailsSource**
  * `WebAuthenticationDetails` 를 생성하는 클래스

<br>

![image](https://user-images.githubusercontent.com/43431081/90309409-5ae49280-df23-11ea-9e5f-660e91df923c.png)

<br>

## 코드

* **FormAuthenticationDetailsSource.java**

  ```java
  package securitytutorial.tutorial.security.common;
  
  import org.springframework.security.authentication.AuthenticationDetailsSource;
  import org.springframework.security.web.authentication.WebAuthenticationDetails;
  import org.springframework.stereotype.Component;
  
  import javax.servlet.http.HttpServletRequest;
  
  @Component
  public class FormAuthenticationDetailsSource implements
      AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {
  
    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
      return new FormWebAuthenticationDetails(context);
    }
  
  }
  ```

* **FormWebAuthenticationDetails**

  ```java
  package securitytutorial.tutorial.security.common;
  
  import org.springframework.security.web.authentication.WebAuthenticationDetails;
  
  import javax.servlet.http.HttpServletRequest;
  
  public class FormWebAuthenticationDetails extends WebAuthenticationDetails {
  
    private String secretKey;
  
    public FormWebAuthenticationDetails(HttpServletRequest request) {
      super(request);
      secretKey = request.getParameter("secret_key");
    }
  
    public String getSecretKey() {
      return secretKey;
    }
  
  }
  ```

* **SecurityConfig**

  ```java
  package securitytutorial.tutorial.security.configs;
  
  import lombok.extern.slf4j.Slf4j;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.authentication.AuthenticationDetailsSource;
  import org.springframework.security.authentication.AuthenticationProvider;
  import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.config.annotation.web.builders.WebSecurity;
  import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
  import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
  import org.springframework.security.crypto.factory.PasswordEncoderFactories;
  import org.springframework.security.crypto.password.PasswordEncoder;
  import securitytutorial.tutorial.security.provider.CustomAuthenticationProvider;
  
  @Configuration
  @EnableWebSecurity
  @Slf4j
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    ...
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
          .antMatchers("/", "/users", "user/login/**").permitAll()
          .antMatchers("/mypage").hasRole("USER")
          .antMatchers("/manages").hasRole("MANAGER")
          .antMatchers("/config").hasRole("ADMIN")
          .anyRequest().authenticated()
      .and()
          .formLogin()
          .loginPage("/login")               
          .loginProcessingUrl("/login_proc") 
        // authenticationDetailsSource 등록
          .authenticationDetailsSource(authenticationDetailsSource)
          .defaultSuccessUrl("/")            
          .permitAll();                      
    }
  
  }
  ```

  