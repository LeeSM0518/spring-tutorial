# CustomAuthenticationSuccessHandler

: 로그인 완료 후에 후속 작업을 처리하는 핸들러

<br>

## 설정 방법

* **SecurityConfig**

  ```java
  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.formLogin().successHandler(CustomAuthenticationSuccessHandler());
  }
  ```

* **CustomAuthenticationSuccessHandler**

  ```java
  @Override
  public void onAuthenticationSuccess(
  HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
    // 요청 캐시와 관련된 작업
    RequestCache requestCache = new HttpSessionRequestCache(); 
    // 세션 관련 작업
    final HttpSession session = request.getSession(false);
    // 인증된 사용자 관련 작업
    Object principal = authentication.getPrincipal();
    // 인증 성공 후 이동
    redirectStrategy.sendRedirect(request, response, targetUrl);
  }
  ```

<br>

## 실제 코드

* **CustomAuthenticationSuccessHandler**

  ```java
  @Component
  // 사용자가 인증에 성공했을 때 처리해줄 핸들러
  public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
  
    private RequestCache requestCache = new HttpSessionRequestCache();
  
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
  
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
  
      setDefaultTargetUrl("/");
  
      // 사용자가 인증하기 전에 요청했던 정보
      SavedRequest savedRequest = requestCache.getRequest(request, response);
      if (savedRequest != null) {
        String targetUrl = savedRequest.getRedirectUrl();
        redirectStrategy.sendRedirect(request, response, targetUrl);
      } else {
        redirectStrategy.sendRedirect(request, response, getDefaultTargetUrl());
      }
    }
  }
  ```

* **SecurityConfig**

  ```java
  @Configuration
  @EnableWebSecurity
  @Slf4j
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    @Autowired
    private AuthenticationSuccessHandler customAuthenticationSuccessHandler;
  
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
          .loginPage("/login")                 // 로그인 페이지
          .loginProcessingUrl("/login_proc")   // 로그인 처리 URL
          .authenticationDetailsSource(authenticationDetailsSource) // ID, PW 이외의 추가 인증
          .defaultSuccessUrl("/")              // 기본 URL
          .successHandler(customAuthenticationSuccessHandler) // 인증 성공 후처리 핸들러
          .permitAll();                        // 누구든지 인증
    }
  
  }
  ```