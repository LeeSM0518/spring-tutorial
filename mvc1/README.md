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