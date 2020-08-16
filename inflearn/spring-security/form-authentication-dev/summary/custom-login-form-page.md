# Custom Login Form Page

![image](https://user-images.githubusercontent.com/43431081/90124587-95bbbe80-dd9b-11ea-9353-2be7ab189651.png)

```java
@Override
public void configure(HttpSecurity http) throws Exception {
  http.formLogin().loginPage("/customLogin")
}
```

<br>

## 예시

```java
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
    .loginProcessingUrl("/login_proc")   // 로그인 성공 페이지
    .defaultSuccessUrl("/")              // 기본 URL
    .permitAll();                        // 누구든지 인증
}
```