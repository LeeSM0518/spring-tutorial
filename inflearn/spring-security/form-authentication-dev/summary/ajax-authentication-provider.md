# AjaxAuthenticationProvider

* **AuthenticationProvider 인터페이스 구현**
* **인증 작동 조건**
  * `supports(Class\<?> authentication)`
    * ProviderManager 로부터 넘어온 인증객체가 AjaxAuthenticationToken 타입이면 작동
* **인증 검증이 완료되면 AjaxAuthenticationToken 생성하여 최종 인증 객체 반환**

<br>

## 실제 코드

* **AjaxAuthenticationProvider**

  ```java
  public class AjaxAuthenticationProvider implements AuthenticationProvider {
  
    @Autowired
    private UserDetailsService userDetailsService;
  
    @Autowired
    private PasswordEncoder passwordEncoder;
  
    @Override
    // 인증에 관련된 모든 검증하는 메서드
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
  
      String username = authentication.getName();
      String password = (String) authentication.getCredentials();
  
      // ID 검증 (해당 ID로 회원이 존재하는지 확인)
      AccountContext accountContext = (AccountContext) userDetailsService.loadUserByUsername(username);
  
      // PW 검증 (사용자로부터 입력된 PW, DB로부터 가져온 PW)
      if (!passwordEncoder.matches(password, accountContext.getAccount().getPassword())) {
        throw new BadCredentialsException("BadCredentialsException");
      }
  
      /**
       * public UsernamePasswordAuthenticationToken(Object principal, Object credentials,
       * 			Collection<? extends GrantedAuthority> authorities)
       * @param principle   : 인증에 사용한 사용자 객체(Account)
       * @param credentials : PW 정보
       * @param authorities : 권한 정보들
       */
      // 인증 객체 생성
      return new AjaxAuthenticationToken(accountContext.getAccount(), null, accountContext.getAuthorities());
    }
  
    @Override
    // authentication 파라미터의 클래스 타입과 인증 토큰의 타입이 일치하는지 확인
    public boolean supports(Class<?> authentication) {
      return authentication.equals(AjaxAuthenticationToken.class);
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
      AjaxLoginProcessFilter ajaxLoginProcessFilter = new AjaxLoginProcessFilter();
      ajaxLoginProcessFilter.setAuthenticationManager(authenticationManagerBean());
      return ajaxLoginProcessFilter;
    }
  
  }
  ```

* **ajax.http 로 API 호출**

  ```java
  POST http://localhost:8080/api/login
  
  HTTP/1.1 302 
  X-Content-Type-Options: nosniff
  X-XSS-Protection: 1; mode=block
  Cache-Control: no-cache, no-store, max-age=0, must-revalidate
  Pragma: no-cache
  Expires: 0
  X-Frame-Options: DENY
  Location: http://localhost:8080/
  Content-Length: 0
  Date: Sat, 15 Aug 2020 14:35:28 GMT
  Keep-Alive: timeout=60
  Connection: keep-alive
  
  <Response body is empty>
  
  Response code: 302; Time: 99ms; Content length: 0 bytes
  
  Cookies are preserved between requests:
  > /Users/sangminlee/spring-tutorial/inflearn/spring-security/form-authentication-dev/.idea/httpRequests/http-client.cookies
  ```

  * 위와 같은 결과가 나오는 이유는 현재 로그인을 성공하면 리다이렉트되어 루트 페이지로 넘어가는데, 이것은 API 호출이기 때문에 이전과는 다르게 body를 통해 응답이 와야한다.