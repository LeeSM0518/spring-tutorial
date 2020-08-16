# FilterSecurityInterceptor(인가처리 담당 필터)

* 마지막에 위치한 필터로써 인증된 사용자에 대하여 특정 요청의 승인/거부 여부를 최종적으로 결정
* 인증객체 없이 보호자원에 접근을 시도할 경우 AuthenticationException 발생
* 인증 후 자원에 접근 가능한 권한이 존재하지 않을 경우 AccessDeniedException 을 발생
* 권한 제어 방식 중 HTTP 자원의 보안을 처리하는 필터
* 권한 처리를 AccessDecisionManager에게 맡김

<br>

## 처리 과정

![image](https://user-images.githubusercontent.com/43431081/90087318-6be0a880-dd57-11ea-8649-1e087cf839e4.png)

![image](https://user-images.githubusercontent.com/43431081/90087350-7e5ae200-dd57-11ea-8782-7dfe8ba48949.png)