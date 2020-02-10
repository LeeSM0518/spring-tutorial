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