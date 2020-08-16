# ExceptionTranslationFilter(인증/인가 예외)

## 예외들

* **AuthenticationException**
  * 인증 예외 처리
    1. *AuthenticationEntryPoint 호출*
       * 로그인 페이지 이동, 401 오류 코드 전달
    2. *인증 예외가 발생하기 전의 요청 정보를 저장*
       * RequestCache : 사용자의 이전 요청 정보를 세션에 저장하고 이를 꺼내 오는 캐시 메커니즘
         * SavedRequest : 사용자가 요청했던 request 파라미터 값들, 그 당시의 헤더 값들 등이 저장
* **AccessDeniedException**
  * 인가 예외 처리
    * *AccessDeniedHandler에서 예외 처리하도록 제공*

<br>

## 예외 처리 다이어그램

![image](https://user-images.githubusercontent.com/43431081/89981259-890d6c80-dcae-11ea-9cb6-815c981c02f7.png)

<br>

## ExceptionTranslationFilter 사용

![image-20200812151544278](../../../Library/Application Support/typora-user-images/image-20200812151544278.png)

<br>

## ExceptionTranslationFilter sequence

![image](https://user-images.githubusercontent.com/43431081/89981370-bb1ece80-dcae-11ea-8fe9-caba59989428.png)

<br>

## ExceptionTranslationFilter 사용 예제

* **SecurityConfig.java**

  ```java
  package io.security.basicsecurity;
  
  ...
  
  @Configuration
  @EnableWebSecurity
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
      // 계정을 메모리에 올린다.
      auth.inMemoryAuthentication()
          // {noop} : 패스워드 암호화 유형(noop: 평문 그대로)
          .withUser("user").password("{noop}1111").roles("USER");
      auth.inMemoryAuthentication()
          .withUser("sys").password("{noop}1111").roles("SYS");
      auth.inMemoryAuthentication()
          .withUser("admin").password("{noop}1111").roles("ADMIN");
    }
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
          .antMatchers("/login").permitAll() // 로그인 페이지는 인증을 허락해야 인증을 할 수 있다.
          .antMatchers("/user").hasRole("USER")
          .antMatchers("/admin/pay").hasRole("ADMIN")
          .antMatchers("/admin/**").access("hasRole('ADMIN') or hasRole('SYS')")
          .anyRequest().authenticated();
      http
          .formLogin()
          .successHandler(((request, response, authentication) -> {
            // 로그인 이전의 페이지에서의 request, response 를 가져와서 리다이렉트 하는 과정s
            HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            String redirectUrl = savedRequest.getRedirectUrl();
            response.sendRedirect(redirectUrl);
          }));
      http
          .exceptionHandling()
          .authenticationEntryPoint(((request, response, authException) -> {
            response.sendRedirect("/login");
          }))
          .accessDeniedHandler(((request, response, accessDeniedException) -> {
            response.sendRedirect("/denied");
          }));
    }
  }
  
  ```

  