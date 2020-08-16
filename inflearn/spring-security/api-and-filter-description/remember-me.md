# Remember Me 인증

![image](https://user-images.githubusercontent.com/43431081/89966581-6ff2c480-dc8a-11ea-9cfc-4fff7698a98c.png)

![image](https://user-images.githubusercontent.com/43431081/89966748-d1b32e80-dc8a-11ea-90c8-2fbf4a38d6ed.png)

<br>

## Remember Me 필터 적용

* **SecurityConfig.java**

  ```java
  package io.security.basicsecurity;
  
  ...
  
  @Configuration
  @EnableWebSecurity
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    @Autowired
    private UserDetailsService userDetailsService;
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
          .anyRequest().authenticated()
          ...
          .and()
          // remember me 필터 구성
          .rememberMe()
          .rememberMeParameter("remember")            // 파라미터 명
          .tokenValiditySeconds(3600)
          .userDetailsService(userDetailsService);
  
    }
  
  }
  ```