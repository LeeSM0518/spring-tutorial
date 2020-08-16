# Access Denied

: 자원 접근의 권한이 없을 때, 인가 예외를 처리하기 위함

<br>

## 설정 예시

* **SecurityConfig**

  ```java
  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.exceptionHandling().accessDeniedPage("/accessDenied")
      .accessDeniedHandler(accessDeniedHandler);
  }
  ```

* **AccessDeniedHandler**

  ```java
  @Override
  public void handle(HttpServlerRequest request,
                    HttpServletResponse response,
                    AccessDeniedException accessDeniedException)
    throws IOException, SerlvetException {
    String deniedUrl = errorPage + "?exception=" + 
      accessDeniedException.getMessage();
    response.sendRedirect(request, response, deniedUrl);
  }
  
  public void setErrorPage(String errorPage) {
    this.errorPage = errorPage;
  }
  ```

<br>

## 실제 코드

* **CustomAccessDeniedHandler**

  ```java
  public class CustomAccessDeniedHandler implements AccessDeniedHandler {
  
    private String errorPage;
  
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
      String deniedUrl = errorPage + "?exception=" + accessDeniedException.getMessage();
      response.sendRedirect(deniedUrl);
    }
  
    public void setErrorPage(String errorPage) {
      this.errorPage = errorPage;
    }
  
  }
  ```

* **SecurityConfig**

  ```java
  @Configuration
  @EnableWebSecurity
  @Slf4j
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    ...
      
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
      CustomAccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler();
      accessDeniedHandler.setErrorPage("/denied");
      return accessDeniedHandler;
    }
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
          /**
           * "/login*" : 로그인 URL에 query param 을 넘기기 위함
           */
          .antMatchers("/", "/users", "user/login/**", "/login*").permitAll()
          .antMatchers("/mypage").hasRole("USER")
          .antMatchers("/messages").hasRole("MANAGER")
          .antMatchers("/config").hasRole("ADMIN")
          .anyRequest().authenticated()
      .and()
          .formLogin()
          .loginPage("/login")                 // 로그인 페이지
          .loginProcessingUrl("/login_proc")   // 로그인 처리 URL
          .authenticationDetailsSource(authenticationDetailsSource) // ID, PW 이외의 추가 인증
          .defaultSuccessUrl("/")              // 기본 URL
          .successHandler(customAuthenticationSuccessHandler) // 인증 성공 후처리 핸들러
          .failureHandler(customAuthenticationFailureHandler) // 인증 실패 후처리 핸들러
          .permitAll();                        // 누구든지 인증
  
      http
          .exceptionHandling()
          .accessDeniedHandler(accessDeniedHandler()); // 권한 인증 실패시 처리할 핸들러
    }
  
  }
  ```