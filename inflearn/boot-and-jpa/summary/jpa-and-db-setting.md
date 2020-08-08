# JPA와 DB 설정, 동작확인

Spring Boot의 `application.yml` 설정은 https://docs.spring.io/spring-boot/docs/current/reference/html/ 에서 배우자.

1. `application.yml` 세팅

   ```yaml
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
   
     jpa:
       hibernate:
         # 자동으로 테이블을 만들어주는 모드.
         #   기능: 애플리케이션 실행 시점에 테이블을 drop하고, 다시 생성
         ddl-auto: create
       properties:            # hibernate 의 특정한 속성을 작성
         hibernate:
         # hinbernate가 생성한 SQL을 system.out 에 출력한다.
         #   운영환경에서는 지워야 한다.
   #        show_sql: true       
           format_sql: true
   
   logging:
     level:
       # hibernate가 생성한 SQL을 Logger에 출력한다.
       #  운영환경에서는 Logger로만 출력한다.
       org.hibernate.SQL: debug  
   ```

2. 실제 동작하는지 확인

   * **회원 엔티티**

     ```java
     @Entity                  // 엔티티로 지정
     @Getter @Setter
     public class Member {

       @Id                // 식별자 지정
       @GeneratedValue    // 데이터베이스가 자동으로 값을 생성하도록 함
       private Long id;
       private String username;

     }
     ```
  
   * **회원 리포지토리**
   
     ```java
     // DAO 랑 비슷한 개념이다.
     @Repository
     public class MemberRepository {
     
       // 엔티티 매니저
       //  스프링 부트가 엔티티 매니저를 주입하도록 하는 에노테이션
       @PersistenceContext
       private EntityManager em;
     
       // 멤버 저장 메서드
       public Long save(Member member) {
         em.persist(member);
         return member.getId(); // 커맨드와 쿼리를 분리하기 위해서 ID 정보만 반환
       }
     
       // 멤버 조회 메서드
       public Member find(Long id) {
         return em.find(Member.class, id);
       }
     
     }
     ```
   
3. 테스트 코드를 작성한다( `shift + command + T` ).

   ![image](https://user-images.githubusercontent.com/43431081/89622209-aa94df80-d8cd-11ea-959a-68be26d3f167.png)

   * **Tip. Intellij Template 만들기**

     ![image](https://user-images.githubusercontent.com/43431081/89622855-a7e6ba00-d8ce-11ea-9939-b9bebc737e7a.png)

     * `custom` 이라는 template group을 하나 만들어서 `tdd` 라는 template 하나 만든다.

   <br>

   * **MemberRepositoryTest**

     ```java
     package japbook.jpashop;
     
     import org.assertj.core.api.Assertions;
     import org.junit.Test;
     import org.junit.runner.RunWith;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.boot.test.context.SpringBootTest;
     import org.springframework.test.annotation.Rollback;
     import org.springframework.test.context.junit4.SpringRunner;
     import org.springframework.transaction.annotation.Transactional;
     
     import static org.junit.Assert.*;
     
     @RunWith(SpringRunner.class)  // JUnit한테 Spring과 관련된 테스트라고 알려준다.
     @SpringBootTest               // Spring Boot 를 사용한 테스트라고 알려준다.
     public class MemberRepositoryTest {
     
       @Autowired
       MemberRepository memberRepository;
     
       @Test
       @Transactional // 트랜잭션 환경을 반드시 만들어줘야 한다.
       // @Rollback(false) // 롤백을 못하게 한다.
       public void testMember() throws Exception {
         // given
         Member member = new Member();
         member.setUsername("memberA");
     
         // when
         //   "option + command + V" 단축키: 해당 메서드로부터 나온 값을 저장하는 변수를 만들어준다.
         Long saveId = memberRepository.save(member);
         Member findMember = memberRepository.find(saveId);
     
         // then
         Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
         Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
     
         // True가 나온다.
         Assertions.assertThat(findMember).isEqualTo(member); // == 비교와 같다.
     
         // 영속성 컨텍스트로부터 기존에 캐시에 관리하던 데이터를 가져온다.
         //  select 조차 하지 않음.
         System.out.println("findMember == member: " + (findMember == member));
       }
     
     }
     ```

     * 테스트가 끝나고 DB를 `select * from member` 로 확인해보면 member가 존재하지 않는다.
     * 왜냐하면, Test 값이 DB로 들어가면 반복적인 테스트가 안되므로 테스트가 끝나면 `rollback` 을 한다.

<br>

## Jar 빌드

1. 프로젝트 위치로 이동

   ```bash
   bash$ cd ~/spring-tutorial/inflearn/jpashop
   ```

2. 깔끔하게 지우고 빌드한다.

   ```bash
   bash$ ./gradlew clean build
   ```

   * 기존 빌드 삭제 => 빌드 => 테스트 => 완료

3. 빌드된 파일의 위치로 이동한다.

   ```bash
   bash$ cd build/libs
   bash$ ll 
   total 73424
   -rw-r--r--  1 sangminlee  staff    36M  8  7 17:13 jpashop-0.0.1-SNAPSHOT.jar
   ```

4. `jar` 파일을 실행한다.

   ```bash
   bash$ java -jar jpashop-0.0.1-SNAPSHOT.jar
   ```

<br>

## 쿼리 파라미터 로그 남기기

**application.yml**

```yaml
...

logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.type: trace # 추가
```

**실행 결과**

```
2020-08-07 17:26:39.505 DEBUG 527 --- [           main] org.hibernate.SQL: 
    insert 
    into
        member
        (username, id) 
    values
        (?, ?)
... binding parameter [1] as [VARCHAR] - [memberA]
... binding parameter [2] as [BIGINT] - [1]
```

> 이렇게 쿼리 파라미터에 어떤 값이 들어간 것인지를 볼 수 있다.

하지만 좀 더 자세하게 쿼리 파라미터를 조회하고 싶을 수 있다. 이때 외부 라이브러리를 사용하면 된다.

1. `build.gradle` 에 `dependency` 를 하나 추가해준다.

   ```java
   implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.6")
   ```

2. 그 후, 실행을 시켜보면 쿼리 파라미터에 어떤 값이 들어가서 실행됬는지 확인할 수 있다.

   ```
   ...
   insert into member (username, id) values (?, ?)
   insert into member (username, id) values ('memberA', 1);
   ...
   ```