# SecurityContextPersistenceFilter

**SecurityContextPersistenceFilter** : SecurityContext 객체의 생성, 저장, 조회 역할

* **익명 사용자**
  * 새로운 SecurityContext 객체를 생성하여 SecurityContextHolder에 저장
  * AnonymouseAuthenticationFilter 에서 AnonymousAuthenticationToken 객체를 SecurityContext에 저장
* **인증 시**
  * 새로운 SecurityContext 객체를 생성하여 SecurityContextHolder에 저장
  * UsernamePasswordAuthenticationFilter 에서 인증 성공 후 SecurityContext 에 UsernamePasswordAuthentication 객체를 SecurityContext 에 저장
  * 인증이 최종 완료되면 Session 에 SecurityContext 를 저장
* **인증 후**
  * Session 에서 SecurityContext 꺼내어 SecurityContextHolder 에서 저장
  * SecurityContext 안에 Authentication 객체가 존재하면 계속 인증을 유지한다.
* **최종 응답 시 공통**
  * SecurityContextHolder.clearContext()

<br>

## 처리 과정

![image](https://user-images.githubusercontent.com/43431081/90082937-0fc45700-dd4c-11ea-90e5-bd77ee0f1cb3.png)

<br>

## 처리 과정 간략화

![image](https://user-images.githubusercontent.com/43431081/90082964-21a5fa00-dd4c-11ea-8b58-817392935ccf.png)

<br>

## Sequence

![image](https://user-images.githubusercontent.com/43431081/90083312-215a2e80-dd4d-11ea-8e95-1659602a086d.png)