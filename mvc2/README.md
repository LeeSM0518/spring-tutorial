# Chapter 12. MVC 2: 메시지, 커맨드 객체 검증

* 이 장에서 다룰 내용
  * **메시지 처리**
  * **커맨드 객체 검증과 에러 메시지**

<br>

# 1. 프로젝트 준비

11장에서 사용한 파일들 그대로 사용한다.

<br>

# 2. \<spring:message> 태그로 메시지 출력하기

* 사용자 화면에 보일 문자열을 JSP에 직접 코딩할 때의 문제점
  * 문자열을 직접 하드 코딩하면 동일 문자열을 변경할 때 문제가 있다(ex. 로그인 폼, 회원 가입 폼, 회원 정보 수정 폼).
  * 다국어 지원에 문제가 있다(ex. '이메일' 을 'E-mail' 로 바꿔야 할 때).

<br>

위의 문제들을 해결하는 방법은 뷰 코드에서 사용할 **문자열을 언어별로 파일에 보관하고 뷰 코드는 언어에 따라 알맞은 파일에서 문자열을 읽어와 출력하는 것이다.**

<br>

문자열을 별도 파일에 작성하고 JSP 코드에서 이를 사용하려면 다음 작업을 하면 된다.

1. 문자열을 담은 메시지 파일을 작성한다.
2. 메시지 파일에서 값을 읽어오는 MessageSource 빈을 설정한다.
3. JSP 코드에서 \<spring:message> 태그를 사용해서 메시지를 출력한다.

<br>

먼저 메시지 파일을 작성해보자. 메시지 파일은 자바의 **프로퍼티 파일 형식으로** 작성한다. 메시지 파일을 보관하기 위해 **src/main/resources에 message 폴더를 생성하고** 이 폴더에 **label.properties** 파일을 생성한다.

이 장에서는 **UTF-8** 인코딩을 사용해서 label.properties 파일을 작성한다.

<br>

* **resources/message/label.properties**

  ```properties
  member.register=회원가입
  
  term=약관
  term.agree=약관동의
  next.btn=다음단계
  
  member.info=회원정보
  email=이메일
  name=이름
  password=비밀번호
  password.confirm=비밀번호 확인
  register.btn=가입 완료
  
  register.done=<strong>{0}님</strong>, 회원 가입을 완료했습니다.
  
  go.main=메인으로 이동
  ```

<br>

다음으로 MessageSource 타입의 빈을 추가하자.

* **java/config/MvcConfig.java**

  ```java
  @Configuration
  @EnableWebMvc
  public class MvcConfig implements WebMvcConfigurer {
  
    ... 생략
  
    @Bean
    public MessageSource messageSource() {
      ResourceBundleMessageSource ms =
        new ResourceBundleMessageSource();
      ms.setBasenames("message.label");
      ms.setDefaultEncoding("UTF-8");
      return ms;
    }
  
  }
  ```

  * **ms.setBasenames("message.label")**
    * message 패키지에 속한 label 프로퍼티 파일로부터 메시지를 읽어온다고 설정
    * 이 메서드는 가변  인자이므로 사용할 메시지 프로퍼티 목록으로 전달할 수 있다.
  * **ms.setDefaultEncoding("UTF-8")**
    * defaultEncoding 속성의 값으로 "UTF-8" 로 지정
  * 위 코드에서 주의할 점은 **빈의 아이디를 "messageSource"로 지정해야** 한다는 것이다. 다른 이름을 사용할 경우 정상적으로 동작하지 않는다.

<br>

MessageSource를 사용해서 메시지를 출력하도록 JSP 코드를 수정해보자.

* **webapp/WEB-INF/view/register/step1.jsp**

  ```jsp
  <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title><spring:message code="member.register"/></title>
    </head>
    <body>
      <h2><spring:message code="term"/></h2>
      <p>약관 내용</p>
      <form action="step2" method="post">
        <label>
          <input type="checkbox" name="agree" value="true">
          <spring:message code="term.agree"/>
        </label>
        <input type="submit" value="<spring:message code="next.btn"/>">
      </form>
    </body>
  </html>
  ```

* **webapp/WEB-INF/view/register/step2.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
  <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
  <html>
    <head>
      <title><spring:message code="member.register"/></title>
    </head>
    <body>
      <h2><spring:message code="member.info"/></h2>
      <form:form action="step3" modelAttribute="registerRequest">
        <p>
          <label><spring:message code="email"/>:<br>
            <form:input path="email"/>
          </label>
        </p>
        <p>
          <label><spring:message code="name"/>:<br>
            <form:input path="name"/>
          </label>
        </p>
        <p>
          <label><spring:message code="password"/>:<br>
            <form:input path="password"/>
          </label>
        </p>
        <p>
          <label><spring:message code="password.confirm"/>:<br>
            <form:password path="confirmPassword"/>
          </label>
        </p>
        <input type="submit" value="<spring:message code="register.btn"/>">
      </form:form>
    </body>
  </html>
  ```

* **webapp/WEB-INF/view/register/step3.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
  <html>
    <head>
      <title><spring:message code="member.register"/></title>
    </head>
    <body>
      <p>
        <spring:message code="register.done" arguments="${registerRequest.name}"/>
      </p>
      <p><a href="<c:url value='/main'/>">[<spring:message code="go.main"/>]</a></p>
    </body>
  </html>
  ```

* **수정한 코드들의 공통점**

  * \<string:message> 커스텀 태그를 사용하기 위해 태그 라이브러리 설정 추가

    : \<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

  * \<spring:message> 태그를 이용해서 메시지 출력

<br>

\<spring:message> 태그의 code 값은 앞서 작성한 프로퍼티 파일의 프로퍼티 이름과 일치한다.

\<spring:message> 태그는 MessageSource로부터 코드에 해당하는 메시지를 읽어온다. 

MessageSource는 label.properties 파일로부터 메시지를 읽어온다.

<br>

### 다국어 지원 위한 메시지 파일

다국어 메시지를 지원하려면 각 프로퍼티 파일 이름에 언어에 해당하는 **로케일 문자를 추가한다.**

* ex) label_ko.properties(한국어), label_en.properties(영어)

스프링 MVC는 웹 브라우저가 전송한 Accept-Language 헤더를 이용해서 Locale을 구한다.

이 Locale을 MessageSource에서 메시지를 구할 때 사용한다.

<br>

## 2.1. 메시지 처리를 위한 MessageSource와 \<spring:message> 태그

스프링은 로케일(지역)에 상관없이 일관된 방법으로 문자열(메시지)을 관리할 수 있는 MessageSource 인터페이스를 정의하고 있다.

* **MessageSource 인터페이스**

  ```java
  package org.springframework.context;
  
  import java.util.Locale;
  
  public interface MessageSource {
    String getMessage(String code, Object[] args,
                     String defaultMessage, Locale locale);
    
    String getMessage(String code, Object[] args, Locale locale)
      throws NoSuchMessageException;
    
    ... // 일부 메서드 생략
  }
  ```

  * 특정 로케일에 해당하는 메시지가 필요한 코드는 getMessage() 메서드를 이용해서 필요한 메시지를 가져와서 사용한다.
  * **code 파라미터** : 메시지를 구분하기 위한 코드
  * **locale 파라미터** : 지역을 구분하기 위한 locale

<br>

MessageSource의 구현체로는 자바의 프로퍼티 파일로부터 메시지를 읽어오는 **ResourceBundleMessageSource 클래스를** 사용한다.

ResourceBundleMessageSource 클래스는 자바의 리소스번들(ResourceBundle)을 사용하기 때문에 해당 프로퍼티 파일이 클래스 패스에 위치해야 한다.

\<spring:message> 태그는 스프링 설정에 등록된 'messageSource' 빈을 이용해서 메시지를 구한다. 즉 \<spring:message> 태그를 실행하면 내부적으로 MessageSource의 getMessage() 메서드를 실행해서 필요한 메시지를 구한다.

\<spring:message> 태그의 code 속성에 지정한 메시지가 존재하지 않으면 익셉션**(HTTP 500 에러)**이 발생한다.

<br>

## 2.2. \<spring:message> 태그의 메시지 인자 처리

```properties
register.done=<strong>{0}님</strong>, 회원 가입을 완료했습니다.
```

* **{0}** : 인덱스 기반 변수 중 0번 인덱스(첫 번째 인덱스)의 값으로 대치되는 부분을 표시한 것이다.

* \<spring:message> 태그를 사용할 때에는 **arguments 속성을** 사용해서 인덱스 기반 변수값을 전달한다.

  ```jsp
  <spring:message code="register.done" arguments="${registerRequest.name}"/>
  ```

<br>

label.properties 파일의 register.done 프로퍼티에 {1}을 추가해보자.

```properties
register.done=<string>{0}님 ({1})</strong>, 회원 가입을 완료했습니다.
```

이 메시지를 사용하려면 두 개의 인자를 전달해야 한다.

* **두 개 이상의 값을 전달하는 방법**

  * 콤마로 구분한 문자열

    ```jsp
    <spring:message code="register.done" arguments="${registerRequest.name},${registerRequest.email}"/>
    ```

  * 객체 배열

  * \<spring:argument> 태그 사용

    ```jsp
    <spring:message code="register.done">
      <spring:argument value="${registerRequest.name}"/>
      <spring:argument value="${registerRequest.email}"/>
    </spring:message>
    ```

<br>

# 3. 커맨드 객체의 값 검증과 에러 메시지 처리

이전까지의 코드는 올바르지 않은 이메일 주소를 입력해도 가입 처리가 되고 이름을 입력하지 않아도 가입할 수 있다. 즉 입력한 값에 대한 검증 처리를 하지 않는다.

그리고 가입이 실패한 이유를 보여주지 않기 때문에 사용자는 혼란을 겪게 된다.

폼 값 검증과 에러 메시지 처리는 어플리케이션을 개발할 때 놓쳐서는 안 된다.

<br>

스프링은 이 두 가지 문제를 처리하기 위해 방법들을 제공한다.

* **커맨드 객체를 검증하고 결과를 에러 코드로 저장**
* **JSP에서 에러 코드로부터 메시지 출력**

<br>

## 3.1. 커맨드 객체 검증과 에러 코드 지정하기

스프링 MVC에서 커맨드 객체의 값이 올바른지 검사하기 위한 인터페이스

* **org.springframework.validation.Validator**
* **org.springframework.validation.Errors**

<br>

**Validator 인터페이스**

```java
package org.springframework.validation;

public interface Validator {
  boolean supports(Class<?> clazz);
  void validate(Object target, Errors errors);
}
```

* **supports()** : Validator가 검증할 수 있는 타입인지 검사한다.
* **validate()** : 첫 번째 파라미터로 전달받은 객체를 검증하고 오류 결과를 Errors에 담는 기능을 정의한다.

<br>

Validator 인터페이스를 구현해보자.

* **java/controller/RegisterRequestValidator.java**

  ```java
  public class RegisterRequestValidator implements Validator {
  
    private static final String emailRegExp =
      "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
      "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private Pattern pattern;
  
    public RegisterRequestValidator() {
      pattern = Pattern.compile(emailRegExp);
    }
  
    @Override
    public boolean supports(Class<?> clazz) {
      // clazz 객체가 RegisterRequest 클래스로 타입 변환이 가능한지 확인한다.
      // 스프링 MVC가 자동으로 검증 기능을 수행하도록 설정하려면 올바르게 구현해야 한다.
      return RegisterRequest.class.isAssignableFrom(clazz);
    }
  
    @Override
    // target : 검사 대상 객체
    // errors : 검사 결과 에러 코드를 설정하기 위한 객체
    public void validate(Object target, Errors errors) {
      // 전달받은 target을 실제 타입으로 변환
      RegisterRequest regReq = (RegisterRequest) target;
      // 검사 대상 객체의 특정 프로퍼티나 상태가 올바른지 검사
      if (regReq.getEmail() == null || regReq.getEmail().trim().isEmpty()) {
        // 올바르지 않다면 Errors의 rejectValue() 메서드를 이용해서 에러 코드 저장
        errors.rejectValue("email", "required");
      } else {
        // 정규 표현식을 이용해서 이메일이 올바른지 확인
        Matcher matcher = pattern.matcher(regReq.getEmail());
        if (!matcher.matches()) {
          // 정규 표현식이 일치하지 rejectValue 를 통해 에러 코드 추가
          errors.rejectValue("email", "bad");
        }
      }
      // ValidationUtils : 객체의 값 검증 코드를 간결하게 작성할 수 있도록 도와준다.
  
      // 검사 대상 객체의 "name" 프로퍼티가 null 이거나 공백문자로만 되어 있는 경우
      // "name" 프로퍼티의 에러 코드로 "required"를 추가한다.
      // Errors 객체는 커맨드 객체의 특정 프로퍼티 값을 구할 수 있는
      //  getFieldValue() 메서드를 제공한다.
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "required");
      ValidationUtils.rejectIfEmpty(errors, "password", "required");
      ValidationUtils.rejectIfEmpty(errors, "confirmPassword", "required");
      if (!regReq.getPassword().isEmpty()) {
        if (!regReq.isPasswordEqualToConfirmPassword()) {
          errors.rejectValue("confirmPassword", "nomatch");
        }
      }
    }
  
  }
  ```

* **java/controller/RegisterController.java**

  ```java
  @Controller
  public class RegisterController {
  
    ... 생략
  
    @PostMapping("/register/step3")
    // 스프링 MVC가 handleStep3() 메서드를 호출할 때
    //  커맨드 객체와 연결된 Errors 객체를 생성해서 파라미터로 전달한다.
    public String handleStep3(RegisterRequest regReq, Errors errors) {
      // 커맨드 객체의 값이 올바른지 검사하고 그 결과를 Errors 객체에 담는다.
      new RegisterRequestValidator().validate(regReq, errors);
      // 에러가 존재하는지 검사
      if (errors.hasErrors())
        return "register/step2";
      try {
        memberRegisterService.regist(regReq);
        return "register/step3";
      } catch (DuplicateMemberDaoException ex) {
        // 동일한 이메일을 가진 회원 데이터가 이미 존재시 발생
        // 이메일 중복 에러를 추가하기 위해 "email" 프로퍼티의
        //  에러 코드로 "duplicate" 추가
        errors.rejectValue("email", "duplicate");
        return "register/step2";
      }
    }
  
  }
  ```

  * 커맨드 객체의 특정 프로퍼티가 아닌 커맨드 객체 자체가 잘못된 경우에는 **reject() 메서드를 사용한다.**

    ```java
    try {
      ... 인증 처리 코드
    } catch(WrongIdPasswordException ex) {
      // 특정 프로퍼티가 아닌 커맨드 객체 자체에 에러 코드 추가
      errors.reject("notMatchingIdPassword");
      return "login/loginForm";
    }
    ```

    * reject() 메서드는 개별 프로퍼티가 아닌 객체 자체에 에러 코드를 추가하므로 **이 에러를 글로벌 에러라고** 부른다.

  * 요청 매핑 애노테이션을 붙인 메서드에 Errors 타입의 파라미터를 추가할 때 주의할 점은 **Errors 타입 파라미터는 반드시 커맨드 객체를 위한 파라미터 다음에 위치해야 한다.**

  * Errors 타입 파라미터가 커맨드 객체 앞에 위치하면 실행 시점에 에러 발생

  > Errors 대신에 BindingResult 인터페이스를 파라미터 타입으로 사용해도 된다.