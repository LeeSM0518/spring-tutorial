# 권한 설정

## 설정 예시

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
  http
    .anyMatcher("/shop/**")
    .authorizeRequests()
      // 해당 URL은 요청은 권한을 허락한다.
      .antMatchers("/shop/login", "/shop/users/**").permitAll()
      // 해당 URL은 USER 권한이 있어야 한다.
      .antMatchers("/shop/mypage").hasRole("USER")
      // 해당 URL은 ADMIN 권한이 있어야 한다.
      .antMatchers("/shop/admin/pay").access("hasRole('ADMIN')")
      // 해당 URL은 ADMIN 이나 SYS 권한이 있어야 한다.
      .antMatchers("/shop/admin/**").access("hasRole('ADMIN') or hasRole('SYS')");
      // 인증을 설정한다.
      .anyRequest().authenticated();
}
```

> **주의 사항**
> : 설정 시 구체적인 경로가 먼저 오고 그것 보다 큰 범위의 경로가 뒤에 오도록 해야 한다.

<br>

## 인가 API - 표현식

| 메소드                         | 동작                                                     |
| ------------------------------ | -------------------------------------------------------- |
| **authenticated()**            | 인증된 사용자의 접근을 허용                              |
| **fullyAuthenticated()**       | 인증된 사용자의 접근을 허용, rememberMe 인증 제외        |
| **permitAll()**                | 무조건 접근을 허용                                       |
| **denyAll()**                  | 무조건 접근을 허용하지 않음                              |
| **anonymous()**                | 익명사용자의 접근을 허용                                 |
| **rememberMe()**               | 기억하기를 통해 인증된 사용자의 접근을 허용              |
| **access(String)**             | 주어진 SpEL  표현식의 평가 결과가 true이면 접근을 허용   |
| **hasRole(String)**            | 사용자가 주어진 역할이 있다면 접근을 허용                |
| **hasAuthority(String)**       | 사용자가 주어진 권한이 있다면                            |
| **hasAnyRole(String...)**      | 사용자가 주어진 권한이 있다면 접근을 허용                |
| **hasAnyAuthority(String...)** | 사용자가 주어진 권한 중 어떤 것이라도 있다면 접근을 허용 |
| **hasIpAddress(String)**       | 주어진 IP로부터 요청이 왔다면 접근을 허용                |

<br>

## 권한 설정 예시

* **SecurityConfig**

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
          .antMatchers("/user").hasRole("USER")
          .antMatchers("/admin/pay").hasRole("ADMIN")
          .antMatchers("/admin/**").access("hasRole('ADMIN') or hasRole('SYS')")
          .anyRequest().authenticated();
      http
          .formLogin();
    }
  }
  ```

  

