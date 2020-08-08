# H2 데이터베이스 설치

H2 데이터베이스 : 자바 기반의 RDBMS 이다. 개발이나 테스트 용도로 가볍고 편리한 DB, 웹 화면 제공

<br>

## Spring boot에서 H2 데이터베이스 사용하기

* `resources/application.yml` 에 H2 데이터베이스 정보를 넣어준다.

  ```properties
  server:
    port: 8099
    
  spring:
    h2:
      console:
        enabled: true   # H2 웹 콘솔을 사용하겠다는 의미
        path: /test_db  # 콘솔의 경로
  
    datasource:
      driver-class-name: org.h2.Driver  # h2 드라이버 설정
      url: jdbc:h2:file:/Users/sangminlee/spring-tutorial/inflearn/jpashop/db/test_db;AUTO_SERVER; # 접속 URL
      username: test  # 사용자 이름
    password: 1234  # 사용자 암호
  ```
  
  * `jdbc:h2:file` : file로 DB를 관리한다는 의미
  * `~/../test_db` : 현재 설정된 경로에 *test_db* 라는 이름으로 데이터베이스를 생성 (해당 경로에 파일이 생긴다.)
  * `AUTO_SERVER` : 다중 접속을 허용한다.

<br>

* 서버를 실행하고 `http://localhost:8099/test_db` 에 접속한다.

  ![image](https://user-images.githubusercontent.com/43431081/89618438-09a32600-d8c7-11ea-957e-1648f521b650.png)

<br>

* `Saved Settings` 와 `JDBC URL` , `User Name` ,  `Password` 를 수정하고 `Connect` 한다.

  ![image](https://user-images.githubusercontent.com/43431081/89618879-ed53b900-d8c7-11ea-877e-fa10c357ef49.png)

<br>

* 설정완료

  ![image](https://user-images.githubusercontent.com/43431081/89619087-51767d00-d8c8-11ea-842a-a5629bef6575.png)