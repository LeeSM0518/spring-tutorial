# CustomAuthenticationFailureHandler

: 로그인 실패 후에 후속 작업을 처리하는 핸들러

<br>

## 설정 예시

* **SecurityConfig**

  ```java
  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.formLogin().failureHandler(CustomAuthenticationFailureHandler());
  }
  ```

* **CustomAuthenticationFailureHandler**

  ```java
  @Override
  public void onAuthenticationFailure(HttpServletRequest request,
                                     HttpServletResponse response,
                                     AuthenticationException exception) 
    throws IOException {
    if (exception instanceof UsernameNotFoundException) {
      errorMessage = message.getMessage("사용자가 존재하지 않습니다.", null, locale);
    } else if (exception instanceof BadCredentialsException) {
      errorMessage = message.getMessage("아이디 혹은 비밀번호가 일치하지 않습니다.", null, locale);
    } else {
      errorMessage = "인증에 실패했습니다. 웹 마스터에게 문의하십시오.!";
    }
  }
  ```

<br>

## 실제 코드

* **CustomAuthenticationFailureHandler**

  ```java
  @Component
  public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
  
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
      String errorMessage = "Invalid Usernamd or Password";
  
      if (exception instanceof BadCredentialsException) {
        errorMessage = "Invalid Username or Password";
      } else if (exception instanceof InsufficientAuthenticationException) {
        errorMessage = "Invalid Secret key";
      }
      setDefaultFailureUrl("/login?error=true&exception=" + errorMessage);
  
      super.onAuthenticationFailure(request, response, exception);
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
    private AuthenticationFailureHandler customAuthenticationFailureHandler;
    
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
          .failureHandler(customAuthenticationFailureHandler) // 인증 실패 후처리 핸들러
          .permitAll();                        // 누구든지 인증
    }
  
  }
  ```