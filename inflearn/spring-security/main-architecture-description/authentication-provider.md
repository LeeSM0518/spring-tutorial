# AuthenticationProvider

![image](https://user-images.githubusercontent.com/43431081/90086449-6d10d600-dd55-11ea-9bf0-15ea7517bf1c.png)

* **AuthenticationProvider** : 인터페이스
  * `authenticate(authentication)` : 인증
    * `authentication` : 인증 객체
    * paassword 검증
      * passwrod를 저장할 때, password encoding 을 통해 암호화한다.
  * `supports` : 인증을 처리할 수 있는지 검사