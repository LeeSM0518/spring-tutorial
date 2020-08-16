# 인증 API - 사용자 정의 보안 기능 구현

## 개념 설명

* 의존성 추가 => 웹 보안 활성화
  * **문제점**
    * 사용자 계정이 하나 밖에 없다.
    * 권한을 수정 불가
    * 보안 옵션 부재

<br>

![image](https://user-images.githubusercontent.com/43431081/89962504-d6beb080-dc7f-11ea-9a2d-0c3d6cc6fcfa.png)

* `WebSecurityConfigurerAdapter` : 스프링 시큐리티의 웹 보안 기능 초기화 및 설정
  * `HttpSecurity` 생성 : 세부적인 보안 기능을 설정할 수 있는 API 제공
    * 인증 API
    * 인가 API

* `SecurityConfig` 를 만들 것이다.
  * 사용자 정의 보안 설정 클래스를 만들기 위해

<br>

## SecurityConfig 설정

* **SecurityConfig**

  ```java
  package io.security.basicsecurity;
  
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
  import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
  
  @Configuration
  @EnableWebSecurity // 웹 보안 활성화
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    @Override
    // 인증, 인가 API 설정 및 보안 옵션 설정 가능
    protected void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
          .anyRequest().authenticated()   // 어떠한 요청에도 인증
          .and()
          .formLogin();
  
    }
    
  }
  ```

* **application.yml**

  ```yaml
  spring:
    security:
      user:
        name: user
        password: 1111
  ```

  * `formLogin` 의 유저 아이디랑 비밀번호 설정

