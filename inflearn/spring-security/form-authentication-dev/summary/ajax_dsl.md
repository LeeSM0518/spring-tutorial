# DSL로 Config 설정하기

* **DSL** : 도메인 특화 언어

* **Custom DSLs**

  * `AbstractHttpConfigurer`

    * 스프링 시큐리티 초기화 설정 클래스

    * 필터, 핸들러, 메서드, 속성 등을 한 곳에 정의하여 처리할 수 있는 편리함 제공

      ```java
      public void init(H http) throws Exception // 초기화
      public void configure(H http) // 설정
      ```

  * `HttpSecurity` 의 `apply(C configurer)` 메서드 사용

    ```java
    @Configuration
    @EnableWebSecurity
    public class SecurityConfig extends WebSecurityConfigurerAdapter {
      @Override
      protected void configure(HttpSecurity http) throws Exception {
        http
          .exceptionHandling()
          .authenticationEntryPoint(
          new AjaxLoginUrlAuthenticationEntryPoint())
          .and()
          .apply(new AjaxLoginConfigurer<>())
          .successHandlerAjax(ajaxAuthenticationSuccessHandler)
          .failureHandlerAjax(ajaxAuthenticationFailureHandler)
          .loginProcessingUrl("/ajaxLogin")
          .setAuthenticationManager(ajaxAuthenticationManger())
          .readAndWriteMapper(objectMapper);
      }
    }
    ```

<br>

## 실제 코드

* **AjaxLoginConfigurer**

  ```java
  public final class AjaxLoginConfigurer<H extends HttpSecurityBuilder<H>> extends
      AbstractAuthenticationFilterConfigurer<H, AjaxLoginConfigurer<H>, AjaxLoginProcessFilter> {
  
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;
    private AuthenticationManager authenticationManager;
  
    public AjaxLoginConfigurer() {
      super(new AjaxLoginProcessFilter(), null);
    }
  
    @Override
    public void init(H http) throws Exception {
      super.init(http);
    }
  
    @Override
    public void configure(H http) throws Exception {
      if (authenticationManager == null) {
        // AuthenticationManger 공유 객체를 가져온다.
        authenticationManager = http.getSharedObject(AuthenticationManager.class);
      }
  
      // 필터 설정
      getAuthenticationFilter().setAuthenticationManager(authenticationManager);
      getAuthenticationFilter().setAuthenticationSuccessHandler(successHandler);
      getAuthenticationFilter().setAuthenticationFailureHandler(failureHandler);
  
      SessionAuthenticationStrategy sessionAuthenticationStrategy =
          http.getSharedObject(SessionAuthenticationStrategy.class);
      if (sessionAuthenticationStrategy != null) {
        getAuthenticationFilter().setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
      }
  
      RememberMeServices rememberMeServices = http.getSharedObject(RememberMeServices.class);
  
      if (rememberMeServices != null) {
        getAuthenticationFilter().setRememberMeServices(rememberMeServices);
      }
  
      http.setSharedObject(AjaxLoginProcessFilter.class, getAuthenticationFilter());
      http.addFilterBefore(getAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
  
    public AjaxLoginConfigurer<H> setSuccessHandler(AuthenticationSuccessHandler successHandler) {
      this.successHandler = successHandler;
      return this;
    }
  
    public AjaxLoginConfigurer<H> setFailureHandler(AuthenticationFailureHandler failureHandler) {
      this.failureHandler = failureHandler;
      return this;
    }
  
    public AjaxLoginConfigurer<H> setAuthenticationManager(AuthenticationManager authenticationManager) {
      this.authenticationManager = authenticationManager;
      return this;
    }
  
    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
      return new AntPathRequestMatcher(loginProcessingUrl, "POST");
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
    public AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler() {
      return new AjaxAuthenticationSuccessHandler();
    }
  
    @Bean
    public AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler() {
      return new AjaxAuthenticationFailureHandler();
    }
  
    @Bean
    public AuthenticationProvider ajaxAuthenticationProvider() {
      return new AjaxAuthenticationProvider();
    }
  
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
  //        .addFilterBefore(ajaxLoginProcessFilter(), UsernamePasswordAuthenticationFilter.class)
          .exceptionHandling()
          .authenticationEntryPoint(ajaxLoginAuthenticationEntryPoint())
          .accessDeniedHandler(ajaxAccessDeniedHandler());
      http.csrf().disable(); // CSRF 기능 X
  
      customConfigurerAjax(http);
    }
  
    private void customConfigurerAjax(HttpSecurity http) throws Exception {
      http
          .apply(new AjaxLoginConfigurer<>())
          .setSuccessHandler(ajaxAuthenticationSuccessHandler())
          .setFailureHandler(ajaxAuthenticationFailureHandler())
          .setAuthenticationManager(authenticationManager())
          .loginProcessingUrl("/api/login");
    }
  
  //  @Bean
  //  public AjaxLoginProcessFilter ajaxLoginProcessFilter() throws Exception {
  //    AjaxLoginProcessFilter ajaxLoginProcessFilter = new  AjaxLoginProcessFilter();
  //    ajaxLoginProcessFilter.setAuthenticationManager(authenticationManagerBean());
  //    ajaxLoginProcessFilter.setAuthenticationSuccessHandler(ajaxAuthenticationSuccessHandler());
  //    ajaxLoginProcessFilter.setAuthenticationFailureHandler(ajaxAuthenticationFailureHandler());
  //    return ajaxLoginProcessFilter;
  //  }
  
  }
  ```

  