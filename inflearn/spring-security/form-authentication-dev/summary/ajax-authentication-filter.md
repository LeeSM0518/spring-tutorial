# AjaxAuthenticationFilter

## 구현 설명

* **AbstractAuthenticationProcessingFilter 상속**
* **필터 작동 조건**
  * AntPathRequestMatcher("/api/login") 로 요청정보와 매칭하고 요청 방식이 Ajax 이면 필터 작동
* **AjaxAuthenticationToken 생성하여 AuthenticationManager 에게 전달하여 인증처리**
* **Filter 추가**
  * http.addFilterBefore(AjaxAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
    * `UsernamePasswordAuthenticationFilter` 를 거치기 전에 `AjaxAuthenticationFilter` 를 먼저 거칠 수 있도록 한다.

<br>

## 실제 코드

### 설정 코드

* **AjaxLoginProcessFilter**

  ```java
  public class AjaxLoginProcessFilter extends AbstractAuthenticationProcessingFilter {
  
    private ObjectMapper objectMapper = new ObjectMapper();
  
    public AjaxLoginProcessFilter() {
      // 로그인 요청 URL 설정
      super(new AntPathRequestMatcher("/api/login"));
    }
  
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
        throws AuthenticationException, IOException, ServletException {
      // 요청이 Ajax 요청인지 점검
      if (!isAjax(request)) {
        throw new IllegalStateException("Authentication is not supported");
      }
  
      AccountDto accountDto = objectMapper.readValue(request.getReader(), AccountDto.class);
  
      if (StringUtils.isEmpty(accountDto.getUsername()) || StringUtils.isEmpty(accountDto.getPassword())) {
        throw new IllegalArgumentException("Username or Password is empty");
      }
  
      AjaxAuthenticationToken ajaxAuthenticationToken =
          new AjaxAuthenticationToken(accountDto.getUsername(), accountDto.getPassword());
  
      return getAuthenticationManager().authenticate(ajaxAuthenticationToken);
    }
  
    private boolean isAjax(HttpServletRequest request) {
      return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
  
  }
  ```

* **AjaxAuthenticationToken**

  ```java
  public class AjaxAuthenticationToken extends AbstractAuthenticationToken {
  
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
  
    private final Object principal;
    private Object credentials;
  
    // 인증을 받기 전에 사용자가 입력한 아이디, 패스워드를 담는다.
    public AjaxAuthenticationToken(Object principal, Object credentials) {
      super(null);
      this.principal = principal;
      this.credentials = credentials;
      setAuthenticated(false);
    }
  
    // 인증된 이후의 결과를 담는다.
    public AjaxAuthenticationToken(Object principal, Object credentials,
                                               Collection<? extends GrantedAuthority> authorities) {
      super(authorities);
      this.principal = principal;
      this.credentials = credentials;
      super.setAuthenticated(true); // must use super, as we override
    }
  
    public Object getCredentials() {
      return this.credentials;
    }
  
    public Object getPrincipal() {
      return this.principal;
    }
  
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
      if (isAuthenticated) {
        throw new IllegalArgumentException(
            "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
      }
  
      super.setAuthenticated(false);
    }
  
    @Override
    public void eraseCredentials() {
      super.eraseCredentials();
      credentials = null;
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
    public AccessDeniedHandler accessDeniedHandler() {
      CustomAccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler();
      accessDeniedHandler.setErrorPage("/denied");
      return accessDeniedHandler;
    }
  
    @Bean
    public AjaxLoginProcessFilter ajaxLoginProcessFilter() throws Exception {
      AjaxLoginProcessFilter ajaxLoginProcessFilter = new AjaxLoginProcessFilter();
      ajaxLoginProcessFilter.setAuthenticationManager(authenticationManagerBean());
      return ajaxLoginProcessFilter;
    }
  
  }
  ```

<br>

### Ajax 테스트

1. `HTTP Request` 파일을 만든다.

2. 내용을 작성한다.

   ```java
   POST http://localhost:8080/api/login
   Content-Type: application/json
   X-Requested-With: XMLHttpRequest
   
   {
     "username": "asd",
     "password": "asd"
   }
   ```