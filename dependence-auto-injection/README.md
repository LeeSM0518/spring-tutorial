# Chapter 04. 의존 자동 주입

* **이전의 의존 주입 코드**

  ```java
  @Configuration
  public class AppCtx {
    
    @Bean
    public MemberDao memberDao() {
      return new MemberDao();
    }
    
    @Bean
    public ChangePasswordService changePwdSvc() {
      ChangePasswordService pwdSvc = new ChangePasswordService();
      pwdSvc.setMemberDao(memberDao());	// 의존 주입
      return pwdSvc;
    }
    
  }
  ```

  * 의존 대상을 생성자나 메서드를 이용해서 직접 주입했었다.
  * 스프링에는 자동으로 의존하는 빈 객체를 주입해주는 기능이 있다.
  * 스프링에서 의존 자동 주입을 설정하려면 **@Autowired 애노테이션이나 @Resource 애노테이션을** 사용하면 된다.
  * 이에 대해서 뒤쪽에서 살펴보도록 하자.

<br>

# 1. 예제 프로젝트 준비

* **gradle 프로젝트를 준비한다.**

  ![image](https://user-images.githubusercontent.com/43431081/74693192-fd021700-522d-11ea-8bf3-9aa14ef403d3.png)

* **build.gradle 에 dependency를 추가한다.**

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

# 2. @Autowired 애노테이션을 이용한 의존 자동 주입

자동 주입 기능을 사용하면 스프링이 알아서 의존 객체를 찾아서 주입한다.

* **이전의 주입 코드**

  ```java
  @Bean
  public MemberDao memberDao() {
    return new MemberDao();
  }
  
  @Bean
  public ChangePasswordService changePwdSvc() {
    ChangePasswordService pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao());	// 의존 주입
    return pwdSvc;
  }
  ```

* **자동 주입 기능을 사용한 코드**

  ```java
  @Bean
  public MemberDao memberDao() {
    return new MemberDao();
  }
  
  @Bean
  public ChangePasswordService changePwdSvc() {
    ChangePasswordService pwdSvc = new ChangePasswordService();
    return pwdSvc;
  }
  ```

<br>

자동 주입 기능을 사용하는 것은 매우 간단하다. 의존을 주입할 대상에 **@Autowired 애노테이션을** 붙이기만 하면 된다.

* **spring/ChangePasswordService.java**

  ```java
  package spring;
  
  import org.springframework.beans.factory.annotation.Autowired;
  
  public class ChangePasswordService {
  
    // 자동 주입 기능
    @Autowired
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

  * @Autowired 애노테이션을 붙이면 설정 클래스에서 의존을 주입하지 않아도 된다.
  * @Autowired 애노테이션이 붙어 있으면 스프링이 해당 타입의 빈 객체를 찾아서 필드를 할당한다.

<br>

@Autowired 애노테이션을 통해 자동 주입을 하였으므로 설정 클래스를 수정해주자.

* **config/AppCtx.java**

  ```java
  package config;
  
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import spring.*;
  
  // @Configuration : 스프링 설정 클래스를 의미한다.
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
  //    passwordService.setMemberDao(memberDao());
      return passwordService;
    }
    
    ...
  ```

<br>

@Autowired 애노테이션은 메서드에도 붙일 수 있다. MemberInfoPrinter 클래스의 두 세터 메서드에 @Autowired 애노테이션을 붙여보자.

* **spring/MemberInfoPrinter.java**

  ```java
  package spring;
  
  import org.springframework.beans.factory.annotation.Autowired;
  
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
  
    @Autowired
    public void setMemberDao(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
  
    @Autowired
    public void setPrinter(MemberPrinter printer) {
      this.printer = printer;
    }
    
  }
  ```

<br>

MemberInfoPrinter 의 세터 메서드에 @Autowired 를 붙여줬으므로 설정 클래스도 변경해보자.

* **config/AppCtx.java**

  ```java
  package config;
  
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import spring.*;
  
  @Configuration
  public class AppCtx {
  
    @Bean
    public MemberDao memberDao() {
      return new MemberDao();
    }
    
    ...
  
    @Bean
    public MemberInfoPrinter infoPrinter() {
      MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
  //    infoPrinter.setMemberDao(memberDao());
  //    infoPrinter.setPrinter(memberPrinter());
      return infoPrinter;
    }
    
    ...
  ```

  * 빈 객체인 MemberInfoPrinter 의 세터 메서드에 @Autowired 애노테이션을 붙여줬으므로 스프링은 해당 메서드를 호출한다. 이때 메서드 파라미터 타입에 해당하는 빈 객체를 찾아 인자로 주입해준다.
  * 그래서, 설정 클래스에서 빈 객체를 생성할 때 세터 메서드로 의존을 주입했던 코드를 제거하게 된다.

<br>

## 2.1. 일치하는 빈이 없는 경우

@Autowired 애노테이션을 적용한 대상에 일치하는 빈이 없는 경우에는 어떻게 될까?

AppCtx 설정 클래스의 memberDao() 메서드를 주석 처리해보고 확인해보자.

* **config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
  
  //  @Bean
  //  public MemberDao memberDao() {
  //    return new MemberDao();
  //  }
    ...
  ```

  > memberDao()에 주석을 달고 실행해보자.

* **실행 결과**

  ```
  Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'spring.MemberDao' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}
  ```

  * 다른 클래스들에 Autowired 로 호출되는 MemberDao 빈이 존재하지 않는다고 알려주고 있다.

<br>

반대로 @Autowired 애노테이션을 붙인 주입 대상에 일치하는 빈이 두 개 이상이면 어떻게 되는지 확인해보자.

* **config/AppCtx.java**

  ```java
  //  @Bean
  //  public MemberPrinter memberPrinter() {
  //    return new MemberPrinter();
  //  }
  
  @Bean
  public MemberPrinter memberPrinter1() {
    return new MemberPrinter();
  }
  
  @Bean
  public MemberPrinter memberPrinter2() {
    return new MemberPrinter();
  }
  ```

  * memberPrinter 빈 설정 메서드를 주석을 달고 memberPrinter1, 2 로 작성한 뒤 실행해보자.

* **실행 결과**

  ```
  Caused by: org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'spring.MemberPrinter' available: expected single matching bean but found 2: memberPrinter1,memberPrinter2
  ```

  * 해당 타입 빈이 한 개가 아니라 이름이 memberPrinter1, memberPrinter2인 두 개의 빈을 발견했다는 에러를 볼 수 있다.

  * 자동 주입을 하려면 해당 타입을 가진 빈이 어떤 빈인지 정확하게 한정할 수 있어야 하는데 MemberPrinter 타입의 빈이 **두 개여서 어떤 빈을 자동 주입 대상으로 해야 할 지를 선택할 수 없어서 발생하는 익셉션이다.**

<br>

# 3. @Qualifier 애노테이션을 이용한 의존 객체 선택

위의 에러와 같이 자동 주입 가능한 빈이 두 개 이상이면 자동 주입할 빈을 지정할 수 있는 방법이 필요하다. 이때 @Qualifier 애노테이션을 사용한다. **@Qualifier 애노테이션을 사용하면 자동 주입 대상 빈을 한정할 수 있다.**

<br>

@Bean 과 @Qualifier 애노테이션을 함께 붙인 빈 설정 메서드 예시를 보자.

* **config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
  
    ...
  
    @Bean
    @Qualifier("printer")
    public MemberPrinter memberPrinter1() {
      return new MemberPrinter();
    }
  
    @Bean
    public MemberPrinter memberPrinter2() {
      return new MemberPrinter();
    }
    
    ...
  
  }
  ```

  * memberPrinter1() 메서드에 "printer" 값을 갖는 @Qualifier 애노테이션을 붙였다. 이 설정은 해당 빈의 한정 값으로 "printer"를 지정한다.

<br>

위와 같이 지정한 한정 값은 @Autowired 애노테이션에서 자동 주입할 빈을 한정할 때 사용한다.

* **spring/MemberListPrinter.java**

  ```java
  public class MemberListPrinter {
    
    ...
    @Autowired
    @Qualifier("printer")
    public void setPrinter(MemberPrinter printer) {
      this.printer = printer;
    }
    
  }
  ```

* **spring/MemberInfoPrinter.java**

  ```java
  public class MemberInfoPrinter {
  
    ...
  
    @Autowired
    @Qualifier("printer")
    public void setPrinter(MemberPrinter printer) {
      this.printer = printer;
    }
  
  }
  ```

* setPrinter() 메서드에 @Autowired 애노테이션을 붙였으므로 MemberPrinter 타입의 빈을 자동 주입한다.

* 이때 @Qulifier 애노테이션 값이 "printer" 이므로 한정 값이 "printer"인 빈을 의존 주입 후보로 사용한다.

* 이전에 스프링 설정 클래스에서 @Qualifier 애노테이션 값으로 "printer"를 준 **MemberPrinter1()** 을 자동 주입 대상으로 사용한다.

<br>

## 3.1. 빈 이름과 기본 한정자

빈 설정에 @Qualifier 애노테이션이 없으면 **빈의 이름을 한정자로 지정한다.**

* **config/AppCtx2.java**

  ```java
  @Configuration
  public class AppCtx2 {
    
    @Bean
    public MemberPrinter printer() {
      return new MemberPrinter();
    }
    
    @Bean
    @Qualifier("mprinter")
    public MemberPrinter printer2() {
      return new MemberPrinter();
    }
    
    @Bean
    public MemberInfoPrinter2 infoPrinter() {
      return new MemberInfoPrinter2();
    }
    
  }
  ```

  * 여기서 printer() 메서드로 정의한 빈의 한정자는 빈 이름인 **"printer"** 가 된다.
  * printer2 빈은 @Qualifier 애노테이션 값인 **"mprinter"** 가 한정자가 된다.

* **spring/MemberInfoPrinter2.java**

  ```java
  public class MemberInfoPrinter2 {
    
    @Autowired
    private MemberPrinter printer;
    
  }
  ```

  * @Autowired 애노테이션도 @Qualifier 애노테이션이 없으면 필드나 파라미터 이름을 한정자로 사용한다.
  * 위의 코드에서는 필드 이름인 **"printer"** 가 한정자가 된다.

<br>

* **빈 이름과 한정자 관계**

  | 빈 이름     | @Qulifier | 한정자      |
  | ----------- | --------- | ----------- |
  | printer     | x         | printer     |
  | printer2    | mprinter  | mprinter    |
  | infoPrinter | x         | infoPrinter |

<br>

# 4. 상위/하위 타입 관계와 자동 주입

MemberPrinter 클래스를 상속한 MemberSummaryPrinter 클래스를 생성해보자.

* **spring/MemberSummaryPrinter.java**

  ```java
  package spring;
  
  public class MemberSummaryPrinter extends MemberPrinter {
  
    @Override
    public void print(Member member) {
      System.out.printf("회원 정보: 이메일=%s, 이름=%s\n", 
          member.getEmail(), member.getName());
    }
    
  }
  ```

<br>

설정 클래스인 AppCtx 클래스 설정에서 memberPrinter2() 메서드가 MemberSummaryPrinter 타입의 빈 객체를 설정하도록 변경해보자.

* **config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
    
    ...
      
    @Bean
    public MemberPrinter memberPrinter1() {
      return new MemberPrinter();
    }
  
    @Bean
    public MemberSummaryPrinter memberPrinter2() {
      return new MemberSummaryPrinter();
    }
    
    ...
      
  }
  ```

  * 이전에 써놨던 @Qulifier 애노테이션들을 모두 제거한다. 그리고 다시 실행을 하게 되면 앞서 MemberPrinter 타입 빈을 두 개 설정하고 @Qulifier 애노테이션을 붙이지 않았을 때와 동일한 익셉션이 발생한다.

* **실행 결과**

  ```
  Caused by: org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'spring.MemberPrinter' available: expected single matching bean but found 2: memberPrinter1,memberPrinter2
  ```

  * 위와 같은 에러가 발생한 이유는 MemberSummaryPrinter 클래스가 MemberPrinter 를 상속했기 때문에 MemberPrinter 타입 빈을 자동 주입해야 하는 @Autowired 애노테이션을 만났을 때 **memberPrinter1 빈과 memberPrinter2 타입 빈 중 어떤 빈을 주입해야 할 지 알 수 없기 때문이다.**

<br>

위와 같은 에러를 해결하기 위해서는 **@Qualifier 애노테이션을 사용해서 주입할 빈을 한정해야 한다.**

* **config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
  
    ...
  
    @Bean
    @Qualifier("printer")
    public MemberPrinter memberPrinter1() {
      return new MemberPrinter();
    }
  
    @Bean
    @Qualifier("summaryPrinter")
    public MemberSummaryPrinter memberPrinter2() {
      return new MemberSummaryPrinter();
    }
  
    ...
  
  }
  ```

* **spring/MemberInfoPrinter.java**

  ```java
  public class MemberInfoPrinter {
  
    ...
  
    @Autowired
    @Qualifier("printer")
    public void setPrinter(MemberPrinter printer) {
      this.printer = printer;
    }
  
  }
  ```

* **spring/MemberListPrinter.java**

  ```java
  public class MemberListPrinter {
    
    ...
      
    @Autowired
    @Qualifier("summaryPrinter")
    public void setPrinter(MemberPrinter printer) {
      this.printer = printer;
    }
    
  }
  ```

위와 같이 **@Qualifier 애노테이션을** 통해서 한정자를 구분짓게 해줄 수 있고, 파라미터를 수정하는 방법도 있다.

<br>

* **spring/MemberListPrinter.java**

  ```java
  public class MemberListPrinter {
    ...
      
    @Autowired
    public void setMemberPrinter(MemberSummaryPrinter printer) {
      this.printer = printer;
    }
    
  }
  ```

<br>

# 5. @Autowired 애노테이션의 필수 여부

### 첫 번째 방법: @Autowired(required = false)

MemberPrinter 코드를 다음과 같이 바꿔보자.

*   **spring/MemberPrinter.java**

  ```java
  public class MemberPrinter {
    
    private DateTimeFormatter dateTimeFormatter;
  
    public void print(Member member) {
      if (dateTimeFormatter == null) {
        System.out.printf(
            "회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%tF\n",
            member.getId(), member.getEmail(), member.getName(),
            member.getRegisterDateTime()
        );
      } else {
        System.out.printf("회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%s\n",
            member.getId(), member.getEmail(), member.getName(), 
            dateTimeFormatter.format(member.getRegisterDateTime()));
      }
    }
  
    @Autowired
    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
      this.dateTimeFormatter = dateTimeFormatter;
    }
  }
  ```

  * setDateTimeFormatter 메서드에 @Autowired 애노테이션을 이용해서 자동 주입하도록 하였다.
  * print 메서드를 보면 dateTimeFormatter가 null인 경우에도 알맞게 동작한다. 즉 반드시 setDateFormatter()를 통해서 의존 객체를 주입할 필요는 없다.
  * 하지만, @Autowired 애노테이션은 기본적으로 애노테이션을 붙인 타입에 **해당 빈이 존재하지 않으면 익셉션이 발생한다.**
  * 우리가 원하는 것은 dateTimeFormatter 필드가 null 이 될 수도 있는 상황이기 때문에 코드를 변경해줘야 한다.

  ```java
  public class MemberPrinter {
  
    private DateTimeFormatter dateTimeFormatter;
  
    public void print(Member member) {
      ...
    }
  
    @Autowired(required = false)
    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
      this.dateTimeFormatter = dateTimeFormatter;
    }
  }
  ```

  * 자동 주입할 대상이 필수가 아닌 경우에는 @Autowired 애노테이션의 **required 속성을 위와 같이 false로 지정하면 된다.**
  * required 속성을 false로 지정하면 매칭되는 빈이 없어도 익셉션이 발생하지 않으며 자동 주입을 수행하지 않는다.

<br>

### 두 번째 방법: Optional

스프링 5 버전부터는 @Autowired 애노테이션의 required 속성을 false로 하는 대신에 다음과 같이 **의존 주입 대상에 자바 8의 Optional을 사용해도 된다.**

* **spring/MemberPrinter.java**

  ```java
  public class MemberPrinter {
  
    private DateTimeFormatter dateTimeFormatter;
  
    public void print(Member member) {
      ...
    }
  
  //  @Autowired(required = false)
  //  public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
  //    this.dateTimeFormatter = dateTimeFormatter;
  //  }
  
    @Autowired
    public void setDateTimeFormatter(Optional<DateTimeFormatter> formatterOpt) {
      this.dateTimeFormatter = formatterOpt.orElse(null);
    }
    
  }
  ```

  * 자동 주입 대상이 Optional인 경우, 일치하는 빈이 존재하지 않으면 값이 없는 Optional을 인자로 전달하고, 일치하는 빈이 존재하면 해당 빈을 값으로 갖는 Optional을 인자로 전달한다.
  * 위의 코드는 Optional 값이 존재하면 DateTimeFormatter 타입 빈을 주입 받아 필드에 할당하고, 존재하지 않으면 null을 할당한다.

<br>

### 세 번째 방법: @Nullable

* **spring/MemberPrinter.java**

  ```java
  public class MemberPrinter {
  
    private DateTimeFormatter dateTimeFormatter;
  
    public void print(Member member) {
      ...
    }
  
    @Autowired
    public void setDateTimeFormatter(@Nullable DateTimeFormatter dateTimeFormatter) {
      this.dateTimeFormatter = dateTimeFormatter;
    }
  
  }
  ```

  * @Autowired 애노테이션을 붙인 세터 메서드에서 @Nullable 애노테이션을 의존 주입 대상 파라미터에 붙이면, 스프링 컨테이너는 세터 메서드를 호출할 때 **자동 주입할 빈이 존재하면 해당 빈을 인자로 전달하고, 존재하지 않으면 인자로 null을 전달한다.**
  * @Autowired 어노테이션의 required 속성을 false로 할 때와 차이점은 @Nullable 애노테이션을 사용하면 자동 주입할 빈이 존재하지 않아도 메서드가 호출된다.
  * @Autowired 어노테이션의 required 속성을 false로 하였을 때는 빈이 존재하지 않으면 세터 메서드를 호출하지 않는다.

<br>

## 5.1. 생성자 초기화와 필수 여부 지정 방식 동작 이해

* **자동 주입 대상 필드를 기본 생성자에서 초기화한 예. spring/MemberPrinter.java**

  ```java
  public class MemberPrinter {
  
    private DateTimeFormatter dateTimeFormatter;
  
    public MemberPrinter() {
      dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    }
  
    public void print(Member member) {
      if (dateTimeFormatter == null) {
        System.out.printf(
          "회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%tF\n",
          member.getId(), member.getEmail(), member.getName(),
          member.getRegisterDateTime()
        );
      } else {
        System.out.printf("회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%s\n",
                          member.getId(), member.getEmail(), member.getName(),
                          dateTimeFormatter.format(member.getRegisterDateTime()));
      }
    }
  
    @Autowired(required = false)
    public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
      this.dateTimeFormatter = dateTimeFormatter;
    }
  
  }
  ```

  * 이 코드는 기본 생성자에서 dateTimeFormatter 필드의 값을 초기화한다. 그리고 @Autowired 애노테이션의 required 속성을 false로 지정하였다.

* **실행 결과**

  ```
  명령어를 입력하세요: 
  new a ac 123 123
  등록했습니다.
  
  명령어를 입력하세요: 
  list
  회원 정보: 이메일=a, 이름=ac
  명령어를 입력하세요: 
  info a
  회원 정보: 아이디=1, 이메일=a, 이름=ac, 등록일=2020년 02월 18일
  
  명령어를 입력하세요:
  ```

  * @Autowired 애노테이션의 required 속성이 false이면 일치하는 빈이 존재하지 않을 때 자동 주입 대상이 되는 필드나 메서드에 null을 전달하지 않는다는 것을 알 수 있다.
  * 왜냐하면 기본 생성자에서 **"yyyy년 MM월 dd일"** 로 설정한데로 등록일이 출력된 것을 보면 dateTimeFormatter가 null이 아니므로 dateTimeFormatter.format(~) 을 호출했다는 것을 알 수 있다.

<br>

위의 코드에서 @Autowired(required = false) 대신에 @Nullable을 사용하도록 바꿔보자.

* **spring/MemberPrinter.java**

  ```java
  public class MemberPrinter {
  
    private DateTimeFormatter dateTimeFormatter;
  
    public MemberPrinter() {
      dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    }
  
    public void print(Member member) {
      if (dateTimeFormatter == null) {
        System.out.printf(
            "회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%tF\n",
            member.getId(), member.getEmail(), member.getName(),
            member.getRegisterDateTime()
        );
      } else {
        System.out.printf("회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%s\n",
            member.getId(), member.getEmail(), member.getName(),
            dateTimeFormatter.format(member.getRegisterDateTime()));
      }
    }
  
    @Autowired
    public void setDateTimeFormatter(@Nullable DateTimeFormatter dateTimeFormatter) {
      this.dateTimeFormatter = dateTimeFormatter;
    }
  
  }
  ```

* **실행 결과**

  ```
  명령어를 입력하세요: 
  new a ab 123 123
  등록했습니다.
  
  명령어를 입력하세요: 
  info a
  회원 정보: 아이디=1, 이메일=a, 이름=ab, 등록일=2020-02-18
  
  명령어를 입력하세요: 
  ```

  * **@Nullable 애노테이션을 사용할 경우 스프링 컨테이너는 의존 주입 대상이 존재하지 않으면 null을 값으로 전달한다.** 그래서 스프링 컨테이너는 빈을 초기화하기 위해 기본 생성자를 이용해서 객체를 생성하고 의존 자동 주입을 처리하기 위해 setDateFormatter() 메서드를 호출한다. **이때 기본 생성자에서 dateTimeFormatter 필드를 초기화해도 setDateFormatter() 메서드가 null로 다시 저장하기 때문에 위와 같은 결과를 초래하는 것을 볼 수 있다.**

<br>

정리하자면, 일치하는 빈이 없으면 **값 할당 자체를 하지 않는 @Autowired(required = false)와** 달리 **@Nullable 애노테이션을 사용하면 일치하는 빈이 없을 때 null 값을 할당한다.** 유사하게 **Optional 타입은 매칭되는 빈이 없으면 값이 없는 Optional을 할당한다.** 기본 생성자에서 자동 주입 대상이 되는 필드를 초기화할 때는 이 점에 유의해야 한다.

<br>

# 6. 자동 주입과 명시적 의존 주입 간의 관계

설정 클래스에서 의존을 주입했는데 자동 주입 대상이면 어떻게 될까? 

AppCtx 설정 클래스의 infoPrinter() 메서드를 변경해보자.

* **config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
  
    ...
  
    @Bean
    @Qualifier("printer")
    public MemberPrinter memberPrinter1() {
      return new MemberPrinter();
    }
  
    @Bean
    @Qualifier("summaryPrinter")
    public MemberSummaryPrinter memberPrinter2() {
      return new MemberSummaryPrinter();
    }
  
    @Bean
    public MemberListPrinter listPrinter() {
      return new MemberListPrinter();
    }
  
    @Bean
    public MemberInfoPrinter infoPrinter() {
      MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
      infoPrinter.setPrinter(memberPrinter2());
      return infoPrinter;
    }
    
    ...
  
  }
  ```

  * infoPrinter() 메서드는 setPrinter() 메서드를 호출해서 memberPrinter2 빈을 주입하고 있다. memberPrinter2 빈은 MemberSummaryPrinter 객체이므로 이메일과 이름만 출력한다.

    * **spring/MemberSummaryPrinter.java**

      ```java
      public class MemberSummaryPrinter extends MemberPrinter {
      
        @Override
        public void print(Member member) {
          System.out.printf("회원 정보: 이메일=%s, 이름=%s\n",
              member.getEmail(), member.getName());
        }
      
      }
      ```

  * 하지만, MemberInfoPrinter 클래스의 setPrinter() 메서드는 다음과 같이 @Autowired 애노테이션이 붙어 있다.

    * **spring/MemberInfoPrinter.java**

      ```java
      public class MemberInfoPrinter {
      
        ...
      
        @Autowired
        @Qualifier("printer")
        public void setPrinter(MemberPrinter printer) {
          this.printer = printer;
        }
      
      }
      ```

  * 즉, 설정 클래스에서는 명시적 의존 주입을 정의했고 MemberInfoPrinter 클래스에서는 자동 주입을 정의했다. 이 상태에서 실행을 해보자.

* **실행 결과**

  ```
  명령어를 입력하세요: 
  new a b 123 123
  등록했습니다.
  
  명령어를 입력하세요: 
  info a
  회원 정보: 아이디=1, 이메일=a, 이름=b, 등록일=2020-02-18
  
  명령어를 입력하세요: 
  ```

  * 설정 클래스에서 세터 메서드를 통해 의존을 주입해도 해당 세터 메서드에 @Autowired 애노테이션이 붙어 있으면 자동 주입을 통해 일치하는 빈을 주입한다.
  * 즉, 설정 클래스에서 객체를 주입하기보다는 **스프링이 제공하는 자동 주입 기능(@Autowired)을 사용하는 편이 낫다.**

<br>

> 자동 주입을 하는 코드와 수동으로 주입하는 코드가 섞여 있으면 주입을 제대로 하지 않아서 **NullPointerException이** 발생했을 때 원인을 찾는데 오랜 시간이 걸릴 수 있다. 즉, 일관되게 자동 주입 코드를 적용하는것이 좋다.

<br>

