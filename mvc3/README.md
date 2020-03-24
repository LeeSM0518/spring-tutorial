# Chapter 13. MVC 3: 세션, 인터셉터, 쿠키

# 1. 프로젝트 준비

1. gradle

   ```java
   plugins {
       id 'java'
   }
   
   group 'org.example'
   version '1.0-SNAPSHOT'
   
   sourceCompatibility = 1.8
   compileJava.options.encoding("UTF-8")
   
   apply plugin: 'java'
   apply plugin: 'war'
   
   repositories {
       mavenCentral()
   }
   
   dependencies {
       testCompile group: 'junit', name: 'junit', version: '4.12'
       implementation 'javax.servlet:javax.servlet-api:4.0.1'
       implementation 'javax.servlet.jsp:javax.servlet.jsp-api:2.3.3'
       implementation 'jstl:jstl:1.2'
       implementation 'org.springframework:spring-webmvc:5.2.4.RELEASE'
       implementation 'org.springframework:spring-jdbc:5.2.4.RELEASE'
       implementation 'org.apache.tomcat:tomcat-jdbc:10.0.0-M1'
       implementation 'org.postgresql:postgresql:42.2.11.jre7'
       implementation 'org.springframework:springloaded:1.2.8.RELEASE'
       implementation 'org.slf4j:slf4j-api:2.0.0-alpha1'
       implementation 'javax.validation:validation-api:2.0.1.Final'
       implementation 'org.hibernate.validator:hibernate-validator:6.1.2.Final'
   }
   ```

<br>

# 2. 로그인 처리를 위한 코드 준비

로그인 성공 후 **인증 상태 정보를 세션에 보관할 때 사용할** AuthInfo 클래스를 작성하자.

* **java/spring/AuthInfo.java**

  ```java
  public class AuthInfo {
    
    private Long id;
    private String email;
    private String name;
  
    public AuthInfo(Long id, String email, String name) {
      this.id = id;
      this.email = email;
      this.name = name;
    }
  
    public Long getId() {
      return id;
    }
  
    public String getEmail() {
      return email;
    }
  
    public String getName() {
      return name;
    }
    
  }
  ```

<br>

암호 일치 여부를 확인하기 위한 matchPassword() 메서드를 Member 클래스에 추가하자.

* **java/spring/Member.java**

  ```java
  public class Member {
    
    ...
    
    public boolean matcherPassword(String password) {
      return this.password.equals(password);
    }
  
  }
  ```

<br>

이메일과 비밀번호가 일치하는지 확인해서 AuthInfo 객체를 생성하는 AuthService 클래스를 작성하자.

* **java/spring/AuthService.java**

  ```java
  public class AuthService {
  
    private MemberDao memberDao;
  
    public void setMemberDao(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
  
    public AuthInfo authenticate(String email, String password) {
      Member member = memberDao.selectByEmail(email);
      if (member == null) {
        throw new WrongPasswordException();
      }
      if (!member.matcherPassword(password)) {
        throw new WrongPasswordException();
      }
  
      return new AuthInfo(member.getId(),
          member.getEmail(),
          member.getName());
    }
  
  }
  ```

<br>

이제 AuthService를 이용해서 로그인 요청을 처리하는 LoginController 클래스를 작성하자.

폼에 입력한 값을 전달받기 위한 LoginCommand 클래스와 폼에 입력된 값이 올바른지 검사하기 위한 LoginCommandValidator 클래스를 작성해보자.

* **java/controller/LoginCommand.java**

  ```java
  public class LoginCommand {
    
    private String email;
    private String password;
    private boolean rememberEmail;
  
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
  
    public boolean isRememberEmail() {
      return rememberEmail;
    }
  
    public void setRememberEmail(boolean rememberEmail) {
      this.rememberEmail = rememberEmail;
    }
  }
  ```

* **java/controller/LoginCommandValidator.java**

  ```java
  public class LoginCommandValidator implements Validator {
  
    @Override
    public boolean supports(Class<?> clazz) {
      return LoginCommand.class.isAssignableFrom(clazz);
    }
  
    @Override
    public void validate(Object target, Errors errors) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "required");
      ValidationUtils.rejectIfEmpty(errors, "password", "required");
    }
  
  }
  ```

* **java/controller/LoginController.java**

  ```java
  @Controller
  @RequestMapping("/login")
  public class LoginController {
  
    private AuthService authService;
  
    public void setAuthService(AuthService authService) {
      this.authService = authService;
    }
  
    @GetMapping
    public String form(LoginCommand loginCommand) {
      return "login/loginForm";
    }
  
    @PostMapping
    public String submit(LoginCommand loginCommand, Errors errors) {
      new LoginCommandValidator().validate(loginCommand, errors);
      if (errors.hasErrors()) {
        return "login/loginForm";
      }
      try {
        AuthInfo authInfo = authService.authenticate(
          loginCommand.getEmail(),
          loginCommand.getPassword());
  
        //      TODO 세션에 authinfo 저장해야 함
        return "login/loginSuccess";
      } catch (WrongPasswordException e) {
        errors.reject("idPasswordNotMatching");
        return "login/loginForm";
      }
    }
  
  }
  ```

* **webapp/WEB-INF/view/login/loginForm.jsp**

  ```jsp
  <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
  <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
  <%@ taglib prefix="from" uri="http://www.springframework.org/tags/form" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title><spring:message code="login.title"/></title>
    </head>
    <body>
      <%--@elvariable id="loginCommand" type="controller.LoginCommand"--%>
      <form:form modelAttribute="loginCommand">
        <from:errors/>
        <p>
          <label><spring:message code="email"/>:<br>
            <form:input path="email"/>
            <form:errors path="email"/>
          </label>
        </p>
        <p>
          <label><spring:message code="password"/>:<br>
            <form:password path="password"/>
            <form:errors path="password"/>
          </label>
        </p>
        <input type="submit" value="<spring:message code="login.btn"/>">
      </form:form>
    </body>
  </html>
  ```

* **webapp/WEB-INF/view/login/loginSuccess.jsp**

  ```jsp
  <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title><spring:message code="login.done"/></title>
    </head>
    <body>
      <p>
        <spring:message code="login.done"/>
      </p>
      <p>
        <a href="<c:url value='/main'/>">
          [<spring:message code="go.main"/>]
        </a>
      </p>
    </body>
  </html>
  ```

* **resources/message/label.properties (메시지 추가)**

  ```properties
  ...
  login.title=로그인
  login.btn=로그인하기
  idPasswordNotMatching=아이디와 비밀번호가 일치하지 않습니다.
  login.done=로그인에 성공했습니다.
  ```

* **java/config/MemberConfig.java**

  ```jsp
  
  ```

  