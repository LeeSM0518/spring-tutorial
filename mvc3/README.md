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

  ```java
  @Configuration
  @EnableTransactionManagement
  public class MemberConfig {
  
    ... 생략
  
    @Bean
    public AuthService authService() {
      AuthService authService = new AuthService();
      authService.setMemberDao(memberDao());
      return authService;
    }
  
  }
  ```

* **java/config/ControllerConfig.java**

  ```java
  @Configuration
  public class ControllerConfig {
  
    @Autowired
    private MemberRegisterService memberRegSvc;
    @Autowired
    private AuthService authService;
  
    @Bean
    public RegisterController registerController() {
      RegisterController controller = new RegisterController();
      controller.setMemberRegisterService(memberRegSvc);
      return controller;
    }
  
    @Bean
    public SurveyController surveyController() {
      return new SurveyController();
    }
    
    @Bean
    public LoginController loginController() {
      LoginController controller = new LoginController();
      controller.setAuthService(authService);
      return controller;
    }
  
  }
  ```

<br>

# 3. 컨트롤러에서 HttpSession 사용하기

로그인 상태를 유지하는 방법은 크게 **HttpSession을 이용하는 방법과 쿠키를 이용하는 방법이 있다.** 

컨트롤러에서 HttpSession을 사용하려면 다음의 두 가지 방법 중 한 가지를 사용하면 된다.

<br>

* **요청 매핑 애노테이션 적용 메서드에 HttpSession 파라미터를 추가한다.**

  ```java
  @PostMapping
  public String form(LoginCommand command, Errors errors, HttpSession session) {
    ... // session을 사용하는 코드
  }
  ```

  * 스프링 MVC는 컨트롤러의 메서드를 호출할 때 HttpSession 객체를 파라미터로 전달한다.

* **요청 매핑 애노테이션 적용 메서드에 HttpServletRequest 파라미터를 추가하고 HttpServletRequest를 이용해서 HttpSession을 구한다.**

  ```java
  @PostMapping
  public String submit(
    LoginCommand command, Errors errors, HttpServletRequest req) {
    HttpSession session = req.getSession();
    ... // session을 사용하는 코드
  }
  ```

  * 첫 번째 방법은 항상 HttpSession을 생성하지만 두 번째 방법은 필요한 시점에만 HttpSession을 생성한다.

<br>

LoginController 코드에서 인증 후에 인증 정보를 세션에 담도록 수정해보자.

* **java/controller/LoginController.java**

  ```java
  @Controller
  @RequestMapping("/login")
  public class LoginController {
  
    ...
  
    @PostMapping
    public String submit(LoginCommand loginCommand, Errors errors, 
                         // HttpSession을 파라미터로 추가
                         HttpSession session) {
      new LoginCommandValidator().validate(loginCommand, errors);
      if (errors.hasErrors()) {
        return "login/loginForm";
      }
      try {
        AuthInfo authInfo = authService.authenticate(
          loginCommand.getEmail(),
          loginCommand.getPassword());
  
        // HttpSession에 "authInfo" 속성에 인증 정보 객체 저장
        session.setAttribute("authInfo", authInfo);
  
        return "login/loginSuccess";
      } catch (WrongPasswordException e) {
        errors.reject("idPasswordNotMatching");
        return "login/loginForm";
      }
    }
  
  }
  ```

<br>
로그아웃을 위한 컨트롤러 클래스는 **HttpSession을 제거하면 된다.**

* **java/controller/LogoutController.java**

  ```java
  @Controller
  public class LogoutController {
    
    @RequestMapping("/logout")
    public String logout(HttpSession httpSession) {
      httpSession.invalidate();
      return "redirect:/main";
    }
    
  }
  ```

새로운 컨트롤러를 구현했으므로 스프링 설정에 빈을 추가한다.

* **java/config/ControllerConfig.java**

  ```java
  @Configuration
  public class ControllerConfig {
  
    ...
    
    @Bean
    public LogoutController logoutController() {
      return new LogoutController();
    }
  
  }
  ```

<br>

정상 작동을 확인하기 위해 main.jsp를 수정해보자.

* **webapp/WEB-INF/view/main.jsp**

  ```jsp
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title>메인</title>
    </head>
    <body>
      <c:if test="${empty authInfo}">
        <p>환영합니다.</p>
        <p>
          <a href="<c:url value="/register/step1"/>">[회원 가입하기]</a>
          <a href="<c:url value="/login"/>">[로그인]</a>
        </p>
      </c:if>
  
      <c:if test="${!empty authInfo}">
        <p>${authInfo.name}님, 환영합니다.</p>
        <p>
          <a href="<c:url value="/edit/changePassword"/>">[비밀번호 변경]</a>
          <a href="<c:url value="/logout"/>">[로그아웃]</a>
        </p>
      </c:if>
    </body>
  </html>
  ```

<br>

# 4. 비밀번호 변경 기능 구현

먼저 비밀번호 변경에 사용할 커맨드 객체와 Validator 클래스를 작성하자.

* **java/controller/ChangePwdCommand.java**

  ```java
  public class ChangePwdCommand {
  
    private String currentPassword;
    private String newPassword;
  
    public String getCurrentPassword() {
      return currentPassword;
    }
  
    public void setCurrentPassword(String currentPassword) {
      this.currentPassword = currentPassword;
    }
  
    public String getNewPassword() {
      return newPassword;
    }
  
    public void setNewPassword(String newPassword) {
      this.newPassword = newPassword;
    }
  
  }
  ```

* **java/controller/ChangePwdCommandValidator.java**

  ```java
  public class ChangePwdCommandValidator implements Validator {
  
    @Override
    public boolean supports(Class<?> clazz) {
      return ChangePwdCommand.class.isAssignableFrom(clazz);
    }
  
    @Override
    public void validate(Object target, Errors errors) {
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentPassword", "required");
      ValidationUtils.rejectIfEmpty(errors,"newPassword", "required");
    }
  
  }
  ```

<br>

비밀번호 변경 요청을 처리하는 컨트롤러 클래스를 작성해보자.

* **java/controller/ChangePwdController.java**

  ```java
  @Controller
  @RequestMapping("/edit/changePassword")
  public class ChangePwdController {
  
    private ChangePasswordService changePasswordService;
  
    public void setChangePasswordService(ChangePasswordService changePasswordService) {
      this.changePasswordService = changePasswordService;
    }
  
    @GetMapping
    public String form(
      @ModelAttribute("command") ChangePwdCommand pwdCmd) {
      return "edit/changePwdForm";
    }
  
    @PostMapping
    public String submit(
      @ModelAttribute("command") ChangePwdCommand pwdCmd, Errors errors, HttpSession session) {
      new ChangePwdCommandValidator().validate(pwdCmd, errors);
      if (errors.hasErrors()) {
        return "edit/changePwdForm";
      }
      AuthInfo authInfo = (AuthInfo) session.getAttribute("authInfo");
      try {
        changePasswordService.changePassword(
          authInfo.getEmail(),
          pwdCmd.getCurrentPassword(),
          pwdCmd.getNewPassword());
        return "edit/changePwd";
      } catch (WrongPasswordException e) {
        errors.rejectValue("currentPassword", "notMatching");
        return "edit/changePwdForm";
      }
    }
  
  }
  ```

<br>

뷰를 작성해보자.

* **webapp/WEB-INF/view/edit/changePwdForm.jsp**

  ```jsp
  <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
  <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title><spring:message code="change.pwd.title"/></title>
    </head>
    <body>
      <form:form>
        <p>
          <label><spring:message code="currentPassword"/>:
            <form:input path="currentPassword"/>
            <form:errors path="currentPassword"/>
          </label>
        </p>
        <p>
          <label><spring:message code="newPassword"/>:
            <form:password path="newPassword"/>
            <form:errors path="newPassword"/>
          </label>
        </p>
        <input type="submit" value="<spring:message code="change.btn"/>">
      </form:form>
    </body>
  </html>
  ```

* **webapp/WEB-INF/view/changedPwd.jsp**

  ```jsp
  <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
  <head>
      <title><spring:message code="change.pwd.title"/></title>
  </head>
  <body>
  <p>
      <spring:message code="change.pwd.done"/>
  </p>
  <p>
      <a href="<c:url value='/main'/>">
          [<spring:message code="go.main"/>]
      </a>
  </p>
  </body>
  </html>
  ```

* **resources/message/label.properties**

  ```properties
  ...
  
  change.pwd.title=비밀번호 변경
  currentPassword=현재 비밀번호
  newPassword=새 비밀번호
  change.btn=변경하기
  notMatching.currentPassword=비밀번호를 잘못 입력했습니다.
  change.pwd.done=비밀번호를 변경했습니다.
  ```

<br>

ControllerConfig 설정에 ChangePwdController를 빈으로 등록하자.

* **java/config/ControllerConfig.java**

  ```java
  @Configuration
  public class ControllerConfig {
    
    ...
    
    @Autowired
    private ChangePasswordService changePasswordService;
    
    @Bean
    public ChangePwdController changePwdController() {
      ChangePwdController controller = new ChangePwdController();
      controller.setChangePasswordService(changePasswordService);
      return controller;
    }
    
    ...
  
  }
  ```

<br>

# 5. 인터셉터 사용하기

로그인하지 않은 상태에서 비밀번호 변경 폼을 요청하면 로그인 화면으로 이동시켜야 한다.

이를 위해 HttpSession에 "authInf" 객체가 존재하는지 검사하고 존재하지 않으면 로그인 경로로 리다이렉트하도록 클래스를 수정해야 한다.

그런데 이처럼 각 기능을 구현한 컨트롤러 코드마다 세션 확인 코드를 삽입하는 것은 많은 중복을 일으킨다.

이렇게 **다수의 컨트롤러에 대해 동일한 기능을 적용해야 할 때** 사용할 수 있는 것이 **HandlerInterceptor** 이다.

<br>

## 5.1. HandlerInterceptor 인터페이스 구현하기

org.springframework.web.HandlerInterceptor 인터페이스를 사용하면 세 시점에 공통 기능을 넣을 수 있다.

* **컨트롤러(핸들러) 실행 전**
* **컨트롤러(핸들러) 실행 후, 아직 뷰를 실행하기 전**
* **뷰를 실행한 이후**

<br>

세 시점을 처리하기 위해  HandlerInterceptor가 제공하는 메서드

```java
boolean preHandle(
  HttpServletRequest request,
  HttpServletResponse response,
  Object handler) throws Exception;

void postHandle(
  HttpServletRequest request,
  HttpservletResponse response,
  Object handler,
  ModelAndView modelAndView) throws Exception;

void afterCompletion(
  HttpServletRequest request,
  HttpServletResponse response,
  Object handler,
  Exception ex) throws Exception;
```

* **preHandle() 메서드**
  * 메서드는 컨트롤러(핸들러) 객체를 실행하기 전에 필요한 기능을 구현할 때 사용한다.
  * handler 파라미터는 웹 요청을 처리할 컨트롤러(핸들러) 객체이다.
  * 이 메서드가 false를 리턴하면 컨트롤러를 실행하지 않는다.
* **postHandle() 메서드**
  * 컨트롤러가 정상적으로 실행된 이후에 추가 기능을 구현할 때 사용한다.
  * 컨트롤러가 익셉션을 발생하면 이 메서드는 실행하지 않는다.
* **afterCompletion() 메서드**
  * 뷰가 클라이언트에 응답을 전송한 뒤에 실행된다.
  * 컨트롤러 실행 과정에서 익셉션이 발생하면 네 번째 파라미터로 전달된다.
  * 익셉션이 발생하지 않으면 네 번째 파라미터는 null 이다.
  * 익셉션을 로그로 남긴다거나 실행 시간을 기록하는 등의 후처리를 하기에 적합하다.

<br>

### HandlerInterceptor의 실행 흐름

![image](https://user-images.githubusercontent.com/43431081/77393311-20e1eb00-6de0-11ea-83a2-a53bf7bc6363.png)

* HandlerInterceptor 인터페이스의 각 메서드 아무 기능도 구현하지 않은 디폴트 메서드이다. 따라서 메서드를 모두 구현할 필요가 없고 필요한 메서드만 재정의하면 된다.

<br>

로그인 여부에 따라 로그인 폼으로 보내거나 컨트롤러를 실행하도록 구현해보자.

* **java/interceptor/AuthCheckInterceptor.java**

  ```java
  public class AuthCheckInterceptor implements HandlerInterceptor {
  
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, 
                             Object handler) throws Exception {
      HttpSession session = request.getSession(false);
      if (session != null) {
        Object authInfo = session.getAttribute("authInfo");
        if (authInfo != null) {
          // 로그인 정보가 존재할 때만 true 반환
          return true;
        }
      }
      // request.getContextPath() : 현재 컨텍스트 경로를 리턴한다.
      //  ex) http://localhos:8080/test 가 웹 애플리케이션 경로일때, /test 가 컨텍스트 경로
      response.sendRedirect(request.getContextPath() + "/login");
      return false;
    }
  }
  ```

<br>

## 5.2. HandlerInterceptor 설정하기

HandleInterceptor를 구현하면  HandlerInterceptor를 어디에 적용할지 설정해야 한다.

* **java/config/MvcConfig.java**

  ```java
  @Configuration
  @EnableWebMvc
  public class MvcConfig implements WebMvcConfigurer {
  
    ...
  
    @Override
    // 인터셉터를 설정하는 메서드
    public void addInterceptors(InterceptorRegistry registry) {
      // HandleInterceptor 객체를 설정한다.
      registry.addInterceptor(authCheckInterceptor())
        // 인터셉터를 적용할 경로 패턴을 지정한다.
        .addPathPatterns("/edit/**");
    }  
  }
  ```

<br>

### Ant 경로 패턴

* \* : 0 개 또는 그 이상의 글자
* ? : 1 개 글자
* \*\* : 0 개 또는 그 이상의 폴더 경로

<br>

addPathPatterns() 메서드에 지정한 경로 패턴 중 일부를 제외하고 싶다면 **excludePathPatterns() 메서드를** 사용한다.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implments WebMvcConfigurer {
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authCheckInterceptor())
      .addPathPatterns("/edit/**")
      .excludePathPatterns("/edit/help/**");
  }
}
```

제외할 경로 패턴은 두 개 이상이면 각 경로 패턴을 콤마로 구분하면 된다.

<br>

# 6. 컨트롤러에서 쿠키 사용하기

사용자 편의를 위해 아이디를 기억해 두었다가 다음에 로그인할 때 아이디를 자동으로 넣어주는 쿠키 기능.

* 이메일 기억하기 기능 구현 방식
  * 로그인 폼에 '이메일 기억하기' 옵션 추가
  * 로그인 시에 '이메일 기억하기' 옵션을 선택했으면 로그인 성공 후 쿠키에 이메일 저장. 이때 쿠키는 웹 브라우저를 닫더라도 삭제되지 않도록 유효시간을 길게 설정한다.
  * 이후 로그인 폼을 보여줄 때 이메일을 저장한 쿠키가 존재하면 입력 폼에 이메일을 보여준다.

<br>

*이메일 기억하기 기능을 위해 수정할 코드*

* **loginForm.jsp** : 이메일 기억하기 항목을 추가
* **LoginController의  form() 메서드** : 쿠키가 존재할 경우 폼에 전달할 커맨드 객체의 email 프로퍼티를 쿠키의 값으로 설정한다.
* **LoginController의 submit() 메서드** : 이메일 기억하기 옵션을 선택한 경우 로그인 성공 후에 이메일을 담고 있는 쿠키를 생성한다.
* **label.properties** : 메시지를 추가한다.

<br>

loginForm.jsp를 수정해서 이메일 기억하기를 선택할 수 있도록 체크박스를 추가한다.

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
        <p>
          <label><spring:message code="rememberEmail"/>:
            <form:checkbox path="rememberEmail"/>
          </label>
        </p>
        <input type="submit" value="<spring:message code="login.btn"/>"/>
      </form:form>
    </body>
  </html>
  ```

* **resources/message/label.properties**

  ```properties
  ...
  
  change.pwd.title=비밀번호 변경
  currentPassword=현재 비밀번호
  newPassword=새 비밀번호
  change.btn=변경하기
  notMatching.currentPassword=비밀번호를 잘못 입력했습니다.
  change.pwd.done=비밀번호를 변경했습니다.
  
  rememberEmail=이메일 기억하기
  ```

<br>

LoginController의 form() 메서드는 이메일 정보를 기억하고 있는 쿠키가 존재하면 해당 쿠키의 값을 이용해서 LoginCommand 객체의  email 프로퍼티 값을 설정하면 된다.

스프링 MVC에서 쿠키를 사용하는 방법 중 하나는 **@CookieValue 애노테이션을** 사용하는 것이다.

@CookieValue 애노테이션은 **요청 매핑 애노테이션 적용 메서드의 Cookie 타입 파라미터에** 적용한다.

이를 통해 쉽게 쿠키를 Cookie 파라미터로 전달받을 수 있다.

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
    public String form(LoginCommand loginCommand,
                       @CookieValue(value = "REMEMBER", 
                                    required = false)Cookie rCookie) {
      if (rCookie != null) {
        loginCommand.setEmail(rCookie.getValue());
        loginCommand.setRememberEmail(true);
      }
      return "login/loginForm";
    }
  
    ...
  ```

  * **@CookieValue 애노테이션**
    * **value 속성** : 쿠키의 이름을 지정
    * **required 속성** : 지정한 이름을 가진 쿠키가 존재하지 않을 수 있다면 false로 지정

<br>

실제로 REMEMBER 쿠키를 생성하는 부분은 로그인을 처리하는 submit() 메서드이다. **쿠키를 생성하려면 HttpServletResponse 객체가 필요하므로** submit() 메서드의 파라미터로 HttpServletResponse 타입을 추가한다.

* **java/controller/LoginController.java**

  ```java
  @Controller
  @RequestMapping("/login")
  public class LoginController {
  
    ...
  
      @PostMapping
      public String submit(LoginCommand loginCommand, Errors errors,
                           HttpSession session, HttpServletResponse response) {
      new LoginCommandValidator().validate(loginCommand, errors);
      if (errors.hasErrors()) {
        return "login/loginForm";
      }
      try {
        AuthInfo authInfo = authService.authenticate(
          loginCommand.getEmail(),
          loginCommand.getPassword());
  
        session.setAttribute("authInfo", authInfo);
  
        Cookie rememberCookie =
          new Cookie("REMEMBER", loginCommand.getEmail());
        rememberCookie.setPath("/");
        // 이메일 기억하기 여부 판단
        if (loginCommand.isRememberEmail()) {
          // 30일동안 유지
          rememberCookie.setMaxAge(60 * 60 * 24 * 30);
        } else {
          // 바로 삭제
          rememberCookie.setMaxAge(0);
        }
        response.addCookie(rememberCookie);
  
        return "login/loginSuccess";
      } catch (WrongPasswordException e) {
        errors.reject("idPasswordNotMatching");
        return "login/loginForm";
      }
    }
  
  }
  ```