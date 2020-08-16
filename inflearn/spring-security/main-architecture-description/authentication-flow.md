# Authentication Flow

## 순서도

![image](https://user-images.githubusercontent.com/43431081/90083748-54e98880-dd4e-11ea-9a6b-5bc3d56b2cac.png)

* **UsernamePasswordAuthenticationFilter** : Form 방식 인증을 진행할 때 적용된다.
  1. `Authentication(id + pass)` 인증 객체를 생성한다.
  2. **AuthenticationManager** 의 `authenticate(Authentication)` 를 호출하여 인증 객체를 전달하며 역할을 위임한다.
* **AuthenticationManager** : 인증의 전반적인 관리, 적절한 `AuthenticationProvider` 에 인증을 위임한다.
  1. 적절한 인증 처리를 할 수 있는 **AuthenticationProvider** 를 찾아서 `authenticate(Authentication)` 을 호출하여 인증을 위임한다.
* **AuthenticationProvider** : 실제 인증 처리 역할, 유저 유효성 검증 (패스워드 체크 등)
  1. **UserDetailsService** 의 `loadUserByUsername(username)` 을 호출하여 유저 객체를 조회 요청한다.
* **UserDetailsService** : 유저 객체 조회, `UserDetails` 타입으로 유저를 반환
  1. **Repository** 로부터 `findById()` 를 호출하여 유저 조회
     * 유저가 존재하지 않으면 `UsernameNotFoundException` 이 발생해서 예외를 던지고, **UsernamePasswordAuthenticationFilter** 에서 `FailHandler` 가 예외를 처리하게 된다.