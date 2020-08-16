# Authentication

**Authentication** : 당신이 누구인지 증명하는 것

* **사용자의 인증 정보를 저장하는 토근 개념**
* **인증 시** id 와 password 를 담고 인증 검증을 위해 전달되어 사용된다.
* **인증 후** 최종 인증 결과 (user 객체, 권한 정보)를 담고 SecurityContext 에 저장되어 전역적으로 참조가 가능하다.
  * `Authentication authentication = SecurityContextHolder.getContext().getAuthentication()`
* **구조**
  * **principal** : 사용자 아이디 혹은 User 객체를 저장
  * **credentials** : 사용자 비밀번호
  * **authorities** : 인증된 사용자의 권한 목록
  * **details** : 인증 부가 정보
  * **authenticated** : 인증 여부

<br>

## 요청 시 처리 과정

![image](https://user-images.githubusercontent.com/43431081/89996083-4efb9500-dcc5-11ea-91fd-81a32cbb08db.png)

<br>

## 인증 정보

```java
SecurityContextHolder.getContext().getAuthentication()
```

* 결과
  * **principal** : 사용자 아이디 혹은 User 객체를 저장
  * **credentials** : 사용자 비밀번호
  * **authorities** : 인증된 사용자의 권한 목록
  * **details** : 인증 부가 정보
  * **authenticated** : 인증 여부