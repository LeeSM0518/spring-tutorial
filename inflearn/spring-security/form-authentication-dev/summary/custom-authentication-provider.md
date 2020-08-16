# CustomAuthenticationProvider

**AuthenticationProvider** : `AccountContext` 를 제공받아서 추가적인 인증 작업을 한다.

<br>

## 인증 처리 과정

![image](https://user-images.githubusercontent.com/43431081/90112260-b7607a00-dd8a-11ea-928e-e6f20c104fb0.png)

<br>

### AuthenticationProvider 인증 처리 커스텀 코드

```java
public Authentication authenticate(Authentication auth) throws AuthenticationException {
  
  String loginId = auth.getName();
  String passwd = (String) auth.getCredentials();
  
  // ID 검증
  UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
  
  // PW 검증
  if (userDetails == null || !passwordEncoder.matches(passwd, userDetails.getPassword())) {
    throw new BadCredentialsException("Invalid password");
  }
  
  return new UsernamePasswordAuthenticationToken(
    userDetials.getUser(), 
    null,
    userDetails.getAuthorities());
  
}
```

<br>

## 전체 코드 정리

* **SecurityConfig**

  ```java
  @Configuration
  @EnableWebSecurity
  @Slf4j
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    /**
     * 커스텀 UserDetailService 를 사용하는
     * 커스텀 AuthenticationProvider 을 새롭게 만들었기 때문에,
     * 지워도 된다.
     */
  //  @Autowired
  //  private UserDetailsService userDetailsService;
  
    @Override
    // 커스텀 인증 처리 서비스 등록
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
  //    auth.userDetailsService(userDetailsService);
      // 커스텀 AuthenticationProvider 적용
      auth.authenticationProvider(authenticationProvider());
    }
  
    @Bean
    public AuthenticationProvider authenticationProvider() {
      return new CustomAuthenticationProvider();
    }
  
    ...
  
  }
  ```

* **CustomAuthenticationProvider**

  ```java
  public class CustomAuthenticationProvider implements AuthenticationProvider {
  
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
      UsernamePasswordAuthenticationToken authenticationToken
          = new UsernamePasswordAuthenticationToken(
          accountContext.getAccount(),
          null,
          accountContext.getAuthorities());
  
      return authenticationToken;
    }
  
    @Override
    // authentication 파라미터의 클래스 타입과 인증 토큰의 타입이 일치하는지 확인
    public boolean supports(Class<?> authentication) {
      return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
  
  }
  ```

  