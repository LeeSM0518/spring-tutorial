# AjaxAuthenticationSuccessHandler & AjaxAuthenticationFailureHandler

* **AjaxAuthenticationSuccessHandler**

  * AuthenticationSuccessHandler 인터페이스 구현

  * Response Header 설정

    ```java
    response.setStatus(HttpStatus.OK.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ```

  * JSON 형식으로 변환하여 인증 객체 리턴 함

    ```java
    objectMapper.writeValue(response.getWriter(), ResponseBody.ok(userDto));
    ```

* **AjaxAuthenticationFailureHandler**

  * AuthenticationFailureHandler 인터페이스 구현

  * Response Header 설정

    ```java
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContetnType(MediaType.APPLICATION_JSON_VALUE);
    ```

  * JSON 형식으로 변환하여 오류 메시지 리턴 함

    ```java
    objectMapper.writeValue(response.getWriter(), ResponseBody.error(message));
    ```

<br>

## 실제 코드

* **AjaxAuthenticationSuccessHandler**

  ```java
  public class AjaxAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
  
    private ObjectMapper objectMapper = new ObjectMapper();
  
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
      Account account = (Account) authentication.getPrincipal();
  
      response.setStatus(HttpStatus.OK.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
  
      objectMapper.writeValue(response.getWriter(), account);
    }
  
  }
  ```

* **AjaxAuthenticationFailureHandler**

  ```java
  public class AjaxAuthenticationFailureHandler implements AuthenticationFailureHandler {
  
    private ObjectMapper objectMapper = new ObjectMapper();
  
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
      String errorMessage = "Invalid Username or Password";
  
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
  
      if (exception instanceof BadCredentialsException) {
        errorMessage = "Invalid Username or Password";
      } else if (exception instanceof InsufficientAuthenticationException) {
        errorMessage = "Invalid Secret key";
      } else if (exception instanceof CredentialsExpiredException) {
        errorMessage = "Expired password";
      }
  
      objectMapper.writeValue(response.getWriter(), errorMessage);
    }
  
  }
  ```

* **AjaxSecurityConfig**

  ```java
  @Configuration
  @Order(0) // 설정 순서 결정, 0: 가장 먼저 설정을 한다.
  public class AjaxSecurityConfig extends WebSecurityConfigurerAdapter {
  
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.authenticationProvider(ajaxAuthenticationProvider());
    }
  
    @Bean
    // 인증 성공 핸들러 빈 등록
    public AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler() {
      return new AjaxAuthenticationSuccessHandler();
    }
  
    @Bean
    // 인증 실패 핸들러 빈 등록
    public AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler() {
      return new AjaxAuthenticationFailureHandler();
    }
  
    @Bean
    public AuthenticationProvider ajaxAuthenticationProvider() {
      return new AjaxAuthenticationProvider();
    }
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .antMatcher("/api/**")
          .authorizeRequests()
          .anyRequest().authenticated();
  
      /**
       * addFilter       : 가장 마지막에 필터가 존재해야 할 때
       * addFilterBefore : 추가하고자 하는 필터가 기존의 필터 앞에 존재해야 할 때
       * addFilterAfter  : 추가하고자 하는 필터가 기존의 필터 뒤에 존재해야 할 때
       * addFilterAt     : 기존의 필터 위치를 대체하고자 할 때 사용한다.
       */
      http
          .addFilterBefore(ajaxLoginProcessFilter(), UsernamePasswordAuthenticationFilter.class);
      http.csrf().disable(); // CSRF 기능 X
    }
  
    @Bean
    public AjaxLoginProcessFilter ajaxLoginProcessFilter() throws Exception {
      AjaxLoginProcessFilter ajaxLoginProcessFilter = new  AjaxLoginProcessFilter();
      ajaxLoginProcessFilter.setAuthenticationManager(authenticationManagerBean());
      ajaxLoginProcessFilter.setAuthenticationSuccessHandler(ajaxAuthenticationSuccessHandler()); // 인증 성공 핸들러 등록
      ajaxLoginProcessFilter.setAuthenticationFailureHandler(ajaxAuthenticationFailureHandler()); // 인증 실패 핸들러 등록
      return ajaxLoginProcessFilter;
    }
  
  }
  ```

* **ajax.http**

  ```java
  POST http://localhost:8080/api/login
  Content-Type: application/json
  X-Requested-With: XMLHttpRequest
  
  {
    "username": "asd",
    "password": "asd"
  }
  ```

* **실행 결과**

  ```
  POST http://localhost:8080/api/login
  
  HTTP/1.1 200 
  X-Content-Type-Options: nosniff
  X-XSS-Protection: 1; mode=block
  Cache-Control: no-cache, no-store, max-age=0, must-revalidate
  Pragma: no-cache
  Expires: 0
  X-Frame-Options: DENY
  Content-Type: application/json;charset=ISO-8859-1
  Content-Length: 162
  Date: Sat, 15 Aug 2020 15:35:22 GMT
  Keep-Alive: timeout=60
  Connection: keep-alive
  
  {
    "id": 2,
    "username": "asd",
    "password": "{bcrypt}$2a$10$I.uxFxkDLb49BMB5azp8s.thZb/rASqJxGszAmyBLjQhQ8zAy2QgG",
    "email": "asd@asd.asd",
    "age": "12",
    "role": "ROLE_MANAGER"
  }
  
  Response code: 200; Time: 98ms; Content length: 162 bytes
  ```