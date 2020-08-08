# 프로젝트 환경설정

* 프로젝트 생성
* 라이브러리 살펴보기
* View 환경 설정
* H2 데이터베이스 설치
* JPA와 DB 설정, 동작확인

<br>

## 프로젝트 생성

1. https://start.spring.io 에서 프로젝트를 생성한 뒤, 다운로드 된 프로젝트를 적당한 곳으로 위치시킨다.

![image](https://user-images.githubusercontent.com/43431081/89609640-6ac0fe80-d8b3-11ea-89ed-db6fb715b036.png)

* **Spring Web** : 웹 애플리케이션 개발을 위해 반드시 필요

* **Thymeleaf** : JSP 대신 사용하며 웹 환경과 비웹 환경 모두에서 작동할 수 있는 Java XML / XHTML / HTML5 템플릿 엔진이다.

* **Spring Data JPA** : JPA를 쓰기 편하게 만들어놓은 *모듈* 이다. JPA를 한 단계 추상화시킨 `Repository` 라는 인터페이스를 제공함으로써 개발자가 JPA를 더 쉽고 편하게 사용할 수 있도록 도와준다.

  <img src="https://suhwan.dev/images/jpa_hibernate_repository/overall_design.png">

  * **JPA (Java Persistence API)** : 자바 어플리케이션에서 관계형 데이터베이스를 사용하는 방식을 정의한 *인터페이스* 이다.
  * **Hibernate** : JPA의 *구현체* 이다. JPA와 Hibernate는 마치 자바의 interface와 해당 interface를 구현한 class와 같은 관계이다. *JPA를 사용하기 위해서 반드시 Hibernate를 사용할 필요가 없다.* 하지만, Hibernate가 굉장히 성숙한 라이브러리이기 때문에 많이 사용하는 것이다.

* **H2 Database** : 개발 & 테스트 할 때 주로 사용하는 것으로 웹 어플리케이션을 실행할 때, 데이터베이스를 내장에서 실행한다. 자바로 작성된 *관계형 데이터베이스 관리 시스템(DBMS)* 이다.

* **Lombok** : Getter & Setter 를 Annotation 으로 작성할 수 있도록 한다.

<br>

2. IntelliJ 에서 `Open or Import` 를 클릭한다.

   ![image](https://user-images.githubusercontent.com/43431081/89609835-efac1800-d8b3-11ea-8fcc-abfb5971537c.png)

3. 다운로드한 프로젝트로 이동해 `build.gradle` 을 `open` 한다.

   ![image](https://user-images.githubusercontent.com/43431081/89609944-3863d100-d8b4-11ea-824d-02096b608208.png)

   * `open as project` 선택

<br>

4. 프로젝트 세팅이 다 끝나면 `build.gradle` 을 아래와 같이 수정한다.

   ```java
   plugins {
     // 스프링 부트 버젼을 올리면 다른 라이브러리 버전들이 모두 올라가는것 참고
   	id 'org.springframework.boot' version '2.1.7.RELEASE' 
   	id 'io.spring.dependency-management' version '1.0.8.RELEASE'
   	id 'java'
   }
   
   apply plugin: 'io.spring.dependency-management' 
     
   ext["h2.version"] = "1.4.199" //h2 데이터베이스 버전 지정
   
   group = 'japbook'
   version = '0.0.1-SNAPSHOT'
   sourceCompatibility = '1.8' // 자바 버전
   
   configurations {
   	compileOnly {
   		extendsFrom annotationProcessor // lombok 에 의한 것
   	}
   }
   
   repositories {
   	mavenCentral()
   }
   
   dependencies {
   	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
   	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
   	implementation 'org.springframework.boot:spring-boot-starter-web'
   	compileOnly 'org.projectlombok:lombok'
   	runtimeOnly 'com.h2database:h2'
   	annotationProcessor 'org.projectlombok:lombok'
   	testImplementation 'org.springframework.boot:spring-boot-starter-test'
   }
   ```

<br>

5. 테스트로 실행을 해본다.

   ![image](https://user-images.githubusercontent.com/43431081/89610715-40247500-d8b6-11ea-81a0-64b1ee8e93ce.png)

<br>

6. 이 화면이 나오면 테스트를 성공한 것이다.

   ![image](https://user-images.githubusercontent.com/43431081/89610799-7530c780-d8b6-11ea-957e-d807a23e70cf.png)

7. 테스트 케이스도 실행을 해보자.

   ![image](https://user-images.githubusercontent.com/43431081/89610868-a4dfcf80-d8b6-11ea-9678-f615cf816230.png)

8. Lombok 도 한번 확인해보자.

   1. `Preferences` 를 열어서 `Plugin` 을 검색하고 플러그인 검색창에 `Lombok` 을 검색한다.

      ![image](https://user-images.githubusercontent.com/43431081/89611063-220b4480-d8b7-11ea-996d-cd5bb633cbf3.png)

      > 현재 lombok이 깔려있는 상태라 lombok이 안나오지만, 안깔려있으면 원래 `lombok` 이 나와서 해당 플러그인을 깔면된다.

      <br>

      * 만약 에러가 발생했을 때

        <img src="https://user-images.githubusercontent.com/43431081/89611217-934af780-d8b7-11ea-8a88-abcffcd0ffaa.png" alt="image" style="zoom:33%;" />

      * `Preferences` 에 들어가서 `annotation processor` 를 검색하고 `Default` 를 클릭한 뒤, `Enable annotation processing` 을 선택하면 된다.

        ![image](https://user-images.githubusercontent.com/43431081/89611296-cab9a400-d8b7-11ea-9d22-dc9ab76504f4.png)

<br>

9. 클래스를 하나 만든다.

   ![image](https://user-images.githubusercontent.com/43431081/89611445-369c0c80-d8b8-11ea-8db9-28d7bbae936c.png)

10. Application 클래스에서 Hello 를 생성하고 실행해본다.

    ![image](https://user-images.githubusercontent.com/43431081/89611544-74993080-d8b8-11ea-880a-6e6a56491672.png)

11. 프로젝트 설정 & 테스트 완료

<br>

최근 InteliJ 버전은 Gradle로 실행을 하는 것이 기본 설정인데, 이렇게 하면 실행속도가 느리다. 그러므로 자바로 바로 실행하도록 환경을 설정하자.

![image](https://user-images.githubusercontent.com/43431081/89611764-effae200-d8b8-11ea-8ce6-f5df6f954e59.png)

<br>

