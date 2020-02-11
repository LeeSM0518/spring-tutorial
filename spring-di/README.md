# Chapter 03. 스프링 DI

* **Content**
  * 객체 의존과 의존 주입(DI)
  * 객체 조립
  * 스프링 DI 설정

<br>

# 1. 의존이란?

**DI(Dependency Injection)** : 의존 주입

**의존(dependency)** : 객체 간의 의존을 의미한다.

* **예시) 회원 가입을 처리하는 기능**

  ```java
  public class MemberRegisterService {
    
    private MemberDao memberDao = new MemberDao();
    
    public void regist(RegisterRequest req) {
      // 이메일로 회원 데이터(Member) 조회
      Member member = memberDao.selectByEmail(req.getEmail());
      if (member != null) {
        // 같은 이메일을 가진 회원이 이미 존재하면 Exception 발생
        throw new DuplicateMemberException("dup email " + req.getEmail());
      }
      // 같은 이메일을 가진 회원이 존재하지 않으면 DB에 삽입
      Member newMember = new Member(
        req.getEmail(), req.getPassword(), req.getName(),
        LocalDateTime.now());
      memberDao.insert(newMember);
    }
    
  }
  ```

  * 위의 코드의 핵심은 MemberRegisterService 클래스가 DB 처리를 위해 MemberDao 클래스의 메서드를 사용한다는 점이다.

  * 이렇게 **한 클래스가 다른 클래스의 메서드를 실행할 때 이를 '의존'한다고 표현한다.**

    * "MemberRegisterService 클래스가 MemberDao 클래스에 의존한다"
    * 의존은 **변경에 의해 영향을 받는 관계를** 의미한다.

  * 의존 대상을 구하는 방법 중에 가장 쉬운 방법은 직접 생성하는 것이다.

    ```java
    public class MemberRegisterService {
      // 의존 객체를 직접 생성
      private MemberDao memberDao = new MemeberDao();
    }
    ```

    * MemberRegisterService 클래스에서 의존하는 MemberDao 객체를 직접 생성하기 때문에 MemberRegisterService 객체를 생성하는 순간에 MemberDao 객체도 함께 생성한다.

      ```java
      MemberRegisterService svc = new MemberRegisterService();
      ```

  * 클래스 내부에서 직접 의존 개게를 생성하는 것은 쉽지만, **유지보수 관점에서 문제점을 유발한다.**

  <br>

**의존 객체를 구하는 또 다른 방법**

* DI
* 서비스 로케이터

<br>

# 2. DI를 통한 의존 처리

**DI(Dependency Injection, 의존 주입)는** 의존하는 객체를 직접 생성하는 대신 의존 객체를 전달받는 방식을 사용한다.

* **예시) 회원 가입 처리하는 기능 (DI 추가)**

  ```java
  public class MemberRegisterService {
    private MemberDao memberDao;
    
    public MemberRegisterService(MemberDao memberDao) {
      this.MemberDao = memberDao;
    }
    
    public Long regist(RegisterRequest req) {
      Member member = memberDao.selectByEmail(req.getEmail());
      if (member != null) {
        throw new DuplicateMemberException("dup email " + req.getEmail());
      }
      Member newMember = new Member(
        req.getEmail(), req.getPassword(), req.getName(),
        LocalDateTime.now());
      memberDao.insert(newMember);
      return newMember.getId();
    }
  }
  ```

  * **04~06 행** : 직접 의존 객체를 생성했던 코드와 달리 바뀐 코드는 의존 객체를 직접 생성하지 않는다. 대신 생성자를 통해서 의존 객체를 전달받는다.
    * **의존 객체를 직접 구하지 않고 생성자를 통해서 전달받기 때문에** 이 코드는 DI(의존 주입) 패턴을 따르고 있다.

  ```java
  MemberDao dao = new MemberDao();
  // 의존 객체를 생성자를 통해 주입한다.
  MemberRegisterService svc = new MemberRegisterService(dao);
  ```

  * DI를 적용한 결과 객체를 생성할 때 생성자에 MemberDao 객체를 전달한다.

<br>

# 3. DI와 의존 객체 변경의 유연함

* **MemberRegisterService 클래스**

  ```java
  public class MemberRegisterService {
    private MemberDao memberDao = new MemberDao();
    ...
  }
  ```

* **ChangePasswordService 클래스**

  ```java
  public class ChangePasswordService {
    private MemberDao memberDao = new MemberDao();
    ...
  }
  ```

<br>

위와 같이 두 개의 클래스가 존재하고 있는데, MemberDao 클래스를 상속받은 CachedMemberDao 클래스를 만들었다고 가정해보자.

```java
public class CachedMemberDao extends MemberDao {
  ...
}
```

<br>

캐시 기능을 적용한 CachedMemberDao를 사용하려면 MemberRegisterService 클래스와 ChangePasswordService 클래스의 코드를 바꿔줘야 한다.

```java
public class MemberRegisterService {
  // private MemberDao memberDao = new MemberDao();
  private MemberDao memberDao = new CachedMemberDao();
  ...
}

public class ChangePasswordService {
  // private MemberDao memberDao = new MemberDao();
  private MemberDao memberDao = new CachedMemberDao();
  ...
}
```

> 지금은 클래스가 2개라 간단하지만 MemberDao 클래스를 사용하던 클래스가 20개 이상이라면 매우 번거로운 작업이 될 것이다. 즉, 유지보수가 어렵다.

<br>

위와 같은 상황에서 DI를 사용하면 수정할 코드가 줄어든다.

```java
public class MemberRegisterService {
  private MemberDao memberDao;
  public MemberRegisterService(MemberDao memberDao) {
    this.memberDao = memberDao;
  }
  ...
}

public class ChangePasswordService {
  private MemberDao memberDao;
  public ChangePasswordService(MemberDao memberDao) {
    this.memberDao = memberDao;
  }
  ...
}
```

 두 클래스의 객체를 생성하는 코드

```java
MemberDao memberDao = new MemberDao();
MemberRegisterService regSvc = new MemberRegisterService(memberDao);
ChangePasswordService pwdSvc = new ChangePasswordService(memberDao);
```

MemberDao 대신 CachedMemberDao를 사용하도록 수정해보자.

```java
// MemberDao memberDao = new MemberDao();
MemberDao memberDao = new CachedMemberDao();
MemberRegisterService regSvc = new MemberRegisterService(memberDao);
ChangePasswordService pwdSvc = new ChangePasswordService(memberDao);
```

> 앞서 의존 객체를 직접 생성했더 방식에 비해 변경할 코드가 한 곳으로 집중되는 것을 알 수 있다.

<br>

# 4. 예제 프로젝트 만들기

1. Gradle 프로젝트를 만든다.

2. gradle 파일에 `implementation 'org.springframework:spring-context:5.2.3.RELEASE'` 를 추가해준다.

   ```
   plugins {
       id 'java'
   }
   
   group 'org.example'
   version '1.0-SNAPSHOT'
   
   sourceCompatibility = 1.8
   
   repositories {
       mavenCentral()
   }
   
   dependencies {
       testCompile group: 'junit', name: 'junit', version: '4.12'
       implementation 'org.springframework:spring-context:5.2.3.RELEASE'
   }
   ```

<br>

**구현할 클래스 목록**

* 회원 데이터 관련 클래스
  * Member
  * WrongIdPasswordException
  * MemberDao
* 회원 가입 처리 관련 클래스
  * DuplicateMemberException
  * RegisterRequest
  * MemberRegisterService
* 암호 변경 관련 클래스
  * MemberNotFoundException
  * ChangePasswordService

<br>

## 4.1. 회원 데이터 관련 클래스

* **src/main/java/spring/Member.java**

  ```java
  package spring;
  
  import java.time.LocalDateTime;
  
  public class Member {
    
    private Long id;
    private String email;
    private String password;
    private String name;
    private LocalDateTime registerDateTime;
  
    public Member(Long id, String email, String password, String name, LocalDateTime registerDateTime) {
      this.id = id;
      this.email = email;
      this.password = password;
      this.name = name;
      this.registerDateTime = registerDateTime;
    }
  
    public Long getId() {
      return id;
    }
  
    public void setId(Long id) {
      this.id = id;
    }
  
    public String getEmail() {
      return email;
    }
  
    public void setEmail(String email) {
      this.email = email;
    }
  
    public String getPassword() {
      return password;
    }
  
    public void setPassword(String password) {
      this.password = password;
    }
  
    public String getName() {
      return name;
    }
  
    public void setName(String name) {
      this.name = name;
    }
  
    public LocalDateTime getRegisterDateTime() {
      return registerDateTime;
    }
  
    public void setRegisterDateTime(LocalDateTime registerDateTime) {
      this.registerDateTime = registerDateTime;
    }
    
    // 암호 변경 기능을 구현
    public void changePassword(String oldPassword, String newPassword) {
      if (!password.equals(oldPassword))
        throw new WrongPasswordException();
      this.password = newPassword;
    }
    
  }
  ```

* **src/main/java/spring/WrongIdPasswordException.java**

  ```java
  package spring;
  
  public class WrongPasswordException extends RuntimeException {
    
  }
  ```

* **spring/MemberDao.java**

  ```java
  package spring;
  
  import java.util.HashMap;
  import java.util.Map;
  
  public class MemberDao {
    
    private static long nextId = 0;
    
    private Map<String, Member> map = new HashMap<>();
    
    public Member selectByEmail(String email) {
      return map.get(email);
    }
    
    public void insert(Member member) {
      member.setId(++nextId);
      map.put(member.getEmail(), member);
    }
    
    public void update(Member member) {
      map.put(member.getEmail(), member);
    }
    
  }
  ```

<br>

## 4.2. 회원 가입 처리 관련 클래스

* **spring/DuplicateMemberDaoException.java**

  ```java
  package spring;
  
  public class DuplicateMemberDaoException extends RuntimeException {
  
    public DuplicateMemberDaoException(String message) {
      super(message);
    }
  }
  ```

* **spring/RegisterRequest.java**

  ```java
  package spring;
  
  public class RegisterRequest {
    
    private String email;
    private String password;
    private String confirmPassword;
    private String name;
  
    public String getEmail() {
      return email;
    }
  
    public void setEmail(String email) {
      this.email = email;
    }
  
    public String getPassword() {
      return password;
    }
  
    public void setPassword(String password) {
      this.password = password;
    }
  
    public String getConfirmPassword() {
      return confirmPassword;
    }
  
    public void setConfirmPassword(String confirmPassword) {
      this.confirmPassword = confirmPassword;
    }
  
    public String getName() {
      return name;
    }
  
    public void setName(String name) {
      this.name = name;
    }
    
    public boolean isPasswordEqualToConfirmPassword() {
      return password.equals(confirmPassword);
    }
    
  }
  ```

* **spring/MemberRegisterService.java**

  ```java
  package spring;
  
  import java.time.LocalDateTime;
  
  public class MemberRegisterService {
  
    private MemberDao memberDao;
  
    public MemberRegisterService(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
  
    public Long regist(RegisterRequest req) {
      Member member = memberDao.selectByEmail(req.getEmail());
      if (member != null) {
        throw new DuplicateMemberDaoException("dup email " + req.getEmail());
      }
      Member newMember = new Member(
          req.getEmail(), req.getPassword(), req.getName(), LocalDateTime.now());
      memberDao.insert(newMember);
      return newMember.getId();
    }
  
  }
  \
  ```

<br>

## 4.3. 암호 변경 관련 클래스

* **spring/ChangePasswordService.java**

  ```java
  package spring;
  
  public class ChangePasswordService {
    
    private MemberDao memberDao;
    
    public void changePassword(String email, String oldPwd, String newPwd) {
      Member member = memberDao.selectByEmail(email);
      if (member == null) 
        throw new MemberNotFoundException();
      
      member.changePassword(oldPwd, newPwd);
      
      memberDao.update(member);
    }
  
    public void setMemberDao(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
    
  }
  ```

  * setMemberDao() 메서드로 의존하는 MemberDao를 전달받는다. 즉 세터(setter)를 통해서 의존 객체를 주입받는다.

* **spring/MemberNotFoundException.java**

  ```java
  package spring;
  
  public class MemberNotFoundException extends RuntimeException {
  }
  ```

<br>

# 5. 객체 조립기

앞서 DI를 설명할 때 객체 생성에 사용할 클래스를 변경하기 위해 객체를 주입하는 코드 한 곳만 변경하면 된다고 했다.

```java
public class Main {
  public static void main(String[] agrs) {
    MemberDao memberDao = new MemberDao();
    MemberRegisterService regSvc = new MemberRegisterService(memberDao);
    ChangePasswordService pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao);
    ... // regSvc와 pwdSvc를 사용하는 코드
  }
}
```

위와 같이 main 메서드에서 주입해주는 방법이 있지만, 좀 더 나은 방법은 **객체를 생성하고 의존 객체를 주입해주는 클래스를 따로 작성하는 것이다.** 

의존 객체를 주입한다는 것은 서로 다른 두 객체를 조립한다고 생각할 수 있는데, 이런 의미에서 **이 클래스를 조립기라고도 부른다.**

<br>

회원 가입이나 암호 변경 기능을 제공하는 클래스의 객체를 생성하고 의존 대상이 되는 객체를 주입해주는 조립기 클래스를 작성해보자.

* **assembler/Assembler.java**

  ```java
  package assembler;
  
  import spring.ChangePasswordService;
  import spring.MemberDao;
  import spring.MemberRegisterService;
  
  public class Assembler {
  
    private MemberDao memberDao;
    private MemberRegisterService regSvc;
    private ChangePasswordService pwdSvc;
  
    public Assembler() {
      memberDao = new MemberDao();
      regSvc = new MemberRegisterService(memberDao);
      pwdSvc = new ChangePasswordService();
      pwdSvc.setMemberDao(memberDao);
    }
  
    public MemberDao getMemberDao() {
      return memberDao;
    }
  
    public MemberRegisterService getRegSvc() {
      return regSvc;
    }
  
    public ChangePasswordService getPwdSvc() {
      return pwdSvc;
    }
    
  }
  ```

<br>

* **필요한 객체를 구하고 그 객체를 사용하는 코드**

  ```java
  Assembler assembler = new Assembler();
  ChangePasswordService changePwdSvc =
    assembler.getChangePasswordService();
  changePwdSvc.changePassword("nalsm@naver.com", "1234", "newpwd");
  ```

* **MemberDao 객체를 CachedMemberDao 객체로 변경하여 사용할때**

  ```java
  public Assembler() {
    memberDao = new CachedMemberDao();
    regSvc = new MemberRegisterService(memberDao);
    pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao);
  }
  ```

<br>

## 5.1. 조립기 사용 예제

메인 클래스를 작성해보자.

메인 클래스는 콘솔에서 명령어를 입력받고 각 명령어에 알맞은 기능을 수행하도록 구현해라.

* **처리할 명령어**
  * **new** : 새로운 회원 데이터를 추가한다.
  * **change** : 회원 데이터와 암호를 변경한다.

<br>

* **main/MainForAssembler.java**

  ```java
  package main;
  
  import assembler.Assembler;
  import spring.*;
  
  import java.io.BufferedReader;
  import java.io.IOException;
  import java.io.InputStreamReader;
  
  public class MainForAssembler {
    
    public static void main(String[] args) throws IOException {
      // 콘솔에서 입력받기 위해 System.in 을 이용해서 BufferedReader 생성
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(System.in));
      
      while (true) {
        System.out.println("명령어를 입력하세요.");
        // 한 줄 입력
        String command = reader.readLine();
        
        // 입력한 문자열이 "exit"이면 프로그램 종료
        if (command.equalsIgnoreCase("exit")) {
          System.out.println("종료합니다.");
          break;
        }
  
        // 입력한 문자열이 "new" 로 시작하면 processNewCommand() 메서드를 찾아 실행
        if (command.startsWith("new")) {
          processNewCommand(command.split(" "));
          continue;
        } 
        // 입력한 문자열이 "change "로 시작하면 processChangeCommand() 메서드를 찾아 실행
        else if (command.startsWith("change ")) {
          processChangeCommand(command.split(" "));
          continue;
        }
        printHelp();
      }
    }
  
    // Assembler 객체가 생성되면서
    //  필요한 객체를 생성하고 의존을 주입한다.
    private static Assembler assembler = new Assembler();
  
    // 새로운 회원 정보를 생성
    private static void processNewCommand(String[] arg) {
      if (arg.length != 5) {
        printHelp();
        return;
      }
      // Assembler 객체 사용
      MemberRegisterService regSvc = assembler.getMemberRegisterService();
      RegisterRequest req = new RegisterRequest();
      req.setEmail(arg[1]);
      req.setName(arg[2]);
      req.setPassword(arg[3]);
      req.setConfirmPassword(arg[4]);
  
      // 입력한 암호 값이 올바른지 확인
      if (!req.isPasswordEqualToConfirmPassword()) {
        System.out.println("암호와 확인이 일치하지 않습니다.\n");
        return;
      }
      // 이미 동일한 이메일을 가진 회원 데이터가 
      //  존재하면 에러 메시지 출력
      try {
        regSvc.regist(req);
        System.out.println("등록했습니다.\n");
      } catch (DuplicateMemberDaoException e) {
        System.out.println("이미 존재하는 이메일입니다.\n");
      }
    }
  
    
    private static void processChangeCommand(String[] arg) {
      if (arg.length != 4) {
        printHelp();
        return;
      }
      
      // Assembler 객체 사용
      ChangePasswordService changePasswordService =
          assembler.getChangePasswordService();
      try {
        changePasswordService.changePassword(arg[1], arg[2], arg[3]);
        System.out.println("암호를 변경했습니다.\n");
      } catch (MemberNotFoundException e) {
        System.out.println("존재하지 않는 이메일입니다.\n");
      } catch (WrongPasswordException e) {
        System.out.println("이메일과 암호가 일치하지 않습니다.\n");
      }
    }
    
    
    private static void printHelp() {
      System.out.println();
      System.out.println("잘못된 명령입니다. 아래 명령어 사용법을 확인하세요.");
      System.out.println("명령어 사용법:");
      System.out.println("new 이메일 이름 암호 암호확인");
      System.out.println("change 이메일 현재비번 변경비번");
      System.out.println();
    }
  
  }
  ```

* **실행 결과**

  ```
  명령어를 입력하세요.
  new
  
  잘못된 명령입니다. 아래 명령어 사용법을 확인하세요.
  명령어 사용법:
  new 이메일 이름 암호 암호확인
  change 이메일 현재비번 변경비번
  
  명령어를 입력하세요.
  new abc 이상민 1234 1234
  등록했습니다.
  
  명령어를 입력하세요.
  change abc 1234 123
  암호를 변경했습니다.
  
  명령어를 입력하세요.
  change abc 12 1234
  이메일과 암호가 일치하지 않습니다.
  
  명령어를 입력하세요.
  ```

<br>

# 6. 스프링의 DI 설정

위의 예제들로 스프링 자체가 아닌 의존, DI, 조립기에 대해 먼저 알아본 이유는 **스프링이 DI를 지원하는 조립기이기 때문이다.** 

스프링은 Assembler 클래스의 생성자 코드처럼 필요한 **객체를 생성하고 생성한 객체의 의존을 주입한다.** 또한 스프링은 객체를 제공하는 기능을 정의하고 있다. 즉, **스프링은 범용 조립기 역할을 한다.**

<br>

## 6.1. 스프링을 이용한 객체 조립과 사용

Assembler 대신 스프링을 사용하는 코드를 작성해보자.

스프링을 사용하려면 먼저 **스프링이 어떤 객체를 생성하고, 의존을 어떻게 주입할지를 정의한 설정 정보를 작성해야 한다.**

<br>

* **config/AppCtx.java** : 설정 정보 코드

  ```java
  package config;
  
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import spring.ChangePasswordService;
  import spring.MemberDao;
  import spring.MemberRegisterService;
  
  // @Configuration : 스프링 설정 클래스를 의미한다.
  @Configuration
  public class AppCtx {
  
    // @Bean : 해당 메서드가 생성한 객체를 스프링 빈이라고 부른다.
    //  memberDao 라는 이름으로 스프링에 등록된다.
    @Bean
    public MemberDao memberDao() {
      return new MemberDao();
    }
  
    @Bean
    public MemberRegisterService memberRegisterService() {
      // memberDao() 가 생성한 객체를 생성자를 통해 주입한다.
      return new MemberRegisterService(memberDao());
    }
  
    @Bean
    public ChangePasswordService changePasswordService() {
      // 의존 객체 주입
      ChangePasswordService passwordService = new ChangePasswordService();
      passwordService.setMemberDao(memberDao());
      return passwordService;
    }
  
  }
  ```

<br>

설절 클래스를 만들었다고 해서 끝난 것이 아니다. 객체를 생성하고 의존 객체를 주입하는 것은 **스프링 컨테이너이므로 설정 클래스를 이용해서 컨테이너를 생성해야 한다.**

```java
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtx.class);
```

컨테이너를 생성하면 getBean() 메서드를 이용해서 사용할 객체를 구할 수 있다.

```java
MemberRegisterService regSvc =
  ctx.getBean("memberRegSvc", MemberRegisterService.class);
```

위 코드에서 MemberRegisterService 객체는 내부에서 memberDao 빈 객체를 사용한다.

```java
@Bean
public MemberDao memberDao() {
  return new MemberDao();
}

@Bean
public MemberRegisterService memberRegSvc() {
  return new MemberRegisterService(memberDao());
}
```

<br>

MainForSpring을 작성해보자.

```java
package main;

import config.AppCtx;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spring.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainForSpring {

  private static ApplicationContext ctx = null;

  public static void main(String[] args) throws IOException {
    // AnnotationConfigApplicationContext 를 사용해서 스프링 컨테이너를 생성한다.
    //  설정 파일(AppCtx 클래스)로부터 생성할 객체와 의존 주입 대상을 정한다.
    ctx = new AnnotationConfigApplicationContext(AppCtx.class);
    ...
  }

  private static void processNewCommand(String[] arg) {
    if (arg.length != 5) {
      printHelp();
      return;
    }
    
    // 스프링 컨테이너로부터 이름이 "memberRegisterService"인 빈 객체를 구한다.
    MemberRegisterService registerService =
        ctx.getBean("memberRegisterService", MemberRegisterService.class);
    RegisterRequest req = new RegisterRequest();
    ...
  }

  private static void processChangeCommand(String[] arg) {
    if (arg.length != 4) {
      printHelp();
      return;
    }

    // 스프링 컨테이너로부터 이름이 "changePasswordService"인 빈 객체를 구한다.
    ChangePasswordService changePasswordService
        = ctx.getBean("changePasswordService", ChangePasswordService.class);
    ...
  }
  ...
}
```

* **발생 가능 에러**
  * **NoSuchBeanDefinitionException**
    * @Bean을 붙이지 않은 경우에 getBean() 메서드를 실행할 때 발생.
    * getBean() 메서드를 호출할 때 빈 이름을 잘못 지정했을때 발생.

<br>

## 6.2. DI 방식1: 생성자 방식

생성자를 통해 의존 객체를 주입받아 필드에 할당한다.

```java
public class MemberRegisterService {
  private MemberDao memberDao;
  
  // 생성자를 통해 의존 객체를 주입 받음
  public MemberRegisterService(MemberDao memberDao) {
    // 주입 받은 객체를 필드에 할당
    this.memberDao = memberDao;
  }
  
  public Long regist(RegisterRequest req) {
    // 주입 받은 의존 객체의 메서드를 사용
    Member member = memberDao.selectByEmail(req.getEmail());
    ...
    memberDao.insert(newMember);
    return newMember.getId();
  }
}
```

스프링에서는 의존 객체를 주입하기 위해 해당 설정을 담은 메서드를 호출했다.

```java
@Bean
public MemberDao memberDao() {
  return new MemberDao();
}

@Bean
public MemberRegisterService memberRegSvc() {
  return new MemberRegisterService(memberDao());
}
```

<br>

생성자에 파라미터가 두 개인 예제를 살펴보자.

```java
public class MemberDao {
  ...
  public Collection<Member> selectAll() {
    return map.values();
  }
}
```

* **spring/MemberPrinter.java**

  ```java
  package spring;
  
  public class MemberPrinter {
  
    public void print(Member member) {
      System.out.printf(
          "회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%tF\n",
          member.getId(), member.getEmail(), member.getName(),
          member.getRegisterDateTime()
      );
    }
  
  }
  ```

* **spring/MemberListPrinter.java**

  ```java
  package spring;
  
  import java.util.Collection;
  
  public class MemberListPrinter {
  
    private MemberDao memberDao;
    private MemberPrinter printer;
  
    public MemberListPrinter(MemberDao memberDao, MemberPrinter printer) {
      this.memberDao = memberDao;
      this.printer = printer;
    }
  
    public void printAll() {
      Collection<Member> members = memberDao.selectAll();
      members.forEach(m -> printer.print(m));
    }
  
  }
  ```

* **config/AppCtx.java**

  ```java
  package config;
  
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import spring.*;
  
  // @Configuration : 스프링 설정 클래스를 의미한다.
  @Configuration
  public class AppCtx {
  
    // @Bean : 해당 메서드가 생성한 객체를 스프링 빈이라고 부른다.
    //  memberDao 라는 이름으로 스프링에 등록된다.
    @Bean
    public MemberDao memberDao() {
      return new MemberDao();
    }
    
    ...
    
    @Bean
    public MemberPrinter memberPrinter() {
      return new MemberPrinter();
    }
  
    @Bean
    public MemberListPrinter listPrinter() {
      return new MemberListPrinter(memberDao(), memberPrinter());
    }
  
  }
  ```

* **spring/MainForSpring.java**

  ```java
  ...
    
  public class MainForSpring {
  
    private static ApplicationContext ctx = null;
  
    public static void main(String[] args) throws IOException {
      // AnnotationConfigApplicationContext 를 사용해서 스프링 컨테이너를 생성한다.
      //  설정 파일(AppCtx 클래스)로부터 생성할 객체와 의존 주입 대상을 정한다.
      ctx = new AnnotationConfigApplicationContext(AppCtx.class);
      
      ...
  
      while (true) {
        ...
        else if (command.equals("list")) {
          processListCommand();
          continue;
        }
        printHelp();
      }
    }
  
    private static void processListCommand() {
      // 스프링 컨테이너에서 "listPrinter" 빈 객체를 구한다.
      //  이 빈 객체는 생성자를 통해서 MemberDao 객체와 MemberPrinter 객체를 주입 다는다.
      MemberListPrinter listPrinter =
          ctx.getBean("listPrinter", MemberListPrinter.class);
      listPrinter.printAll();
    }
    
    ...
  
  }
  ```

* **실행 결과**

  ```
  3:18:22 오후: Executing task 'MainForSpring.main()'...
  
  > Task :compileJava
  > Task :processResources NO-SOURCE
  > Task :classes
  
  > Task :MainForSpring.main()
  명령어를 입력하세요: 
  new abc a 13 13
  등록했습니다.
  
  명령어를 입력하세요: 
  list
  회원 정보: 아이디=1, 이메일=abc, 이름=a, 등록일=2020-02-11
  명령어를 입력하세요: 
  ```

<br>

## 6.3. DI 방식2: 세터 메서드 방식

생성자 외에 세터 메서드를 이용해서 객체를 주입받기도 한다. 일반적인 세터(setter) 메서드는 자바빈 규칙에 따라 작성된다.

* **자바빈 규칙**
  * 메서드 이름이 set으로 시작한다.
  * set 뒤에 첫 글자는 대문자로 시작한다.
  * 파라미터가 1개 이다.
  * 리턴 타입이 void 이다.
* 자바빈에서는 getter와 setter를 이용해서 프로퍼티를 정의한다. setAge와 같은 쓰기 메서드는 프로퍼티 값을 변경하므로 **프로퍼티 설정 메서드라고도 부른다.**

<br>

세터 메서드를 이용해서 의존 객체를 주입받는 코드를 작성해보자.

* **spring/MemberInfoPrinter.java**

  ```java
  package spring;
  
  public class MemberInfoPrinter {
  
    private MemberDao memberDao;
    private MemberPrinter printer;
  
    public void printMemberInfo(String email) {
      Member member = memberDao.selectByEmail(email);
      if (member == null) {
        System.out.println("데이터 없음\n");
        return;
      }
      printer.print(member);
      System.out.println();
    }
  
    // 의존 주입을 위한 세터 메서드
    public void setMemberDao(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
  
    // 의존 주입을 위한 세터 메서드
    public void setPrinter(MemberPrinter printer) {
      this.printer = printer;
    }
  }
  ```

* **config/AppCtx.java**

  ```java
  ...
  
  // @Configuration : 스프링 설정 클래스를 의미한다.
  @Configuration
  public class AppCtx {
  
    ...
  
    @Bean
    public MemberInfoPrinter infoPrinter() {
      MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
      // 의존 객체 주입
      infoPrinter.setMemberDao(memberDao());
      infoPrinter.setPrinter(memberPrinter());
      return infoPrinter;
    }
  
  }
  ```

* **main/MainForSpring.java**

  ```java
  package main;
  
  import config.AppCtx;
  import org.springframework.context.ApplicationContext;
  import org.springframework.context.annotation.AnnotationConfigApplicationContext;
  import spring.*;
  
  import java.io.BufferedReader;
  import java.io.IOException;
  import java.io.InputStreamReader;
  
  public class MainForSpring {
  
    private static ApplicationContext ctx = null;
  
    public static void main(String[] args) throws IOException {
      ...
  
      while (true) {
        ...
  
        else if (command.startsWith("info ")) {
          processInfoCommand(command.split(" "));
          continue;
        }
        printHelp();
      }
    }
    
    private static void processInfoCommand(String[] arg) {
      if (arg.length != 2) {
        printHelp();
        return;
      }
      MemberInfoPrinter infoPrinter =
          ctx.getBean("infoPrinter", MemberInfoPrinter.class);
      infoPrinter.printMemberInfo(arg[1]);
    }
  
    ...
      
  }
  ```

* **실행 결과**

  ```
  3:39:09 오후: Executing task 'MainForSpring.main()'...
  
  > Task :compileJava
  > Task :processResources NO-SOURCE
  > Task :classes
  
  > Task :MainForSpring.main()
  명령어를 입력하세요: 
  new abc a 123 123
  등록했습니다.
  
  명령어를 입력하세요: 
  info abc
  회원 정보: 아이디=1, 이메일=abc, 이름=a, 등록일=2020-02-11
  
  명령어를 입력하세요: 
  ```

  <br>

### 생성자 vs 세터 메서드

* **생성자 DI 방식** : 빈객체를 생성하는 시점에 모든 의존 객체가 주입된다.
  * **장점** : 빈 객체를 생성하는 시점에 필요한 모든 의존 객체를 주입받기 때문에 객체를 사용할 때 완전한 상태로 사용할 수 있다.
  * **단점** : 만약 생성자의 파라미터 개수가 많을 경우 각 인자가 어떤 의존 객체를 설정하는지 코드를 확인해야한다.
* **설정 메서드 DI 방식** : 세터 메서드 이름을 통해 어떤 의존 객체가 주입되는지 알 수 있다.
  * **장점** : 메서드 이름만으로도 어떤 의존 객체를 설정하는지 쉽게 유추할 수 있다.
  * **단점** : 필요한 의존 객체를 전달하지 않아도 빈 객체가 생성되기 때문에 객체를 사용하는 시점에서  NullPointerException이 발생할 수 있다.

<br>

## 6.4. 기본 데이터 타입 값 설정

* **spring/VersionPrinter.java**

  ```java
  package spring;
  
  public class VersionPrinter {
    
    private int majorVersion;
    private int minorVersion;
    
    public void print() {
      System.out.printf("이 프로그램의 버전은 %d.%d 입니다.\n\n",
          majorVersion, minorVersion);
    }
  
    public void setMajorVersion(int majorVersion) {
      this.majorVersion = majorVersion;
    }
  
    public void setMinorVersion(int minorVersion) {
      this.minorVersion = minorVersion;
    }
    
  }
  ```

* **config/AppCtx.java**

  ```java
  ...
    
  @Configuration
  public class AppCtx {
  
    ...
    
    @Bean
    public VersionPrinter versionPrinter() {
      VersionPrinter versionPrinter = new VersionPrinter();
      versionPrinter.setMajorVersion(5);
      versionPrinter.setMinorVersion(0);
      return versionPrinter;
    }
  
  }
  ```

* **main/MainForSpring.java**

  ```java
  ...
  
  public class MainForSpring {
  
    private static ApplicationContext ctx = null;
  
    public static void main(String[] args) throws IOException {
      ...
  
      while (true) {
        
        ...
        
        else if (command.equals("version")) {
          processVersionCommand();
          continue;
        }
        printHelp();
      }
    }
  
    private static void processVersionCommand() {
      VersionPrinter versionPrinter =
          ctx.getBean("versionPrinter", VersionPrinter.class);
      versionPrinter.print();
    }
    
    ...
  
  }
  ```

* **실행 결과**

  ```
  3:49:25 오후: Executing task 'MainForSpring.main()'...
  
  Starting Gradle Daemon...
  Gradle Daemon started in 987 ms
  > Task :compileJava
  > Task :processResources NO-SOURCE
  > Task :classes
  
  > Task :MainForSpring.main()
  명령어를 입력하세요: 
  version
  이 프로그램의 버전은 5.0 입니다.
  
  명령어를 입력하세요: 
  ```

<br>

# 7. @Configuration 설정 클래스의 @Bean 설정과 싱글톤

* **config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
  
    @Bean
    public MemberDao memberDao() {
      return new MemberDao();
    }
  
    @Bean
    public MemberRegisterService memberRegisterService() {
      return new MemberRegisterService(memberDao());
    }
  
    @Bean
    public ChangePasswordService changePasswordService() {
      ChangePasswordService passwordService = new ChangePasswordService();
      passwordService.setMemberDao(memberDao());
      return passwordService;
    }
    
  }
  ```

  * memberDao() 가 새로운 MemberDao 객체를 생성해서 리턴하므로
  * MemberRegisterService 객체와 ChangePasswordService 객체는 서로 다른  MemberDao 객체를 사용하는 것인가?

* 스프링 컨테이너는 **@Bean이 붙은 메서드에 대해 한 개의 객체만 생성한다.** 

* 스프링은 설정 클래스를 그대로 사용하지 않는다. 대신 설정 클래스를 상속한 새로운 설정 클래스를 만들어서 사용한다.

* 스프링이 런타임에 생성한 설정 클래스는 다음과 유사한 방식으로 동작한다.

  ```java
  public class AppCtxExt extends AppCtx {
    
    private Map<String, Object> beans = ...;
    
    @Override
    public MemberDao memberDao() {
      if (!beans.containsKey("memberDao"))
        beans.put("memberDao" super.memberDao());
      
      return (MemberDao) beans.get("memberDao");
    }
    
  }
  ```

  > 위의 코드는 가상의 코드일 뿐 실제 스프링 코드는 훨씬 복잡하다.

  * 스프링이 런타임에 생성한 설정 클래스의 memberDao() 메서드는 매번 새로운 객체를 생성하지 않는다.
  * 대신 한 번 생성한 객체를 보관했다가 이후에는 **동일한 객체를 리턴한다.**

<br>

# 8. 두 개 이상의 설정 파일 사용하기

설정하는 빈의 개수가 증가하면 한 개의 클래스 파일에 설정하는 것보다 영역별로 설정 파일을 나누면 관리하기 편해진다.

스프링은 한 개 이상의 설정 파일을 이용해서 컨테이너를 생성할 수 있다.

<br>

빈 설정을 나눠보자.

* **config/AppConf1.java**

  ```java
  package config;
  
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import spring.MemberDao;
  import spring.MemberPrinter;
  
  @Configuration
  public class AppConf1 {
    
    @Bean
    public MemberDao memberDao() {
      return new MemberDao();
    }
    
    @Bean
    public MemberPrinter memberPrinter() {
      return new MemberPrinter();
    }
    
  }
  ```

* **config/AppConf2.java**

  ```java
  package config;
  
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import spring.*;
  
  @Configuration
  public class AppConf2 {
  
    // @Autowired : 스프링의 자동 주입 기능을 위한 것이다.
    //  스프링 설정 클래스의 필드에 @Autowired 애노테이션을 붙이면
    //  해당 타입의 빈을 memberDao 필드에 할당한다.
    //  AppConf1 클래스에 MemberDao 탕비의 빈을 설정했으므로
    //  AppConf2 클래스의 membreDao 필드에는 AppConf1 클래스에서 설정한
    //  빈이 할당된다.
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private MemberPrinter memberPrinter;
  
    @Bean
    public MemberRegisterService memberRegisterService() {
      return new MemberRegisterService(memberDao);
    }
  
    @Bean
    public ChangePasswordService changePasswordService() {
      ChangePasswordService changePasswordService = new ChangePasswordService();
      changePasswordService.setMemberDao(memberDao);
      return changePasswordService;
    }
  
    @Bean
    public MemberListPrinter listPrinter() {
      return new MemberListPrinter(memberDao, memberPrinter);
    }
  
    @Bean
    public MemberInfoPrinter infoPrinter() {
      MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
      infoPrinter.setMemberDao(memberDao);
      infoPrinter.setPrinter(memberPrinter);
      return infoPrinter;
    }
  
    @Bean
    public VersionPrinter versionPrinter() {
      VersionPrinter versionPrinter = new VersionPrinter();
      versionPrinter.setMinorVersion(0);
      versionPrinter.setMajorVersion(5);
      return versionPrinter;
    }
  
  }
  ```

* **main/MainForSpring.java**

  ```java
  public class MainForSpring {
  
    private static ApplicationContext ctx = null;
  
    public static void main(String[] args) throws IOException {
      // 설정 클래스가 두 개 이상이어도 아래와 같이
      // 설정 클래스 목록을 콤마로 구분해서 전달하면 된다.
      ctx = new AnnotationConfigApplicationContext(AppConf1.class, AppConf2.class);
      
      ...
  ```

<br>

## 8.1. @Configuration 애노테이션, 빈, @Autowired 애노테이션

* **@Autowired** : 스프링 빈에 의존하는 다른 빈을 자동으로 주입하고 싶을 때 사용한다.

스프링 컨테이너는 설정 클래스에서 사용한 @Autowired에 대해서도 자동 주입을 처리한다. 실제로 스프링은 **@Configuration 애노테이션이 붙은 설정 클래스를 내부적으로 스프링 빈으로 등록한다.** 그리고 다른 빈과 마찬가지로 **@Autowired가 붙은 대상에 대해 알맞는 빈을 자동으로 주입한다.**

<br>

## 8.2. @Import 애노테이션 사용

두 개 이상의 설정 파일을 사용할 때 **@Import 애노테이션을** 사용하는 방법도 있다.

@Import 애노테이션은 **함께 사용할 설정 클래스를 지정한다.**

<br>

AppConf1.java에 @Import 애노테이션을 추가해보자.

* **config/AppConfImport.java**

  ```java
  package config;
  
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.context.annotation.Import;
  import spring.MemberDao;
  import spring.MemberPrinter;
  
  @Configuration
  @Import(AppConf2.class)
  public class AppConfImport {
    
    @Bean
    public MemberDao memberDao() {
      return new MemberDao();
    }
    
    @Bean
    public MemberPrinter memberPrinter() {
      return new MemberPrinter();
    }
    
  }
  ```

  * @Import 애노테이션을 지정한 AppConf2 설정 클래스도 함께 사용하기 때문에 스프링 컨테이너를 사용할 때 AppConfImport 클래스만 사용하면 AppConf2 클래스의 설정도 함께 사용해서 컨테이너를 초기화한다.

* **main/MainForSpring.java**

  ```java
  public class MainForSpring {
  
    private static ApplicationContext ctx = null;
  
    public static void main(String[] args) throws IOException {
      ctx = new AnnotationConfigApplicationContext(AppConfImport.class);
      
      ...
  ```

<br>

@Import 애노테이션은 배열을 이용해서 두 개 이상의 설정 클래스도 지정할 수 있다.

* **config/AppConfImport.java**

  ```java
  package config;
  
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.context.annotation.Import;
  import spring.MemberDao;
  import spring.MemberPrinter;
  
  @Configuration
  @Import({AppConf1.class, AppConf2.class})
  public class AppConfImport {
  
  }
  ```

<br>

# 9. getBean() 메서드 사용

getBean() 메서드의 첫 번째 인자는 **빈의 이름이고** 두 번째 인자는 **빈의 타입이다.**

getBean() 메서드를 호출할 때 존재하지 않는 빈 이름을 사용하면 익셉션이 발생한다.

* 만약 Bean 객체 이름이 "versionPrinter" 인데 "versionPrinter2"를 getBean() 메서드에 인자로 주면 다음과 같은 익셉션이 발생한다.

  ```
  Exception in thread "main"
  org.springframework.beans.factory.NoSuchBeanDefinitionException:
  No bean named 'versionPrinter2' availavle
  ```

* 빈의 실제 타입과 getBean() 메서드에 지정한 타입이 다르면 다음과 같은 익셉션이 발생한다.

  ```
  Exception in thread "main"
  org.springframework.beans.factory.BeanNotOfRequiredTypeException:
  Bean named 'listPrinter' is expected to be of type 'spring.VersionPrinter' but was actually of type 'spring.MemberListPrinter'
  ```

* getBean() 메서드에 빈 이름을 지정하지 않고 타입만으로 빈을 구할 수도 있다

  * 해당 타입의 빈 객체가 없을 때

    ```
    Exception in thread "main"
    org.springframework.beans.factory.NoSuchBeanDefinitionException:
    No qualifying bean of type 'spring.MemberPrinter' available
    ```

  * 해당 타입의 빈 객체가 두 개 이상 존재할 때

    ```
    Exception in thraed "main"
    org.springframework.beans.factory.NoUniqueBeanDefinitionException:
    No qualifying bean of type 'spring.VersionPrinter' available: expected single matching bean but found 2: versionPrinter, oldVerionPrinter
    ```

<br>

# 10. 주입 대상 객체를 모두 빈 객체로 설정해야 하나?

주입할 객체가 꼭 스프링 빈이어야 할 필요는 없다. 

* **예시) MemberPrinter를 빈으로 등록하지 않고 객체로 주입**

  ```java
  @Configuration
  public class AppCtxNoMemberPrinterBean {
    private MemberPrinter printer = new MemberPrinter();	// 빈이 아님
    
    ...
      
    @Bean
    public MemberListPrinter listPrinter() {
      return new MemberListPrinter(memberDao(), printer);
    }
    
    @Bean
    public MemberInfoPrinter infoPrinter() {
      MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
      infoPrinter.setMemberDao(memberDao());
      infoPrinter.setPrinter(printer);
      return infoPrinter;
    }
    
    ...
  }
  ```

  * 위의 코드는 MemberPrinter를 빈으로 등록하지 않았다.
  * 객체를 스프링 빈으로 등록할 때와 등록하지 않을 때의 차이는 **스프링 컨테이너가 객체를 관리하는지 여부이다.**
  * 스프링 컨테이너는 자동 주입, 라이플사이클 관리 등 단순 객체 생성 외에 객체 관리를 위한 다양한 기능을 제공하는데 빈으로 등록한 객체에만 기능을 적용한다.
  * 스프링 컨테이너가 제공하는 관리 기능이 필요 없고 getBean() 메서드로 구할 필요가 없다면 빈 객체로 꼭 등록해야 하는 것은 아니다.
  * 그래도 **의존 주입 대상은 스프링 빈으로 등록하는 것이 보통이다.**