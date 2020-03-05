# Chapter 10. 스프링 MVC 프레임워크 동작 방식

* 이 장에서 다룰 내용
  * 스프링 MVC 구성 요소
  * DispatcherServlet
  * WebMvcConfigurer과 스프링 MVC 설정

스프링 MVC는 웹 요청을 처리하기 위해 다양한 구성 요소를 연동하는 것의 핵심 구성 요소에 대해서 살펴보자.
<br>

# 1. 스프링 MVC 핵심 구성 요소

![image](https://user-images.githubusercontent.com/43431081/75980857-9e73b300-5f26-11ea-8a1f-44b1eefd4c43.png)

* **\<\<spring bean>>**
  * 스프링 빈을 등록해야 하는 것을 의미한다.
* **회색 배경**
  * 개발자가 직접 구현해야 하는 요소이다.
  * 컨트롤러 구성 요소는 개발자가 직접 구현해야 하고 스프링 빈으로 등록해야 한다.
    * ex) HelloControllor.java
* **DispatcherServlet**
  * 웹 브라우저로부터 요청이 들어오면 그 요청을 처리하기 위한 컨트롤러 객체를 검색한다.
  * 직접 컨트롤러를 검색하지 않고 <u>HandlerMapping 이라는 빈 객체에게 컨트롤러 검색을 요청한다.</u>
  * HandlerMapping이 찾아준 컨트롤러 객체를 처리할 수 있는 <u>HandlerAdapter 빈에게 요청 처리를 위임한다.</u>
* **HandlerMapping**
  * 클라이언트의 요청 경로를 이용해서 이를 <u>처리할 컨트롤러 빈 객체를 DispatcherServlet에 전달한다.</u>
* **HandlerAdapter**
  * <u>@Controller, Controller 인터페이스, HttpRequestHandler 인터페이스를 동일한 방식으로</u> 처리하기 위해 중간에 사용되는 것이다.
  * 컨트롤러의 알맞은 메서드를 호출해서 요청을 처리하고 그 결과를 DispatcherServlet에 리턴한다.
  * 이때 처리 결과를 <u>ModelAndView라는 객체로 변환해서 리턴한다.</u>
* **ViewResolver**
  * ModelAndView는 컨트롤러가 리턴한 뷰 이름을 담고 있는데 ViewResolver는 이 <u>뷰 이름에 해당하는 View 객체를 찾거나 생성해서 리턴한다.</u>
  * 매번 새로운 View 객체를 생성해서 리턴한다.
* **View**
  * JSP를 사용하는 경우 JSP를 실행함으로써 웹 브라우저에 전송할 <u>응답 결과를 생성한다.</u>

<br>

## 1.1. 컨트롤러와 핸들러

클라이언트의 요청을 실제로 처리하는 것은 컨트롤러이고 DispatcherServlet은 클라이언트의 요청을 전달받는 창구 역할을 한다.

스프링 MVC는 **웹 요청을 실제로 처리하는 객체를 핸들러(Handler)라고** 표현하고 있으며 @Controller 적용 객체나 Controller 인터페이스를 구현한 객체는 모두 스프링 MVC 입장에서는 핸들러가 된다. 따라서 **특정 요청 경로를 처리해주는 핸들러를 찾아주는 객체를 HandlerMapping** 이라고 부른다.

핸들러가 ModelAndView를 리턴하는 객체도 있고, 그렇지 않은 객체도 있다. 따라서 핸들러의 처리 결과를 **ModelAndView로 변환해주는 객체가 필요하며 HandlerAdapter가 이 변환을 처리해준다.**

핸들러 객체의 실제 타입마다 그에 알맞은 HandlerMapping과 HandlerAdapter가 존재하기 때문에, **사용할 핸들러의 종류에 따라 해당 HandlerMapping과 HandlerAdapter를 스프링 빈으로 등록해야 한다.**

물론 스프링이 제공하는 설정 기능을 사용하면 이 두 종류의 빈을 직접 등록하지 않아도 된다.

<br>

# 2. DispatcherServlet과 스프링 컨테이너

* **web.xml**

  ```xml
  ...
  <init-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>
      config.MvcConfig
      config.ControllerConfig
    </param-value>
  </init-param>
  ...
  ```

  * DispatcherServlet은 전달받은 설정 파일을 이용해서 스프링 컨테이너를 생성한다.
  * HandlerMapping, HandlerAdapter, 컨트롤러, ViewResolver 등의 빈은 DispatcherServlet이 생성한 스프링 컨테이너에서 구한다.

* DispatcherServlet은 스프링 컨테이너를 생성하고, 그 컨테이너로부터 필요한 빈 객체를 구한다.

  ![image](https://user-images.githubusercontent.com/43431081/76002961-b9f0b500-5f4a-11ea-84f1-b474022f56d1.png)

<br>

# 3. @Controller를 위한 HandlerMapping과 HandlerAdapter

@Controller 적용 객체는 DispatcherServlet 입장에서 보면 한 종류의 핸들러 객체이다.

핸들러에 알맞은 HandlerMapping 빈과 HandlerAdapter 빈이 스프링 설정에 등록되어 있어야 한다.

하지만 HandlerMapping이나 HandlerAdapter 클래스를 빈으로 등록하지 않고 단지 **@EnableWebMvc 애노테이션만 추가했다.**

```java
@Configuration
@EnableWebMvc
public class MvcConfig {
  ...
}
```

* 위 설정은 매우 다양한 스프링 빈 설정을 추가해준다.
* 이 태그가 빈으로 추가해주는 클래스 중에는 @Controller 타입의 핸들러 객체를 처리하기 위한 다음의 두 클래스도 포함되어 있다.
  * org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
  * org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
    * **RequestMappingHandlerMapping 애노테이션** : @Controller 애노테이션이 적용된 객체의 요청 매핑 애노테이션(@GetMapping) 값을 이용해서 웹 브라우저의 요청을 처리할 컨트롤러 빈을 찾는다.
    * **RequestMappingHandlerAdapter 애노테이션** : 컨트롤러의 메서드를 알맞게 실행하고 그 결과를 ModelAndView 객체로 변환해서 DispatcherServlet에 리턴한다.
* RequestMappingHandlerAdapter는 컨트롤러 메서드 결과 값이 String 타입이면 해당 값을("hello") 뷰 이름으로 갖는 ModelAndView 객체를 생성해서 DispatcherServlet에 리턴한다. 이때 첫 번째 파라미터로 전달한 Model 객체에 보관된 값도(String name) ModelAndView에 함께 전달한다.

<br>

# 4. WebMvcConfigurer 인터페이스와 설정

* **@EnableWebMvc**
  * @Controller 애노테이션을 붙인 컨트롤러를 위한 설정을 생성한다.
  * WebConfigurer 타입의 빈을 이용해서 MVC 설정을 추가로 생성한다.