# FilterInvocationSecurityMetadataSource

![image](https://user-images.githubusercontent.com/43431081/90327060-be75cb00-dfca-11ea-9c62-da14415f0d54.png)

* 사용자가 접근하고자 하는 Url 자원에 대한 권한 정보 추출
* AccessDecisionManager 에게 전달하여 인가처리 수행
* DB로부터 자원 및 권한 정보를 매핑하여 맵으로 관리
* 사용자의 매 요청마다 요청정보에 매핑된 권한 정보 확인

<br>

![image](https://user-images.githubusercontent.com/43431081/90327082-f3821d80-dfca-11ea-9980-977b73263b15.png)

<br>

## 사용 예시

```java
// 필터 적용
http.addFilterAt(filterSecurityInterceptor(), FilterSecurityInterceptor.class);

@Bean
public FilterSecurityIntercepto filterSecurityInterceptor() {
  FilterSecurityInterceptor filterSecurityInterceptor =
    new FilterSecurityInterceptor();
  FilterSecurityInterceptor.setAuthenticationManger(authenticationManager);
  filterSecurityInterceptor.setSecurityMetadataSource(
    urlFIlterInvocationSecurityMetadataSource());
  filterSecurityInterceptor.setAccessDecisionManager(accessDecisionManager);
  return filterSecurityInterceptor;
}

@Bean
public FilterInvocationSecurityMetadataSource 
  urlFilterInvocationSecurityMetadataSource() {
  return new UrlFilterInvocationSecurityMetadataSource();
}
```

<br>

## 처리 과정

![image](https://user-images.githubusercontent.com/43431081/90327147-95096f00-dfcb-11ea-8ba0-c77cc60c3e20.png)

<br>

## 실제 코드

* **UrlFilterInvocationSecurityMetadataSource**

  ```java
  public class UrlFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
  
    private LinkedHashMap<RequestMatcher, List<ConfigAttribute>> requestMap = new LinkedHashMap<>();
  
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
  
      HttpServletRequest request = ((FilterInvocation) object).getRequest();
  
      // 직접 권한을 넣어줬는데, DB와 연동할 곳
      requestMap.put(new AntPathRequestMatcher("/mypage"), Arrays.asList(new SecurityConfig("ROLE_USER")));
  
      // 해당 URL의 권한 리스트를 리턴하는것
      if (requestMap != null) {
        return requestMap.entrySet().stream()
            .filter(entry -> entry.getKey().matches(request))
            .findFirst()
            .map(Map.Entry::getValue)
            .orElseGet(ArrayList::new);
      }
  
      return null;
    }
  
    @Override
    // 모든 권한 목록을 가져온다
    public Collection<ConfigAttribute> getAllConfigAttributes() {
      return requestMap.values().stream()
          .flatMap(Collection::stream)
          .collect(Collectors.toSet());
    }
  
    @Override
    public boolean supports(Class<?> clazz) {
      return FilterInvocation.class.isAssignableFrom(clazz);
    }
  
  }
  ```

* **SecurityConfig**

  ```java
  @Configuration
  @EnableWebSecurity
  @Slf4j
  @Order(1)
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    ...
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
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
  
      http
        // 새로운 인가 필터 적용
          .addFilterBefore(customFilterSecurityInterceptor(), FilterSecurityInterceptor.class);
    }
  
    @Bean
    public FilterInvocationSecurityMetadataSource urlFilterInvocationSecurityMetadataSource() {
      return new UrlFilterInvocationSecurityMetadataSource();
    }
  
    @Bean
    public AccessDecisionManager affirmativeBased() {
      AffirmativeBased affirmativeBased = new AffirmativeBased(getAccessDecisionVoters());
      return affirmativeBased;
    }
  
    private List<AccessDecisionVoter<?>> getAccessDecisionVoters() {
      return Arrays.asList(new RoleVoter());
    }
  
    @Bean
    public FilterSecurityInterceptor customFilterSecurityInterceptor() throws Exception {
      FilterSecurityInterceptor filterSecurityInterceptor = new FilterSecurityInterceptor();
      filterSecurityInterceptor.setSecurityMetadataSource(urlFilterInvocationSecurityMetadataSource());
      /**
       * affirmativeBased : 하나라도 승인이 있으면 승인 처리
       * ConsensusBased   : 승인과 거부의 개수를 따져서 다수결 쪽으로 처리
       * UnanimousBased   : 하나라도 거부가 있으면 거부 처리
       */
      filterSecurityInterceptor.setAccessDecisionManager(affirmativeBased());
      filterSecurityInterceptor.setAuthenticationManager(authenticationManagerBean());
      return filterSecurityInterceptor;
    }
  
  }
  ```

  