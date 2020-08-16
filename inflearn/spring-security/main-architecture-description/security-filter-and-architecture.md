# 스프링 시큐리티 필터 및 아키텍처 정리

![image](https://user-images.githubusercontent.com/43431081/90093967-b9651180-dd67-11ea-9005-9764e56ea1b7.png)

1. `SecurityConfig1` 과 `SecurityConfig2` 에 설정한대로 `Filters` 들이 생성된다.
2. `WebSecurity` 클래스로 `Filters` 전달
3. `FilterChainProxy` 의 생성자로 `Filters` 전달 및 생성
4. `DelegatingFilterProxy` 는 의존으로 `SpringSecurityFilterChain` 을 주입 받아야 하는데, `FilterChainProxy` 를 주입받는다.
5. `DelegatingFilterProxy` 가 사용자로부터 요청을 받게 되면 인증 작업을 `FilterChainProxy` 에게 위임한다.

