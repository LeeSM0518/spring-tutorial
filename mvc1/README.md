# Chapter 11. MVC 1: 요청 매핑, 커맨드 객체, 리다이렉트, 폼 태그, 모델

* **이 장에서 다룰 내용**
  * @RequestMapping 설정
  * 요청 파라미터 접근
  * 리다이렉트
  * 개발 환경 구축
  * 스프링 폼 태그
  * 모델 처리

<br>

# 1. 프로젝트 준비

1. 하위 폴더 생성

   * **src/main/java** : 자바 코드, 설정 파일 위치
   * **src/main/webapp** : HTML, CSS, JS 등이 위치할 폴더
   * **src/main/webapp/WEB-INF** : web.xml 파일이 위치할 폴더
   * **src/main/webapp/WEB-INF/view** : 컨트롤러의 결과를 보여줄 JSP 파일 위치

2. dependencies 추가

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
       implementation 'javax.servlet:javax.servlet-api:4.0.1'
       implementation 'jstl:jstl:1.2'
       implementation 'spring:spring-webmvc:1.0.2'
       implementation 'org.springframework:spring-jdbc:5.2.4.RELEASE'
       implementation 'org.apache.tomcat:tomcat-jdbc:10.0.0-M1'
       implementation 'org.postgresql:postgresql:42.2.11.jre7'
   }
   ```

3. 두 개의 스프링 설정 파일과 web.xml 파일을 작성

   * **/java/config/MemberConfig.java**

     ```java
     @Configuration
     @EnableTransactionManagement
     public class MemberConfig {
     
       @Bean(destroyMethod = "close")
       public DataSource dataSource() {
         // DataSource 객체 생성
         DataSource ds = new DataSource();
         // JDBC 드라이버 클래스 지정.
         ds.setDriverClassName("org.postgresql.Driver");
         // DB 연결할 때 사용할 URL, 계정, 암호 설정
         ds.setUrl("jdbc:postgresql://arjuna.db.elephantsql.com:5432/");
         ds.setUsername("kberhhnn");
         ds.setPassword("HYXtqTXqY_vYfqysat4KIyMeNTfFj7cJ");
     
         ds.setInitialSize(2);
         ds.setMaxActive(10);
         ds.setTestWhileIdle(true);
         ds.setMinEvictableIdleTimeMillis(60000 * 3);
         ds.setTimeBetweenEvictionRunsMillis(10 * 1000);
         return ds;
       }
     
       @Bean
       public MemberDao memberDao() {
         return new MemberDao(dataSource());
       }
     
       @Bean
       public PlatformTransactionManager transactionManager() {
         DataSourceTransactionManager tm = new DataSourceTransactionManager();
         tm.setDataSource(dataSource());
         return tm;
       }
     
       @Bean
       public MemberRegisterService memberRegSvc() {
         return new MemberRegisterService(memberDao());
       }
     
       @Bean
       public ChangePasswordService changePwdSvc() {
         ChangePasswordService pwdSvc = new ChangePasswordService();
         pwdSvc.setMemberDao(memberDao());
         return pwdSvc;
       }
     
     }
     ```

   * **/java/config/MvcConfig.java**

     ```java
     @Configuration
     @EnableWebMvc
     public class MvcConfig implements WebMvcConfigurer {
     
       @Override
       public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
         configurer.enable();
       }
     
       @Override
       public void configureViewResolvers(ViewResolverRegistry registry) {
         registry.jsp("/WEB-INF/view/", ".jsp");
       }
     
     }
     ```

   * **/webapp/WEB-INF/web.xml**

     ```xml
     <?xml version="1.0" encoding="UTF-8" ?>
     
     <web-app xmlns="http://java.sun.com/xml/ns/javaee" version="2.5"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
                                  http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd">
     
       <!-- DispatcherServlet을 dispatcher 라는 이름으로 등록한다. -->
       <servlet>
         <servlet-name>dispatcher</servlet-name>
         <servlet-class>
           org.springframework.web.servlet.DispatcherServlet
         </servlet-class>
         <!-- contextClass 초기화 파라미터를 설정한다. -->
         <init-param>
           <param-name>contextClass</param-name>
           <!-- 자바 설정을 이용하는 웹 어플리케이션 용 스프링 컨테이너 클래스-->
           <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
         </init-param>
         <init-param>
           <!-- contextConfiguration 초기화 파라미터의 값을 지정한다. -->
           <param-name>contextConfigLocation</param-name>
           <!-- 스프링 설정 클래스 목록을 지정한다. -->
           <!-- 줄바꿈이나 콤마로 구분 -->
           <param-value>
             config.MemberConfig
             config.MvcConfig
             config.ControllerConfig
           </param-value>
         </init-param>
         <!-- 이 서블릿을 웹 어플리케이션을 구동할 때 실행하도록 설정-->
         <load-on-startup>1</load-on-startup>
       </servlet>
     
       <!-- 모든 요청을 DispatcherServlet이 처리하도록 서블릿 매핑을 설정 -->
       <servlet-mapping>
         <servlet-name>dispatcher</servlet-name>
         <url-pattern>/</url-pattern>
       </servlet-mapping>
     
       <!-- HTTP 요청 파라미터의 인코딩 처리를 위한 서블릿 필터를 등록한다.-->
       <filter>
         <filter-name>encodingFilter</filter-name>
         <!-- 스프링은 인코딩 처리를 위한 필터를 제공한다.-->
         <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
         <!-- encoding 초기화 파라미터를 설정해서
                 HTTP 요청 파라미터를 읽어올 때 사용할 인코딩 지정-->
         <init-param>
           <param-name>encoding</param-name>
           <param-value>UTF-8</param-value>
         </init-param>
       </filter>
       <filter-mapping>
         <filter-name>encodingFilter</filter-name>
         <url-pattern>/*</url-pattern>
       </filter-mapping>
     
     </web-app>
     ```

<br>

# 2. 요청 매핑 애노테이션을 이용한 경로 매핑

웹 어플리케이션을 개발하는 것은 다음 코드를 작성하는 것이다.

* **특정 요청 URL을 처리할 코드**
* **처리 결과를 HTML과 같은 형식으로 응답하는 코드**

<br>

이중 첫 번째는 **@Controller 애노테이션을** 사용한 컨트롤러 클래스를 이용해서 구현한다.

컨트롤러 클래스는 **요청 매핑 애노테이션을 사용해서** 메서드가 처리할 요청 경로를 지정한다.

* **요청 매핑 애노테이션**
  * @RequestMapping
  * @GetMapping
  * @PostMapping

<br>

예를 들어 회원 가입 과정을 생각해보자.

* **회원 가입 과정**
  * 약관 동의 화면 요청 처리 : http://localhost:8080/register/step1
  * 회원 정보 입력 화면 : http://localhost:8080/register/step2
  * 가입 처리 결과 화면 : http://localhost:8080/register/step3

여러 단계를 거쳐 하나의 기능이 완성되는 경우 **관련 요청 경로를 한 개의 컨트롤러 클래스에서 처리하면** 코드 관리에 도움이 된다.

```java
@Controller
public class RegistController {
  
  @RequestMapping("/register/step1")
  public String handleStep1() {
    return "register/step1";
  }
  
  @RequestMapping("/register/step2")
  public String handleStep2() {
    ...
  }
  
  @ReqeustMapping("/register/step3")
  public String handleStep3() {
    ...
  }
  
}
```

<br>

각 요청 매핑 애노테이션의 경로가 **"/register"** 로 시작한다. 이 경우 **공통되는 부분의 경로를 담은 @RequestMapping 애노테이션을 클래스에 사용하고** 각 메서드는 나머지 경로를 값으로 갖는 요청 매핑 애노테이션을 적용할 수 있다.

```java
@Controller
@RequestMapping("/register")
public class RegistController {
  
  @RequestMapping("/step1")
  public String handleStep1() {
    return "register/step1";
  }
  
  @RequestMapping("/step2")
  public String handleStep2() {
    ...
  }
  
  ...
  
}
```

<br>

예제 코드를 만들어보자.

* **/java/controller/RegisterController.java**

  ```java
  @Controller
  public class RegisterController {
  
    @RequestMapping("/register/step1")
    public String handleStep1() {
      return "register/step1";
    }
  
  }
  ```

* **/webapp/WEB-INF/view/register/step1.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title>회원가입</title>
    </head>
    <body>
      <h2>약관</h2>
      <p>약관 내용</p>
      <form action="step2" method="post">
        <label>
          <input type="checkbox" name="agree" value="true"> 약관 동의
        </label>
        <input type="submit" value="다음 단계">
      </form>
    </body>
  </html>
  ```

* **/java/config/ControllerConfig.java**

  ```java
  @Configuration
  public class ControllerConfig {
  
    @Bean
    public RegisterController registerController() {
      return new RegisterController();
    }
  
  }
  ```

* **실행 결과**

  ![image](https://user-images.githubusercontent.com/43431081/76427176-53a6df00-63ef-11ea-960a-6c85f1039f4c.png)

> HTTP에서 GET, POST, PATCH 등을 **메서드(method)** 라고 부른다. 자바의 메서드와 혼동될 수 있어 이 책에서는 메서드라는 용어 대신 **방식** 이란 용어를 사용한다.

<br>

# 3. GET과 POST 구분: @GetMapping, @PostMapping

스프링 MVC는 별도 설정이 없으면 GET과 POST 방식에 상관없이 @RequestMapping에 지정한 경로와 일치하는 요청을 처리한다. 만약 POST 방식 요청만 처리하고 싶다면 @PostMapping 애노테이션을 사용해서 제한할 수 있다.

같은 경로에 대해 GET과 POST 방식을 각각 다른 메서드가 처리하도록 설정할 수 있다.

```java
@Controller
public class LoginController {
  @GetMapping("/member/login")
  public String form() {
    ...
  }
  
  @PostMapping("/member/login")
  public String login() {
    ...
  }
}
```

> @GetMapping, @PostMapping 애노테이션뿐만 아니라 **@PutMapping, @DeleteMapping, @PatchMapping** 애노테이션들도 제공한다.

<br>

# 4. 요청 파라미터 접근

컨트롤러 메서드에서 요청 파라미터를 사용하는 첫 번째 방법은 **HttpServletRequest를 직접 이용하는 것이다.** 

예를 들어 컨트롤러 처리 메서드의 파라미터로 HttpServletRequest 타입을 사용하고 **HttpServletRequest의 getParameter() 메서드를 이용해서** 파라미터의 값을 구하면 된다.

```java
@Controller
public class RegisterController {
  
  ...
    
  @PostMapping("/register/step2")
  // 파라미터로 HttpServletRequest 타입 변수를 선언하고
  // 직접 getParameter() 메소드를 호출한다.
  public String handleStep2(HttpServletRequest request) {
    String agreeParam = request.getParameter("agree");
    if (agreeParam == null || !agreeParam.equals("true")) {
      return "register/step1";
    }
    return "register/step2";
  }
}
```

<br>

요청 파라미터에 접근하는 또 다른 방법은 **@RequestParam 애노테이션을 사용하는 것이다.**

```java
@Controller
public class RegisterController {

  ...

  @PostMapping("/register/step2")
  public String handleStep2(
    @RequestParam(value="agree", defaultValue="false") Boolean agreeVal) {
    if (!agree) {
      return "register/step1";
    }
    return "register/step2";
  }
  
}
```

* agree 요청 파라미터의 값을 읽어와 agreeVal 파라미터에 할당한다.

<br>

**@RequestParam 애노테이션의 속성**

| 속성         | 타입    | 설명                                                         |
| ------------ | ------- | ------------------------------------------------------------ |
| value        | String  | HTTP 요청 파라미터의 이름을 지정한다                         |
| required     | boolean | 필수 여부를 지정한다. 이 값이 true이면서<br />해당 요청 파라미터에 값이 없으면 익셉션이 발생한다.<br />기본값은 true이다. |
| defaultValue | String  | 요청 파라미터가 값이 없을 때 사용할 문자열 값을 지정한다.    |

스프링 MVC는 파라미터 타입에 맞게 String 값을 해당 기본 데이터 타입이나 래퍼 타입으로 변환해준다.

<br>

약관 동의 화면의 다음 요청을 처리하는 코드를 작성해보자.

* **/java/controller/RegisterController.java**

  ```java
  @Controller
  public class RegisterController {
  
    ... 생략
  
    @PostMapping("/register/step2")
    public String handleStep2(
        @RequestParam(value = "agree", defaultValue = "false") Boolean agree) {
      if (!agree) {
        return "register/step1";
      }
      return "register/step2";
    }
  
  }
  ```

* **/webapp/WEB-INF/view/register/step2.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title>회원 가입</title>
    </head>
    <body>
      <h2>회원 정보 입력</h2>
      <form action="step3" method="post">
        <p>
          <label>이메일:<br>
            <input type="text" name="email" id="email">
          </label>
        </p>
        <p>
          <label>이름:<br>
            <input type="text" name="name" id="name">
          </label>
        </p>
        <p>
          <label>비밀번호:<br>
            <input type="password" name="password" id="password">
          </label>
        </p>
        <p>
          <label>비밀번호 확인:<br>
            <input type="password" name="confirmPassword" id="confirmPassword">
          </label>
        </p>
        <input type="submit" value="가입 완료">
      </form>
    </body>
  </html>
  ```

* **실행 결과**

  ![image](https://user-images.githubusercontent.com/43431081/76429709-c796b680-63f2-11ea-9a23-abebc7f482eb.png)

<br>

# 5. 리다이렉트 처리

웹 브라우저에서 http://localhost:8080/register/step2 주소를 직접 입력하면 에러가 발생한다.

![image](https://user-images.githubusercontent.com/43431081/76431799-6fad7f00-63f5-11ea-8143-7611c3c56889.png)

스프링 MVC는 handleStep2() 메서드가 GET 요청의 처리를 지원하지 않으므로 **405 상태 코드를 응답한다.**

잘못된 전송 방식으로 요청이 왔을 때 에러 화면보다 **알맞은 경로로 리다이렉트 하는 것이** 더 좋을 때가 있다.

컨트롤러에서 특정 페이지로 리다이렉트시키는 방법은 **"redirect:경로"** 를 뷰 이름으로 리턴하면 된다.

* **/java/controller/RegisterController.java**

  ```java
  @Controller
  public class RegisterController {
  
    ...
    
    @GetMapping("/register/step2")
    public String handleStep2Get() {
      return "redirect:/register/step1";
    }
  
  }
  ```

  * "redirect:" 뒤의 문자열이 **"/"로 시작하면** 웹 어플리케이션을 기준으로 이동 경로를 생성한다.
  * **"/"로 시작하지 않으면** 현재 경로를 기준으로 상대 경로를 사용한다.
  * **완전한 URL을 사용하면** 해당 경로로 리다이렉트한다.
    * ex) redirect:http://localhost8080/register/step1

<br>

# 6. 커맨드 객체를 이용해서 요청 파라미터 사용하기

* **회원 가입 요청의 form 처리**

  ```java
  public class RegisterController {
    
    ...
    @PostMapping("/register/step3")
    public String handleStep3(HttpServletRequest request) {
      String email = request.getParameter("email");
      String name = request.getParameter("name");
      String password = request.getParameter("password");
      String confirmPassword = request.getParameter("confirmPassword");
      
      RegisterRequest regReq = new RegisterRequest();
      regReq.setEmail(email);
      regReq.setName(name);
      ...
    }
    
  }
  ```

  * 위 코드는 올바르게 동작하지만, 요청 파라미터 개수가 증가할 때마다 메서드의 코드 길이도 함께 길어진다.

<br>

스프링은 위와 같은 불편함을 줄이기 위해 **요청 파라미터의 값을 커맨드(command) 객체에 담아주는 기능을 제공한다.**

요청 파라미터의 값을 전달받을 수 있는 세터 메서드를 포함하는 객체를 커맨드 객체로 사용하면 된다.

```java
@PostMapping("/register/step3")
public String handleStep3(RegisterRequest regReq) {
  ...
}
```

* RegisterRequest 클래스에는 setEmail(), setName(), setPassword(), setConfirmPassword() 메서드가 있다.
* 스프링은 이들 메서드를 사용해서 email, name, password, confirmPassword 요청 파라미터 의 값을 커맨드 객체에 복사한 뒤 regReq 파라미터로 전달한다.
* 즉 **스프링 MVC가 handleStep3() 메서드에 전달한 RegisterRequest 객체를 생성하고 그 객체의 세터 메서드를 이용해서 일치하는 요청 파라미터의 값을 전달한다.**

<br>

폼에 입력한 값을 커맨드 객체로 전달받아 회원가입을 처리하는 코드를 추가해보자.

* **/java/controller/RegisterController.java**

  ```java
  @Controller
  public class RegisterController {
  
    private MemberRegisterService memberRegisterService;
  
    public void setMemberRegisterService(MemberRegisterService memberRegisterService) {
      this.memberRegisterService = memberRegisterService;
    }
  
    @RequestMapping("/register/step1")
    public String handleStep1() {
      return "register/step1";
    }
  
    @PostMapping("/register/step2")
    public String handleStep2(
        @RequestParam(value = "agree", defaultValue = "false") Boolean agree) {
      if (!agree) {
        return "register/step1";
      }
      return "register/step2";
    }
  
    @GetMapping("/register/step2")
    public String handleStep2Get() {
      return "redirect:/register/step1";
    }
  
    @PostMapping("/register/step3")
    public String handleStep3(RegisterRequest regReq) {
      // MemberRegisterService를 이용해서 회원 가입을 처리한다.
      try {
        // 회원 가입 성공시
        memberRegisterService.regist(regReq);
        return "register/step3";
      } catch (DuplicateMemberDaoException ex) {
        // 실패시
        return "register/step2";
      }
    }
  
  }
  ```

* **/java/config/ControllerConfig.java**

  ```java
  @Configuration
  public class ControllerConfig {
  
    @Autowired
    private MemberRegisterService memberRegSvc;
  
    @Bean
    public RegisterController registerController() {
      // MemberRegisterService 타입을 의존 주입한다.
      RegisterController controller = new RegisterController();
      controller.setMemberRegisterService(memberRegSvc);
      return controller;
    }
  
  }
  ```

* **/webapp/WEB-INF/view/register/step3.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <html>
    <head>
      <title>회원가입</title>
    </head>
    <body>
      <p>회원 가입을 완료했습니다.</p>
      <p><a href="<c:url value='/main'/>">[첫 화면 이동]</a></p>
    </body>
  </html>
  ```

<br>

# 7. 뷰  JSP 코드에서 커맨드 객체 사용하기

HTTP 요청 파라미터를 이용해서 회원 정보를 전달했으므로 JSP의 표현식 등을 이용해서 정보를 표시해도 되지만, **커맨드 객체를 사용해서 정보를 표시할 수도 있다.**

* **/webapp/WEB-INF/view/register/step3.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <html>
    <head>
      <title>회원가입</title>
    </head>
    <body>
      <p><strong>${registerRequest.name}님</strong> 회원 가입을 완료했습니다.</p>
      <p><a href="<c:url value='/main'/>">[첫 화면 이동]</a></p>
    </body>
  </html>
  ```

  * 스프링 MVC는 커맨드 객체의 클래스 이름과 동일한 속성 이름을 사용해서 커맨드 객체를 뷰에 전달한다.

<br>

![image](https://user-images.githubusercontent.com/43431081/76489728-a1adf800-646c-11ea-8a78-a13c9dd2635d.png)

<br>

# 8.  @ModelAttribute 애노테이션으로 커맨드 객체 속성 이름 변경

커맨드 객체에 접근할 때 사용할 속성 이름을 변경하고 싶다면 커맨드 객체로 사용할 파라미터에 **@ModelAttribute 애노테이션을 적용하면 된다.**

```java
@PostMapping("/register/step3")
public String handleStep3(@ModelAttribute("formData") RegisterRequest regReq) {
  ...
}
```

위 설정을 사용하면 뷰 코드에서 "formData" 라는 이름으로 커맨드 객체에 접근할 수 있다.

<br>

# 9. 커맨드 객체와 스프링 폼 연동

다시 폼을 보여줄 때 커맨드 객체의 값을 폼에 채워주면 다시 입력해야 되는 불편함을 해소할 수 있다.

```jsp
<input type="text" name="email" id="email" value="${registerRequest.email}">
...
<input type="text" name="name" id="name" value="${registerRequest.name}">
```

<br>

스프링 MVC가 제공하는 커스텀 태그를 사용하면 더 간단하게 커맨드 객체의 값을 출력할 수 있다.

스프링은 \<form:form> 태그와 \<form:input> 태그를 제공하고 있다. 이 두 태그를 사용하면 커맨드 객체의 값을 폼에 출력할 수 있다.

* **/webapp/WEB-INF/view/register/step2.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
  <html>
  <head>
      <title>회원 가입</title>
  </head>
  <body>
  <h2>회원 정보 입력</h2>
  <%--<form action="step3" method="post">--%>
  <%--@elvariable id="registerRequest" type="spring.RegisterRequest"--%>
  <form:form action="step3" modelAttribute="registerRequest">
      <p>
          <label>이메일:<br>
  <%--            <input type="text" name="email" id="email" value="${registerRequest.email}">--%>
              <form:input path="email"/>
          </label>
      </p>
      <p>
          <label>이름:<br>
  <%--            <input type="text" name="name" id="name" value="${registerRequest.name}">--%>
              <form:input path="name"/>
          </label>
      </p>
      <p>
          <label>비밀번호:<br>
              <form:input path="password"/>
          </label>
      </p>
      <p>
          <label>비밀번호 확인:<br>
              <form:password path="confirmPassword"/>
          </label>
      </p>
      <input type="submit" value="가입 완료">
  </form:form>
  </body>
  </html>
  ```

  * **\<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>**

    : 스프링이 제공하는 폼 태그를 사용하기 위해 taglib 디렉티브를 설정

  * **\<form:form> 태그 속성**

    * **action** : \<form> 태그의 action 속성과 동일한 값을 사용한다.
    * **modelAttribute** : 커맨드 객체의 속성 이름을 지정한다. 설정하지 않는 경우  "command"를 기본값으로 사용한다.

  * **\<form:input> 태그**

    * path로 지정한 커맨드 객체의 프로퍼티를 \<input> 태그의  value 속성값으로 사용한다.

  * **\<form:password> 태그**

    * \<form:input> 태그와 유사하다.

<br>

step1에서  step2로 넘어오는 단계에서 이름이 "registerRequest"인 객체를 모델에 넣어야 \<form:form> 태그가 정상 동작한다. 이를 위해 코드를 수정해보자.

* **/java/controller/RegisterController.java**

  ```java
  @Controller
  public class RegisterController {
  
    ...
  
    @PostMapping("/register/step2")
    public String handleStep2(
        @RequestParam(value = "agree", defaultValue = "false") Boolean agree,
        Model model) {
      if (!agree) {
        return "register/step1";
      }
      model.addAttribute("registerRequest", new RegisterRequest());
      return "register/step2";
    }
  
    ...
      
  }
  ```

<br>

# 10. 컨트롤러 구현 없는 경로 매핑

```jsp
<p>
  <a href="<c:url value='/main'/>">[첫 화면 이동]</a>
</p>
```

위의 코드는 회원 가입 완료 후 첫 화면으로 이동할 수 있는 링크를 보여준다. 이 링크를 위한 컨트롤러 클래스는 특별히 처리할 것이 없기 때문에 뷰 이름만 리턴하도록 구현한다.

* **/java/controller/MainController.java**

  ```java
  @Controller
  public class MainController {
    
    @RequestMapping("/main")
    public String main() {
      return "main";
    }
    
  }
  ```

  * 이 컨트롤러 코드는 요청 경로와 뷰 이름을 연결해주는 것에 불과하다. 

<br>

위의 성가신 일을 해결하는 방법은 **WebMvcConfigurer 인터페이스의 addViewControllers() 메서드를** 사용하는 것이다.

이 메서드를 재정의하면 컨트롤러 구현없이 간단한 코드로 요청 경로와 뷰 이름을 연결할 수 있다.

```java
@Override
public void addViewControllers(ViewControllerRegistry registry) {
  registry.addViewController("/main").setViewName("main")
}
```

<br>

* **/resources/spring-controller.xml**

  ```java
  @Configuration
  @EnableWebMvc
  public class MvcConfig implements WebMvcConfigurer {
  
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
      configurer.enable();
    }
  
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
      registry.jsp("/WEB-INF/view/", ".jsp");
    }
  
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
      registry.addViewController("/main").setViewName("main");
    }
    
  }
  ```

* **/webapp/WEB-INF/view/main.jsp**

  ```jsp
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title>메인</title>
    </head>
    <body>
      <p>환영합니다.</p>
      <p><a href="<c:url value="/register/step1"/>">[회원 가입하기]</a></p>
    </body>
  </html>
  ```

<br>

# 11. 주요 에러 발생 상황

## 11.1. 요청 매핑 애노테이션과 관련된 주요 익셉션

요청 경로를 처리할 컨트롤러가 존재하지 않거나 WebMvcConfigurer를 이용한 설정이 없다면 404 에러가 발생한다.

* **404 에러 발생 시 확인해야 할 사항들**
  * 요청 경로가 올바른지
  * 컨트롤러에 설정한 경로가 올바른지
  * 컨트롤러 클래스를 빈으로 등록했는지
  * 컨트롤러 클래스에 @Controller 애노테이션을 적용했는지

컨트롤러가 존재하지 않을 때는 에러 메시지에 **URL이** 나오지만, 뷰 이름에 해당하는 JSP 파일이 존재하지 않을 때는 에러 메시지에  **JSP 파일의 경로가 출력된다.**

지원하지 않는 전송 방식(method)을 사용한 경우  **405** 에러가 발생한다.

<br>

## 11.2. @RequestParam이나 커맨드 객체와 관련된 주요 익셉션

* **예시**

  ```java
  @PostMappin("/register/step2")
  public String handleStep2(
    @RequestParam("agree") Boolean agree,
    Model model) {
    ...
  }
  ```

  * 위와 같이 @RequestParam 애노테이션을 필수로 설정하고 기본값을 지정하지 않았다.
  * 이렇게 수정한 뒤 약관 동의 화면에서 '약관 동의'를 선택하지 않고 [다음 단계] 버튼을 클릭하면, 파라미터로 아무 값도 전송되지 않는다.
  * 즉  agree 파라미터를 전송하지 않기 때문에 **@RequestParam 애노테이션을 처리하는 과정에서 필수인 "agree" 파라미터가 존재하지 않는다는 익셉션이 발생한다.**
  * **400** 에러가 전송되며 에러 메시지는 필수인 파라미터가 없다는 내용으로 출력된다.

만약 value 속성을 "true" 에서  "true1"로 변경하고 실행해보면 **400** 에러가 발생한다. 에러 메시지를 보면 **"true1" 값을 Boolean 타입으로 변환할 수 없어서** 에러가 발생한 것을 확인할 수 있다.

브라우저에서 표시된 400 에러만 보면 어떤 문제로 이 에러가 발생했는지 찾기가 쉽지 않기 때문에 콘솔에 출력된 로그 메시지를 참고하면 도움이 된다.

### Logback으로 자세한 에러 로그 출력하기

로그 레벨을 낮추면 더 자세한 로그를 확인할 수 있다. 

Logback 관련 의존을 추가한다.

* **build.gradle**

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
  }
  ```

그 후 src/main/resources 폴더에 logback.xml 파일을 생성한다.

* **logback.xml**

  ```xml
  <?xml version="1.0" encoding="UTF-8" ?>
  <configuration>
    <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
      <encoder>
        <pattern>%d %5p %c{2} - %m%n</pattern>
      </encoder>
    </appender>
    <root level="INFO">
      <appender-ref ref="stdout"/>
    </root>
  
    <logger name="org.springframework.servlet" level="DEBUG"/>
  </configuration>
  ```

  * 위 설정은 org.springframework.web.servlet과 그 하위 패키지의 클래스에서 출력한 로그를 상세한 수준('DEBUG' 레벨)으로 남긴다.
  * 이 디버깅 라이브러리를 사용하면 400 에러가 발생하는 상황이 되면 콘솔에서 보다 상세한 로그를 볼 수 있다.

<br>

# 12. 커맨드 객체: 중첩 ・ 콜렉션 프로퍼티

세 개의 설문 항목과 응답자의 지역과 나이를 입력받는 설문 조사 정보를 담기 위해 클래스를 작성해보자.

* **/java/survey/Respondent.java**

  ```java
  public class Respondent {
    
    private int age;
    private String location;
  
    public int getAge() {
      return age;
    }
  
    public void setAge(int age) {
      this.age = age;
    }
  
    public String getLocation() {
      return location;
    }
  
    public void setLocation(String location) {
      this.location = location;
    }
    
  }
  ```

* **/java/survey/AnsweredData.java**

  ```java
  public class AnsweredData {
    
    private List<String> response;
    private Respondent res;
  
    public List<String> getResponse() {
      return response;
    }
  
    public void setResponse(List<String> response) {
      this.response = response;
    }
  
    public Respondent getRes() {
      return res;
    }
  
    public void setRes(Respondent res) {
      this.res = res;
    }
    
  }
  ```

  * 이전에 커맨드 객체로 사용한 클래스와 비교하면 차이가 존재한다.
    * 리스트 타입의 프로퍼티가 존재한다.
    * 중첩 프로퍼티를 갖는다.

<br>

스프링 MVC는 커맨드 객체가 리스트 타입의 프로퍼티를 가졌거나 중첩 프로퍼티를 가진 경우에도 요청 파라미터의 값을 알맞게 커맨드 객체에 설정해주는 기능을 제공해준다.

* HTTP 요청 파라미터 이름이 **"프로퍼티이름[인덱스]"** 형식이면 List 타입 프로퍼티의 값 목록으로 처리한다.
* HTTP 요청 파라미터 이름이 **"프로퍼티이름.프로퍼티이름"** 과 같은 형식이면 중첩 프로퍼티 값을 처리한다.

<br>

컨트롤러 클래스를 작성해보자.

* **/java/survey/SurveyController.java**

  ```java
  @Controller
  @RequestMapping("/survey")
  public class SurveyController {
    
    @GetMapping
    public String form() {
      return "survey/surveyForm";
    }
    
    @PostMapping
    public String submit(@ModelAttribute("ansData") AnsweredData data) {
      return "survey/submitted";
    }
    
  }
  ```

* **/java/config/ControllerConfig.java**

  ```java
  @Configuration
  public class ControllerConfig {
  
    @Autowired
    private MemberRegisterService memberRegSvc;
  
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
  
  }
  ```

* **/webapp/WEB-INF/view/survey/surveyForm.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title>설문조사</title>
    </head>
    <body>
      <h2>설문조사</h2>
      <form method="post">
        <p>1. 당신의 역할은?<br/>
          <label><input type="radio" name="responses[0]" value="서버">서버개발자</label>
          <label><input type="radio" name="responses[0]" value="프론트">프론트개발자</label>
          <label><input type="radio" name="responses[0]" value="풀스택">풀스택개발자</label>
        </p>
        <p>
          2. 가장 많이 사용하는 개발도구는?<br/>
          <label><input type="radio" name="responses[1]" value="Eclipse">Eclipse</label>
          <label><input type="radio" name="responses[1]" value="Intellij">Intellij</label>
          <label><input type="radio" name="responses[1]" value="Sublime">Sublime</label>
        </p>
        <p>
          3. 하고싶은 말<br/>
          <label>
            <input type="text" name="responses[2]">
          </label>
        </p>
        <p>
          <label>응답자 위치:<br/>
            <input type="text" name="res.location">
          </label>
        </p>
        <p>
          <label>응답자 나이:<br/>
            <input type="text" name="res.age">
          </label>
        </p>
        <input type="submit" value="전송">
      </form>
    </body>
  </html>
  ```

  * **responses[0], responses[1], responses[2]** : responses 프로퍼티의 값들
  * **res.location** : res 프로퍼티의 location 프로퍼티
  * **res.age** : res 프로퍼티의 age 프로퍼티

* **/webapp/WEB-INF/view/survey/submitted.jsp**

  ```jsp
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title>응답 내용</title>
    </head>
    <body>
      <p>응답 내용:</p>
      <ul>
        <c:forEach var="response"
                   items="${ansData.responses}" varStatus="status">
          <li>${status.index + 1}번 문항: ${response}</li>
        </c:forEach>
      </ul>
      <p>응답자 위치: ${ansData.res.location}</p>
      <p>응답자 나이: ${ansData.res.age}</p>
    </body>
  </html>
  ```

<br>

# 13. Model을 통해 컨트롤러에서 뷰에 데이터 전달하기

컨트롤러는 뷰가 응답 화면을 구성하는데 필요한 데이터를 생성해서 전달해야 한다. 이때 사용하는 것이 **Model** 이다.

* **Model 사용 예시**

  ```java
  @Controller
  public class HelloController {
    @RequestMapping("/hello")
    public String hello(Model, model,
                       @RequestParam(value = "name", required = false) String name) {
      model.addAttribute("greeting", "안녕하세요, " + name)
        return "hello";
    }
  }
  ```

  * 뷰에 데이터를 전달해야하는 컨트롤러는 두 가지만 하면 된다.
    1. 요청 매핑 애노테이션이 적용된 메서드의 파라미터로 Model을 추가
    2. Model 파라미터의 addAttribute() 메서드로 뷰에서 사용할 데이터 전달

<br>

설문 항목을 컨트롤러에서 생성해서 뷰에 전달하는 방식으로 변경해보자.

* **/java/survey/Question.java**

  ```java
  public class Question {
    
    private String title;
    private List<String> options;
  
    public Question(String title, List<String> options) {
      this.title = title;
      this.options = options;
    }
  
    public Question(String title) {
      this.title = title;
    }
  
    public String getTitle() {
      return title;
    }
  
    public List<String> getOptions() {
      return options;
    }
    
    public boolean isChoice() {
      return options != null && !options.isEmpty();
    }
    
  }
  ```

* **/java/survey/SurveyController.java**

  ```java
  @Controller
  @RequestMapping("/survey")
  public class SurveyController {
  
    @GetMapping
    public String form(Model model) {
      List<Question> questions = createQuestions();
      model.addAttribute("questions", questions);
      return "survey/surveyForm";
    }
  
    private List<Question> createQuestions() {
      Question q1 = new Question("당신의 역할은 무엇입니까?",
                                 Arrays.asList("서버", "프론트", "풀스택"));
      Question q2 = new Question("많이 사용하는 개발도구는 무엇입니까?",
                                 Arrays.asList("이클립스", "인텔리J", "서브라임"));
      Question q3 = new Question("하고 싶은 말 적어주세요.");
      return Arrays.asList(q1, q2, q3);
    }
  
    @PostMapping
    public String submit(@ModelAttribute("ansData") AnsweredData data) {
      return "survey/submitted";
    }
  
  }
  ```

  * form() 메서드에 Model 타입의 파라미터를 추가
  * Question 리스트를 "questions"라는 이름으로 모델에 추가했다.

* **/webapp/WEB-INF/view/survey/surveyForm.jsp**

  ```jsp
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title>설문조사</title>
    </head>
    <body>
      <h2>설문조사</h2>
      <form method="post">
        <c:forEach var="q" items="${questions}" varStatus="status">
          <p>${status.index + 1}. ${q.title}<br/>
            <c:if test="${q.choice}">
              <c:forEach var="option" items="${q.options}">
                <label><input type="radio"
                              name="responses[${status.index}]" value="${option}">
                  ${option}</label>
              </c:forEach>
            </c:if>
            <c:if test="${! q.choice}">
              <input type="text" name="responses[${status.index}]">
            </c:if>
          </p>
        </c:forEach>
        <p>
          <label>응답자 위치:<br/>
            <input type="text" name="res.location">
          </label>
        </p>
        <p>
          <label>응답자 나이:<br/>
            <input type="text" name="res.age">
          </label>
        </p>
        <input type="submit" value="전송">
      </form>
    </body>
  </html>
  ```

<br>

## 13.1. ModelAndView를 통한 뷰 선택과 모델 전달

지금까지 구현한 컨트롤러는 두 가지 특징이 있다.

* <u>Model을 이용해서 뷰에 전달할 데이터 설정</u>
* <u>결과를 보여줄 뷰 이름을 리턴</u>

**ModelAndView를** 사용하면 이 두 가지를 한 번에 처리할 수 있다. ModelAndView는 **모델과 뷰 이름을 함께 제공한다.**

```java
@Controller
@RequestMapping("/survey")
public class SurveyController {
  
  @GetMappin
  public ModelAndView form() {
    List<Question> questions = createQuestions();
    ModelAndView mav = new ModelAndView();
    mav.addObject("questions", questions);
    mav.setViewName("survey/surveyForm");
    return mav;
  }
  
}
```

* 뷰에 전달할 **모델 데이터는 addObject() 메서드로 추가**
* **뷰 이름은 setViewName() 메서드를** 이용해서 지정한다.

<br>

## 13.2. GET 방식과 POST 방식에 동일 이름 커맨드 객체 사용하기

\<form:form> 태그를 사용하려면 커맨드 객체가 반드시 존재해야 한다.

커맨드 객체를 파라미터로 추가하면 간단하다.

```java
@PostMapping("/register/step2")
public String handleStep2(
  @RequestParam(value = "agree", defaultValue = "false") Boolean agree,
  RegisterRequest registerRequest) {
  if (!agree) {
    return "register/step1";
  }
  return "register/step2";
}
```

<br>

이름을 명시적으로 지정하려면 **@ModelAttribute 애노테이션을 사용한다.** 

```java
@Controller
@RequestMapping("/login")
public class LoginController {
  
  @GetMapping
  public String form(@ModelAttribute("login") LoginCommand loginCommand) {
    return "login/loginForm";
  }
  
  @PostMapping
  public String form(@ModelAttribute("login") LoginCommand loginCommand) {
    ...
  }
  
}
```

<br>

# 14. 주요 폼 태그 설명

스프링 MVC는 \<form:form>, \<form:input> 등 HTML 폼과 커맨드 객체를 연동하기 위한 JSP 태그 라이브러리를 제공한다. 이 두 태그 외에도 \<select>를 위한 태그와 체크 박스나 라디오 버튼을 위한 커스텀 태그도 제공한다.

<br>

## 14.1. \<form> 태그를 위한 커스텀 태그 : \<form:form>

\<form:form> 커스텀 태그는 **\<form> 태그를 생성할 때 사용된다.**

* **사용 방법**

  ```jsp
  <%@ taglib prefix="form" url="http://www.springframework.org/tags/form" %>
  ...
  <form:form>
    ...
    <input type="submit" value="가입 완료">
  </form:form>
  ```

  * method 속성과 action 속성을 지정하지 않으면 **method 속성값은 "post"로** 설정되고 **action 속성값은 현재 요청 URL로** 설정된다.

<br>

커맨드 객체 이름이 기본값인 "command"가 아니면 **modelAttribute 속성값으로** 커맨드 객체의 이름을 설정해야 한다.

```jsp
<form:form modelAttribute="loginCommand">
  ...
</form:form>
```

<br>

* **\<form:form> 커스텀 태그의 속성들**
  * **action** : 폼 데이터를 전송할 URL을 입력
  * **enctype** : 전송될 데이터의 인코딩 타입
  * **method** : 전송 방식

<br>

커맨드 객체의 값을 사용하면 이전에 입력한 값을 출력할 수 있다.

```jsp
<form:form modelAttribute="loginCommand">
  ...
  <input type="text" name="id" value="${loginCommand.id}"/>
  ...
</form:form>
```

<br>

## 14.2. \<input> 관련 커스텀 태그 : \<form:input>, \<form:password>, \<form:hidden>

* **\<input> 태그와 관련된 기본 커스텀 태그**
  * **\<form:input>** : text 타입의 \<input> 태그
  * **\<form:password>** : password 타입의 \<input> 태그
  * **\<form:hidden>** : hidden 타입의 \<input> 태그

<br>

\<form:input> 커스텀 태그는 **path 속성을** 사용해서 연결할 커맨드 객체의 프로퍼티를 지정한다.

```jsp
<form:form modelAttribute="registerReqeust" action="step3">
  <p>
    <label>이메일:<br/>
      <form:input path="email"/>
    </label>
  </p>
</form:form>
```

<br>

## 14.3. \<select> 관련 커스텀 태그 : \<form:select>, \<form:options>, \<form:option>

* **\<select> 태그와 관련된 커스텀 태그**
  * **\<form:select>** : \<select> 태그를 생성한다. \<option> 태그를 생성할 때 필요한 콜렉션을 전달받을 수도 있다.
  * **\<form:options>** : 지정한 콜렉션 객체를 이용하여 \<option> 태그를 생성한다.
  * **\<form:option>** : \<option> 태그 한 개를 생성한다.

<br>

\<select> 태그는 선택 옵션을 제공할 때 주로 사용한다.

\<select> 태그에서 사용할 옵션 목록을 Model을 통해 전달한다.

```java
@GetMapping("/login")
public String form(Model model) {
  List<String> loginTypes = new ArrayList<>();
  loginTypes.add("일반회원");
  loginTypes.add("기업회원");
  loginTypes.add("헤드헌터회원");
  model.addAttribute("loginTypes", loginTypes);
  return "login/form";
}
```

\<form:select> 커스텀 태그를 사용하면 뷰에 전달할 모델 객체를 갖고 간단하게 \<select>와 \<option> 태그를 생성할 수 있다.

```jsp
<form:form modelAttribute="login">
  <p>
    <label for="loginType">로그인 타입</label>
    <form:select path="loginType" items="${loginTypes}"/>
  </p>
</form:form>
```

<br>

\<form:options> 커스텀 태그는 주로 콜렉션에 없는 값을 \<option> 태그로 추가할 때 사용한다.

```jsp
<form:select path="loginType">
  <option value="">--- 선택하세요 ---</option>
  <form:options items="${loginTypes}"/>
</form:select>
```

<br>

\<form:option> 커스텀 태그는 \<option> 태그를 직접 지정할 때 사용된다.

```jsp
<form:select path="loginType">
  <form:option value="일반회원"/>
  <form:option value="기업회원">기업</form:option>
  <form:option value="헤드헌터회원" label="헤드헌터"/>
</form:select>
```

<br>

\<option> 태그를 생성하는데 사용할 콜렉션 객체가 일반 객체일 때는 **itemLabel, itemVaule 속성들을 사용한다.**

* **Code.java**

  ```java
  public class Code {
    private String code;
    private String label;
    
    public Code(String code, String label) {
      this.code = code;
      this.label = label;
    }
    
    public String getCode() {
      return code;
    }
    
    public String getLabel() {
      return label;
    } 
  }
  ```

  * 콜렉션에 저장된 객체의 특정 프로퍼티를 사용해야 하는 경우 **itemValue 속성과 itemLabel 속성을 사용한다.**

* **jsp 파일**

  ```jsp
  <form:select path="jobCode">
    <option value="">--- 선택하세요 ---</option>
    <form:options items="${jobCodes}" itemLabel="label" itemValue="code"/>
  </form:select>
  ```

<br>

## 14.4. 체크박스 관련 커스텀 태그 : \<form:checkboxes>, \<form:checkbox>

한 개 이상의 값을 커맨드 객체의 특정 프로퍼티에 저장하고 싶다면 배열이나 List와 같은 타입을 사용해서 값을 저장한다.

