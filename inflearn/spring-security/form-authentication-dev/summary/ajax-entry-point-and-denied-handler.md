# AjaxLoginUrlAuthenticationEntryPoint & AjaxAccessDeniedHandler

* **AjaxLoginUrlAuthenticationEntryPoint**

  * 인증을 받지 않은 사용자가 자원에 접근했을 때 처리하는 클래스

  * ExceptionTranslationFilter 에서 인증 예외 시 호출

  * AuthenticationEntryPoint 인터페이스 구현

  * 인증 오류 메시지와 401 상태 코드 반환

    ```java
    response.sendError(HttpServletResponse.SC_UNAYTHORIZED, "Unauthorized");
    ```

* **AjaxAccessDeniedHandler**

  * 인증은 되었는데, 권한이 없는 사용자가 접근했을 때 처리하는 클래스

  * ExceptionTranslationFilter 에서 인가 예외 시 호출

  * AccessDeniedHandler 인터페이스 구현

  * 인가 오류 메시지와 403 상태 코드 반환

    ```java
    response.sendError(HttpServletResponse.SC_FORBIDDEN, "forbidden");
    ```

<br>

## 실제 코드

* **AjaxLoginAuthenticationEntryPoint**

  ```java
  public class AjaxLoginAuthenticationEntryPoint implements AuthenticationEntryPoint {
  
    @Override
    // 익명 사용자나 권한이 없는 사용자가 자원에 접근했을 때 호출되는 메소드
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UnAuthorized");
    }
  
  }
  ```

* **AjaxAccessDeniedHandler**

  ```java
  public class AjaxAccessDeniedHandler implements AccessDeniedHandler {
  
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access is denied");
    }
  
  }
  ```

* **a**

  ```java
  @Configuration
  @Order(0) // 설정 순서 결정, 0: 가장 먼저 설정을 한다.
  public class AjaxSecurityConfig extends WebSecurityConfigurerAdapter {
  
    ...
  
      @Bean
      public AjaxLoginAuthenticationEntryPoint ajaxLoginAuthenticationEntryPoint() {
      return new AjaxLoginAuthenticationEntryPoint();
    }
  
    @Bean
    public AjaxAccessDeniedHandler ajaxAccessDeniedHandler() {
      return new AjaxAccessDeniedHandler();
    }
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
        .antMatcher("/api/**")
        .authorizeRequests()
        .antMatchers("/api/messages").hasRole("MANAGER")
        .anyRequest().authenticated();
      
      http
        .addFilterBefore(ajaxLoginProcessFilter(), UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling()
        // 인증 예외 처리 핸들러 등록
        .authenticationEntryPoint(ajaxLoginAuthenticationEntryPoint())
        // 인가 예외 처리 핸들러 등록
        .accessDeniedHandler(ajaxAccessDeniedHandler());
      http.csrf().disable();
    }
  
    @Bean
    public AjaxLoginProcessFilter ajaxLoginProcessFilter() throws Exception {
      AjaxLoginProcessFilter ajaxLoginProcessFilter = new  AjaxLoginProcessFilter();
      ajaxLoginProcessFilter.setAuthenticationManager(authenticationManagerBean());
      ajaxLoginProcessFilter.setAuthenticationSuccessHandler(ajaxAuthenticationSuccessHandler());
      ajaxLoginProcessFilter.setAuthenticationFailureHandler(ajaxAuthenticationFailureHandler());
      return ajaxLoginProcessFilter;
    }
  
  }
  ```