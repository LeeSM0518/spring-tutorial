# 인증 사용자 정보 구하기

## 1. 어플리케이션 전역

```java
Account account = 
  (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String username = account.getUsername();
```

<br>

## 2. Spring MVC

* **Authentication, Principal**

  ```java
  public String getUsername(Authentication authentication, Principal principal) {
    Account account = (Account) authentication.getPrincipal();
    Account account = (Account)((UsernamePasswordAuthenticationToken)principal).getPrincipal();
    String username = account.getUsername();
  }
  ```

* **@AuthenticationPrincipal** : 인증 성공 이후 생성된 Authentication 객체의 principal 속성에 저장되어 있는 객체

  ```java
  public ModelAndView getUsername(@AuthenticationPrincipal Account account) {
    String username = account.username();
  }
  ```

