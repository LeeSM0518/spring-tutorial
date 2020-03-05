# Chapter 09. 스프링 MVC 시작하기

* 이 장에서 다룰 내용
  * 간단한 스프링 MVC 예제

<br>

# 1. 프로젝트 생성

* **프로젝트를 위한 디렉토리 생성**
  * src/main/webapp
  * src/main/webapp/WEB-INF
  * src/main/webapp/WEB-INF/view

<br>

**webapp** : HTML, CSS, JS, JSP 등 웹 어플리케이션을 구현하는데 필요한 코드가 위치한다.

**WEB-INF** : web.xml 파일이 위치한다.

<br>

1. gradle에 war, java 플러그인을 추가한다.

   ```
   plugins {
       id 'java'
   }
   
   group 'org.example'
   version '1.0-SNAPSHOT'
   
   sourceCompatibility = 1.8
    
   apply plugin: 'war'
   apply plugin: 'java'
   
   repositories {
       mavenCentral()
   }
   
   dependencies {
       testCompile group: 'junit', name: 'junit', version: '4.12'
   }
   
   ```

2. dependency 들과 인코딩 방식을 추가한다.

   ```
   plugins {
       id 'java'
   }
   
   group 'org.example'
   version '1.0-SNAPSHOT'
   
   sourceCompatibility = 1.8
   compileJava.options.encoding("UTF-8")
   
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
   }
   
   ```

   * 서블릿, JSP, JSTL, 스프링 MVC
   * UTF-8 인코딩 추가

<br>

## 2. Tomcat 설정

![image](https://user-images.githubusercontent.com/43431081/75961499-79bb1380-5f05-11ea-92f8-648ae2f40f91.png)

<br>

# 3. 스프링 MVC를 위한 설정

* **최소 설정**
  * 스프링 MVC의 주요 설정(HandlerMapping, ViewResolver 등)
  * 스프링의 DispatcherServlet 설정

<br>

## 3.1. 스프링 MVC 설정

* **/java/config/MvcConfig.java**

  ```java
  @Configuration
  // @EnableWebMvc : 스프링 MVC 설정을 활성화한다.
  @EnableWebMvc
  public class MvcConfig implements WebMvcConfigurer {
  
    @Override
    // DispatcherServlet의 매핑 경로를 '/'로 주었을 때,
    //  JSP/HTML/CSS 등을 올바르게 처리하기 위한 설정을 추가한다.
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
      configurer.enable();
    }
  
    @Override
    // JSP를 이용해서 컨트롤러의 실행 결과를 보여주기 위한 설정을 추가
    public void configureViewResolvers(ViewResolverRegistry registry) {
      registry.jsp("/WEB-INF/view/", ".jsp");
    }
  
  }
  ```

  * **@EnableWebMvc**
    * 내부적으로 다양한 빈 설정을 추가해준다.
  * **WebMvcConfigurer 인터페이스**
    * 스프링 MVC의 개별 설정을 조정할 때 사용한다.
    * 위에서 사용된 메서드들은 디폴트 서블릿과 ViewResolver와 관련된 설정을 조정한다.

<br>

## 3.2. web.xml 파일에 DispatcherServlet 설정

스프링 MVC가 웹 요청을 처리하려면 DispatcherServlet을 통해서 웹 요청을 받아야 한다.

이를 위해 **web.xml 파일에 DispatcherServlet을** 등록한다.

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

  * DispatcherServlet은 초기화 과정에서 contextConfiguration 초기화 파라미터에서 지정한 설정 파일을 이용해서 스프링 컨테이너를 초기화한다.

<br>

# 4. 코드 구현

* 작성할 코드
  * 클라이언트의 요청을 알맞게 처리할 컨트롤러
  * 처리 결과를 보여줄 JSP

<br>

## 4.1. 컨트롤러 구현

* **/java/chap09/HelloController.java**

  ```java
  // @Controller : 스프링 MVC에서 컨트롤러로 지정
  @Controller
  public class HelloController {
  
    // GetMapping : 메서드가 처리할 요청 경로를 지정
    //  GET 메서드에 매핑을 설정한다.
    @GetMapping("/hello")
    // Model : 컨트롤러의 처리 결과를 뷰에 전달할 때 사용
    public String hello(Model model,
                        // @RequestParam : HTTP 요청 파라미터의 값을 메서드의 
                        //  파라미터로 전달할때 사용
                        //  name 요청의 파라미터의 값을 name 파라미터에 전달
                        @RequestParam(value = "name", required = false) String name) {
      // "greeting" 이라는 모델 속성에 값을 설정한다.
      model.addAttribute("greeting", "안녕하세요, " + name);
      // "hello" : 컨트롤러의 처리 결과를 보여줄 뷰 이름
      return "hello";
    }
  
  }
  ```

<br>

* **컨트롤러(Controller)** 
  * 웹 요청을 처리하고 그 결과를 뷰에 전달하는 스프링 빈 객체이다.
  * **@Controller** : 컨트롤러로 사용될 클래스에 붙인다.
  * **@GetMapping, @PostMapping** : 요청 매핑 애노테이션을 이용해서 처리할 경로를 지정해 준다.

<br>

@GetMapping 애노테이션의 값은 **서블릿 컨텍스트 경로(웹 애플리케이션 경로)를** 기준으로 한다.

![image](https://user-images.githubusercontent.com/43431081/75967109-acb5d500-5f0e-11ea-929a-632111e50bdc.png)

* **@RequestParam** 

  * **value** : HTTP 요청 파라미터의 이름을 지정
  * **required** : 필수 여부 지정

* **model.addAttribute()**

  * 뷰에 전달할 데이터를 지정

    ```java
    model.addAttribute("greeting", "안녕하세요, " + name);
    ```

    * **첫 번째 파라미터** : 데이터를 식별하는데 사용되는 속성 이름
    * **두 번째 파라미터** : 속성 이름에 해당하는 값

* **return "hello"**

  * 컨트롤러의 실행 결과를 보여줄 뷰 이름을 리턴한다.
  * 뷰 이름은 논리적인 이름이며 실제로 뷰 이름에 해당하는 뷰 구현을 찾아주는 것은 **ViewResolver** 이다.

<br>

컨트롤러를 구현했다면 컨트롤러를 스프링 빈으로 등록해야 한ㄷ. 설정 파일을 작성해보자.

* **/java/config/ControllerConfig.java**

  ```java
  @Configuration
  public class ControllerConfig {
    
    @Bean
    public HelloController helloController() {
      return new HelloController();
    }
    
  }
  ```

<br>

## 4.2. JSP 구현

* **/webapp/WEB-INF/view/hello.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
  <head>
      <title>Title</title>
  </head>
  <body>
      인사말: ${greeting}
  </body>
  </html>
  ```

<br>

뷰 이름과 JSP 파일과의 연결은MvcConfig 클래스의 설정을 통해서 이루어진다.

```java
@Override
public void configureViewResolvers(ViewResolverRegistry registry) {
  registry.jsp("/WEB-INF/view/", ".jsp");
}
```

* **registry.jsp()**
  * JSP를 뷰 구현으로 사용할 수 있도록 해주는 설정이다.
    * **jsp()**
      * **첫 번째 인자** : JSP 파일 경로를 찾을 때 사용할 접두어
      * **두 번째 인자** : 집미사
    * 뷰 이름의 앞과 뒤에 각각 접두어와 접미사를 붙여서 최종적으로 사용할 JSP 파일의 경로를 결정한다.

![image](https://user-images.githubusercontent.com/43431081/75973354-5b124800-5f18-11ea-95d4-5c08840137ae.png)

<br>

hello.jsp 코드를 보면 **JSP EL(Expression Language)을** 사용했다.

```jsp
인사말: ${greeting}
```

* "greeting" 은 컨트롤러 구현에서 Model에 추가한 속성의 이름인 "greeting" 과 동일하다.
* 스프링 MVC 프레임워크가 모델에 추가한 속성을 JSP 코드에서 접근할 수 있게 **HttpServletRequest에** 옮겨준다.

![image](https://user-images.githubusercontent.com/43431081/75974012-716cd380-5f19-11ea-900e-17579c168129.png)

* JSP로 뷰 코드를 구현할 경우 컨트롤러에서 추가한 속성의 이름을 이용해서 속성값을 응답 결과에 출력하게 된다.

<br>

# 5. 실행하기

![image](https://user-images.githubusercontent.com/43431081/75974442-1c7d8d00-5f1a-11ea-9317-27f459ec6924.png)

* 실행 결과를 보면 hello.jsp 에서 생성한 결과가 웹 브라우저에 출력된 것을 알 수 있고, name 파라미터로 지정한 값이 HelloController를 거쳐 JSP 까지 전달된 것을 알 수 있다.