# 동시 세션 제어 / 세션고정보호 / 세션 정책

## SessionManagementFilter

1. 세션 관리
   * 인증 시 사용자의 세션정보를 등록, 조회, 삭제 등의 세션 이력을 관리
2. 동시적 세션 제어
   * 동일 계정으로 접속이 허용되는 최대 세션수를 제한
3. 세션 고정 보호
   * 인증 할 때마다 세션쿠키를 새로 발급하여 공격자의 쿠키 조작을 방지
4. 세션 생성 정책
   * `Always` , `If_Required` , `Never` , `Stateless`

<br>

## 동시 세션 제어

![image](https://user-images.githubusercontent.com/43431081/89969628-d8916f80-dc91-11ea-9ee6-4187ad86ed49.png)

![image](https://user-images.githubusercontent.com/43431081/89969690-05de1d80-dc92-11ea-82b1-2f121ddb73db.png)

<br>

## 세션고정보호

![image](https://user-images.githubusercontent.com/43431081/89969652-ee069980-dc91-11ea-9bf9-ed7a8b598375.png)

![image](https://user-images.githubusercontent.com/43431081/89969728-18585700-dc92-11ea-8232-63be3ef8a77c.png)

* **changeSessionId(), migrateSession()** : 세션 변경
* **newSession()** : 세션 생성
* **none()** : 기존 세션(공격자에게 위험).

<br>

## 세션 정책

![image](https://user-images.githubusercontent.com/43431081/89974764-ea2d4400-dc9e-11ea-971b-848640bb216c.png)