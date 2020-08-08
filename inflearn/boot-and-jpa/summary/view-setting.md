# View 환경 설정

* **thymeleaf**

  * 장점

    ```jsp
    <table>
      <thead>
        <tr>
          <th th:text="#{msgs.headers.name}">Name</th>
          <th th:text="#{msgs.headers.price}">Price</th>
        </tr>
      </thead>
      <tbody>
        <tr th:each="prod: ${allProducts}">
          <td th:text="${prod.name}">Oranges</td>
          <td th:text="${#numbers.formatDecimal(prod.price, 1, 2)}">0.99</td>
        </tr>
      </tbody>
    </table>
    ```

    * jsp와 다르게 html 형식을 깨지 않고 사용할 수 있다.

<br>

* **스프링 가이드** : https://spring.io/guides#getting-started-guides

<br>

## 간단한 뷰 만들기

* HelloController.java

  ```java
  package japbook.jpashop;
  
  import org.springframework.stereotype.Controller;
  import org.springframework.ui.Model;
  import org.springframework.web.bind.annotation.GetMapping;
  
  @Controller
  public class HelloController {
  
    @GetMapping("hello")
    // Model 에 데이터를 실어서 View 에 넘긴다.
    public String hello(Model model){
      model.addAttribute("data", "hello!!!");
      return "hello"; // "hello" 는 View의 이름 => hello.html
    }
  
  }
  ```

* resources/templates/hello.html

  ```html
  <!DOCTYPE HTML>
  <html xmlns:th="http://www.thymeleaf.org">
    <head>
      <title>Hello</title>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
      <p th:text="'안녕하세요. ' + ${data}" >안녕하세요. 손님</p>
    </body>
  </html>
  ```

* 실행하여 테스트

  * URL : `localhost:8080/hello`

<br>

정적 파일은 `resources/static` 에 위치시킨다.

* `static/index.html`

  ```html
  <!doctype html>
  <html xmlns:th="http://www.thymeleaf.org">
    <head>
      <title>Hello</title>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <body>
      Hello
      <a href="/hello">hello</a>
    </body>
  </html>
  ```

* 실행하여 테스트: `localhost:8080`

<br>

View 파일을 수정하고 매번 프로젝트를 재시작하면 너무 번거롭다. 이를 보완하기 위해서 라이브러리를 추가한다.

1. `build.gradle` 에 라이브러리를 추가한다.

  ````java
  implementation 'org.springframework.boot:spring-boot-devtools' // 추가
  ````
  
2. View 파일이 바뀌게 되었을 때, `Build` 에서 `Recompile "filename"` 을 실행하면 프로젝트를 재시작 안해도 View가 업데이트 된다.
