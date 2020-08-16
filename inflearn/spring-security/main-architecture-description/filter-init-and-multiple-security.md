# 필터 초기화와 다중 보안 설정

![image](https://user-images.githubusercontent.com/43431081/89992777-bb27ca00-dcc0-11ea-8d56-2da20a0117fe.png)

<br>

## 예시

![image](https://user-images.githubusercontent.com/43431081/89992798-c67af580-dcc0-11ea-953a-1108b0ee179c.png)

<br>

## 예제 코드

* **SrpingTest**

  ```java
  @Configuration
  @EnableWebSecurity
  @Order(0)
  class SecurityTest1 extends WebSecurityConfigurerAdapter {
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .antMatcher("/admin/**")
          .authorizeRequests()
          .anyRequest().authenticated()
          .and()
          .httpBasic();
    }
  
  }
  
  
  @Configuration
  @EnableWebSecurity
  @Order(1) // 이 우선순서가 SecurityTest1 보다 우선이 되게 되면 /admin 자원 요청의 인증을 할 수 없다.
  class SecurityTest2 extends WebSecurityConfigurerAdapter {
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
          .anyRequest().permitAll()
          .and()
          .formLogin();
    }
  
  }
  ```