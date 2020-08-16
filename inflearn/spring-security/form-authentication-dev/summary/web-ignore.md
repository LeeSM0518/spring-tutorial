# WebIgnore 설정

**js / css / image 파일 등 보안 필터를 적용할 필요가 없는 리소스를 설정**

<br>

* **설정 방법**

  ```java
  package securitytutorial.tutorial.security.configs;
  
  ...
  
  @Configuration
  @EnableWebSecurity
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    ...
  
    @Override
    public void configure(WebSecurity web) throws Exception {
      // 정적 파일들 요청하는 것은 보안 필터를 적용하지 않는다.
      web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
  
    ...
  
  }
  
  ```

