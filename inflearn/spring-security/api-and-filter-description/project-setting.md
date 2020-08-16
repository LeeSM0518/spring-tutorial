# 프로젝트 생성

1. Intellij 에서 `File` => `New Project` 를 누른다.

   ![image](https://user-images.githubusercontent.com/43431081/89961837-f6ed7000-dc7d-11ea-9804-41863fa6fa87.png)

2. dependency로 `Spring Web` 을 넣어주고 프로젝트 생성

<br>

## 간단한 컨트롤러 생성

* **SecurityController**

  ```java
  package io.security.basicsecurity;
  
  import org.springframework.web.bind.annotation.GetMapping;
  import org.springframework.web.bind.annotation.RestController;
  
  @RestController
  public class SecurityController {
  
    @GetMapping("/")
    public String index() {
      return "home";
    }
  
  }
  ```

  * 보안이 하나도 되어 있지 않아서 누구든 접근할 수 있다.

<br>

## 의존 추가

* **build.gradle**

  ```java
  implementation 'org.springframework.boot:spring-boot-starter-security'
  ```

* **프로젝트 시작시**

  ```
  ...
  Using generated security password: 3b2e4f94-d3d2-4cfb-aada-3d5071a3cf58
  ...
  ```

  > 이와 같은 로그가 나옴

* `localhost:8080` 에 접속하면 `localhost:8080/login` 으로 자동으로 이동되고 로그인 화면이 나온다.

  ![image](https://user-images.githubusercontent.com/43431081/89962067-9c084880-dc7e-11ea-89e5-cc2e7004a2e7.png)

  * 반드시 로그인해야만 자원에 접근할 수 있도록 자동으로 변경이 되었다.
  * **ID** : user
  * **PW** : 로그에 나오는 값

<br>

## 인증 API - 스프링 시큐리티 의존성 추가

**의존성 추가시 연동되는 웹 보안 기능들**

1. 모든 요청은 인증이 되어야 자원에 접근이 가능하다.
2. 인증 방식은 폼 로그인 방식과 httpBasic 로그인 방식을 제공한다.
3. 기본 로그인 페이지를 제공한다.
4. 기본 계정 한 개 제공한다 - username : user / password : 랜덤 문자열

<br>

**문제점**

* 계정 추가, 권한 추가, DB 연동
* 기본적인 보안 기능 외에 시스템에서 필요로 하는 더 세부적이고 추가적인 보안기능이 필요