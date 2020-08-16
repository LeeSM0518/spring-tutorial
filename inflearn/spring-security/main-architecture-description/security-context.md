# SecurityContextHolder & SecurityContext

* **SecurityContext**
  * SecurityContext(Authentication(User()))
  * Authentication 객체가 저장되는 보관소로 필요 시 언제든지 Authentication 객체를 꺼내어 쓸 수 있도록 제공되는 클래스
  * ThreadLocal 에 저장되어 아무 곳에서나 참조가 가능하도록 설계함
  * 인증이 완료되면 HttpSession 에 저장되어 어플리케이션 전반에 걸쳐 전역적인 참조가 가능하다.
* **SecurityContextHolder**
  * SecurityContext 객체 저장 방식
    * *MODE_THREADLOCAL* : 스레드당 SecurityContext 객체를 할당, **기본값**
    * *MODE_INHERITABLETHREADLOCAL* : 메인 스레드와 자식 스레드에 관하여 동일한 SecurityContext 를 유지
    * *MODE_GLOBAL* : 응용 프로그램에서 단 하나의 SecurityContext를 저장한다.
  * SecurityContextHolder.clearContext() : SecurityContext 기존 정보 초기화
* **Authentication authentication = SecurityContextHolder.getContext().getAuthentication()**
  * Authentication 획득

<br>

## 실행 과정

![image](https://user-images.githubusercontent.com/43431081/90080996-24eab700-dd47-11ea-8b4b-7321e7fc4572.png)

<br>

## 코드 예시

* **SecurityController**

  ```java
  @GetMapping("/test")
  public String test1(HttpSession session) {
  
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    SecurityContext context =
      (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
    Authentication authentication1 = context.getAuthentication();
  
    // 둘 다 동일한 객체를 참조하고 있다.
    System.out.println("======== 1 ==========");
    System.out.println(authentication);
    System.out.println("======== 2 ==========");
    System.out.println(authentication1);
  
    return "test";
  }
  
  @GetMapping("/test2")
  public String test2() {
    new Thread(() -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      System.out.println("========= 3 =========");
      System.out.println(authentication); // null 이다
      // 자식 스레드에도 동일한 SecurityContext를 유지하려면
      //  SecurityConfig 에서 SecurityContextHolder.setStrategyName(MODE_INHERITTABLETHREADLOCAL) 을
      //  호출해야 한다.
    }).start();
    return "test2";
  }
  ```