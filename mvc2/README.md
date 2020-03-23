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

  * **정규표현식**

    | 표현식 | 설명                                                         |
    | ------ | ------------------------------------------------------------ |
    | ^      | 문자열의 시작                                                |
    | $      | 문자열의 종료                                                |
    | .      | 임의의 한 문자                                               |
    | *      | 앞 문자가 없을 수도 무한정 많을 수도 있음                    |
    | +      | 앞 문자가 하나 이상                                          |
    | ?      | 앞 문자가 없거나 하나있음                                    |
    | [ ]    | 문자의 집합이나 범위를 나타내는 문자. <br />- 기호로 범위를 나타낸다. ^ 가 존재하면 not을 나타낸다. |
    | { }    | 횟수 또는 범위를 나타낸다.                                   |
    | ( )    | 소괄호 안의 문자를 하나의 문자로 인식                        |
    | \|     | 패턴 안에서 or 연산을 수행할 때 사용                         |
    | \s     | 공백 문자                                                    |
    | \S     | 공백 문자가 아닌 나머지 문자                                 |
    | \w     | 알파벳이나 숫자                                              |
    | \W     | 알파벳이나 숫자를 제외한 문자                                |
    | \d     | 숫자 [0-9]와 동일                                            |
    | \D     | 숫자를 제외한 모든 문자                                      |
    | \      | 정규표현식 역슬래시는 확장 문자<br />역슬래시 다음에 일반 문자가 오면 특수문자로 취급하고 역슬래시 다음에<br />특수문자가 오면 그 문자자체를 의미 |
    | (?!)   | 앞 부분에 (?!) 라는 옵션을 넣어주면 대소문자를 구분하지 않는다. |

    <br>

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

  <br>

## 3.2. Errors와 ValidationUtils 클래스의 주요 메서드

* **Errors 인터페이스가 제공하는 에러 코드 추가 메서드**
  * reject(String errorCode)
  * reject(String errorCode, String defaultMessage)
    * 에러 코드에 해당하는 메시지가 존재하지 않을 때 익셉션을 발생시키는 대신 defaultMessage를 출력한다.
  * reject(String errorCode, Object[ ] errorArgs, String defaultMessage)
    * 에러 코드에 해당하는 메시지가 {0}이나 {1}과 같이 인덱스 기반 변수를 포함할 때, errorArgs 파라미터를 이용해서 변수에 삽입될 값을 전달한다.
  * rejectValue(String field, String errorCode)
  * rejectValue(String field, String errorCode, String defaultMessage)
  * rejectValue(String field, String errorCode, Object[ ] errorArgs, String defaultMessage)

<br>

* **ValidationUtils 클래스가 제공하는 메서드**
  * rejectIfEmpty(Errors errors, String field, String errorCode)
    * field에 해당하는 프로퍼티 값이 null이거나 빈 문자열("")인 경우 에러 코드로 errorCode를 추가한다.
  * rejectIfEmpty(Errors errors, String field, String errorCode, Object[ ] errorArgs)
  * rejectIfEmptyOrWhitespace(Errors errors, String field, String errorCode)
    * null 이거나 빈 문자열인 경우 그리고 공백 문자(스페이스, 탭 등)로만 값이 구성된 경우 에러 코드 추가
  * rejectIfEmptyOrWhitespace(Errors errors, String field, String errorCode, Object[ ] errorArgs)

<br>

## 3.3. 커맨드 객체의 에러 메시지 출력하기

Errors에 에러 코드를 추가하면 JSP는 스프링이 제공하는 **\<form:errors>** 태그를 사용해서 에러에 해당하는 메시지를 출력할 수 있다.

* **webapp/WEB-INF/view/step2.jsp**

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
            <form:errors path="email"/>
          </label>
        </p>
        <p>
          <label><spring:message code="name"/>:<br>
            <form:input path="name"/>
            <form:errors path="name"/>
          </label>
        </p>
        <p>
          <label><spring:message code="password"/>:<br>
            <form:input path="password"/>
            <form:errors path="password"/>
          </label>
        </p>
        <p>
          <label><spring:message code="password.confirm"/>:<br>
            <form:password path="confirmPassword"/>
            <form:errors path="confirmPassword"/>
          </label>
        </p>
        <input type="submit" value="<spring:message code="register.btn"/>">
      </form:form>
    </body>
  </html>
  ```

  * \<form:errors> 태그의 path 속성은 에러 메시지를 출력할 프로퍼티 이름을 지정한다.

<br>

* **에러 코드에 해당하는 메시지 코드를 찾을 때의 규칙**
  * 에러코드 + "." + 커맨드객체이름 + "." + 필드명
  * 에러코드 + "." + 필드명
  * 에러코드 + "." + 필드타입
  * 에러코드

<br>

* **프로퍼티 타입이 List나 목록인 경우 메시지 코드 생성 방식**
  * 에러코드 + "." + 커맨드객체이름 + "." + 필드명[인덱스].중첩필드명
  * 에러코드 + "." + 커맨드객체이름 + "." + 필드명.중첩필드명
  * 에러코드 + "." + 필드명[인덱스].중첩필드명
  * 에러코드 + "." + 중첩필드명
  * 에러코드 + "." + 필드타입
  * 에러코드

<br>

예를 들어 errors.rejectValue("email", "required") 코드로 "email" 프로퍼티에 "required" 에러 코드를 추가했고 커맨드 객체의 이름이 "registerRequest" 라면 다음 순서대로 메시지 코드를 검색한다.

1. required.registerRequest.email
2. required.email
3. required.String
4. required

위와 같은 순서로 검색하고 이 중 우선 순위가 높은 메시지 코드를 사용해서 메시지를 출력한다.

<br>

특정 프로퍼티가 아닌 커맨드 객체에 추가한 글로벌 에러 코드는 다음과 같은 순서대로 메시지 코드를 검색한다.

1. 에러코드 + "." + 커맨드객체이름
2. 에러코드

<br>

에러 코드에 해당하는 메시지를 메시지 프로퍼티 파일에 추가해주어야 한다.

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
  
  #register.done=<strong>{0}님</strong>, 회원 가입을 완료했습니다.
  register.done=<string>{0}님 ({1})</strong>, 회원 가입을 완료했습니다.
  
  go.main=메인으로 이동
  
  required=필수항목입니다.
  bad.email=이메일이 올바르지 않습니다.
  duplicate.email=중복된 이메일입니다.
  nomatch.confirmPassword=비밀번호와 확인이 일치하지 않습니다.
  ```

<br>

## 3.4. \<form:errors> 태그의 주요 속성

\<form:errors> 커스텀 태그는 프로퍼티에 추가한 에러 코드 개수만큼 에러 메시지를 출력한다.

* **에러 메시지 구분 속성**

  * **element** : 각 에러 메시지를 출력할 때 사용할 HTML 태그. 기본 값은 span 이다.
  * **delimiter** : 각 에러 메시지를 구분할 때 사용할  HTML 태그. 기본 값은 \<br/> 이다.

  ```jsp
  <form:errors path="userId" element="div" delimeter=""/>
  ```

  * path 속성을 지정하지 않으면 글로벌 에러에 대한 메시지를 출력한다.

<br>

# 4. 글로벌 범위 Validator와 컨트롤러 범위 Validator

스프링 MVC는 모든 컨트롤러에 적용할 수 있는 글로벌 Validator와 단일 컨트롤러에 적용할 수 있는 Validator를 설정하는 방법을 제공한다.

이를 사용하면 **@Valid 애노테이션을** 사용해서 커맨드 객체에 검증 기능을 적용할 수 있다.

<br>

## 4.1. 글로벌 범위 Validator 설정과 @Valid 애노테이션

글로벌 범위 Validator는 모든 컨트롤러에 적용할 수 있는 Validator이다.

* **글로벌 범위 Validator 적용 방법**
  * 설정 클래스에서 WebMvcConfigurer의 getValidator() 메서드가 Validator 구현 객체를 리턴하도록 구현
  * 글로벌 범위 Validator가 검증할 커맨드 객체에 @Valid 애노테이션 적용

<br>

글로벌 범위 Validator를 설정해보자.

* **java/config/MvcConfig.java**

  ```java
  @Configuration
  @EnableWebMvc
  public class MvcConfig implements WebMvcConfigurer {
  
    @Override
    public Validator getValidator() {
      return new RegisterRequestValidator();
    }
    
    ... 생략
  ```

  * 스프링 MVC는 WebMvcConfigurer 인터페이스의 getValidator() 메서드가 리턴한 객체를 글로벌 범위 Validator로 사용한다.
  * 글로벌 범위 Validator를 지정하면 @Valid 애노테이션을 사용해서 Validator를 적용할 수 있다.

<br>

@Valid 애노테이션은 Bean Validation API에 포함되어 있다. 이 API를 사용하려면  의존 설정에 validation-api 모듈을 추가해야 한다.

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
      
    // validation API
    implementation 'javax.validation:validation-api:2.0.1.Final'
}
```

<br>

* **java/controller/RegisterController.java**

  ```java
  @Controller
  public class RegisterController {
  
    ...
  
    @PostMapping("/register/step3")
    public String handleStep3(@Valid RegisterRequest regReq, Errors errors) {
      // 에러가 존재하는지 검사
      if (errors.hasErrors())
        return "register/step2";
      try {
        memberRegisterService.regist(regReq);
        return "register/step3";
      } catch (DuplicateMemberDaoException ex) {
        errors.rejectValue("email", "duplicate");
        return "register/step2";
      }
    }
  
  }
  ```

  * 커맨드 객체에 해당하는 파라미터에 @Valid 애노테이션을 붙이면 글로벌 범위 Validator가 해당 타입을 검증할 수 있는지 확인한다.
  * 검증 가능하면 실제 검증을 수행하고 그 결과를  Errors에 저장한다.
  * @Valid 애노테이션을 사용할 때 주의할 점은 **Errors 타입 파라미터가 없으면 검증 실패 시 400 에러를 응답한다.**

<br>

### 글로벌 Validator의 범용성

스프링  MVC는 자체적으로  제공하는 글로벌 Validator가 존재하는데 이 Validator를 사용하면 Bean Validation이 제공하는 애노테이션을 이용해서 값을 검증할 수 있다.

<br>

## 4.2. @InitBinder 애노테이션을 이용한 컨트롤러 범위 Validator

@InitBinder 애노테이션을 이용하면 컨트롤러 범위 Validator를 설정할 수 있다.

* **java/controller/RegisterController.java**

  ```java
  @Controller
  public class RegisterController {
  
    ... 생략
  
    @PostMapping("/register/step3")
    public String handleStep3(@Valid RegisterRequest regReq, Errors errors) {
      if (errors.hasErrors())
        return "register/step2";
      try {
        memberRegisterService.regist(regReq);
        return "register/step3";
      } catch (DuplicateMemberDaoException ex) {
        errors.rejectValue("email", "duplicate");
        return "register/step2";
      }
    }
    
    // 어떤 Validator가 커맨드 객체를 검증할지를 정의한다.
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
      // 컨트롤러 범위에 적용할 Validator 를 설정한다.
      binder.setValidator(new RegisterRequestValidator());
    }
  
  }
  ```

  * @Valid 애노테이션을 붙인 RegisterRequest를 검증할 때 컨트롤러 범위 Validator인 RegisterRequestValidator를 사용한다.
  * @InitBinder가 붙은 메서드는 컨트롤러의 요청 처리 메서드들 실행하기 전에 매번 실행해서 WebDataBinder를 초기화한다.

<br>

### 글로벌 범위  Validator와 컨트롤러 범위  Validator의 우선 순위

@InitBinder 애노테이션을 붙인 메서드에 전달되는 WebDataBinder는 내부적으로 Validator 목록을 갖는다. 이 목록에는 글로벌 범위 Validator가 기본적으로 포함된다.

그리고 setValidator() 메서드를 사용하면 글로벌 범위 Validator 대신에 컨트롤러 범위 Validator를 사용하게 된다.

<br>

# 5. Bean Validation을 이용한 값 검증 처리

Bean Validation 에는 @Valid 애노테이션뿐만 아니라 @NotNull, @Digits, @Size 등의 애노테이션을 정의하고 있다.

이 애노테이션을 사용하면 **Validator 작성 없이 애노테이션만으로 커맨드 객체의 값 검증을 처리할 수 있다.**

> Bean Validation 2.0 버전을 JSR 380 이라고 한다. 여기서 JSR은 **Java Specifiation Request의** 약자로 **자바 스펙을 기술한 문서를 의미한다.** 각 스펙마다 고유한  JSR 번호를 갖는다.

<br>

* **Bean Validation이 제공하는 애노테이션을 이용해서 커맨드 객체의 값을 검증하는 방법**
  * Bean Validation과 관련된 의존을 설정에 추가한다.
  * 커맨드 객체에 @NotNull, @Digits 등의 애노테이션을 이용해서 검증 규칙을 설정한다.

<br>

가장 먼저 Bean Validation 의존을 추가한다.

```java
dependencies {
    testCompile group: 'junit', name: 'junit', version: '4.12'
    ... 생략
    implementation 'org.hibernate.validator:hibernate-validator:6.1.2.Final'
}
```

<br>

Bean Validation과 프로바이더가 제공하는 애노테이션을 이용해서 값 검증 규칙을 설정해보자.

* **java/config/controller/RegisterRequest.java**

  ```java
  public class RegisterRequest {
  
    @NotBlank
    @Email
    private String email;
    @Size(min = 6)
    private String password;
    @NotEmpty
    private String confirmPassword;
    @NotEmpty
    private String name;
    
    ... 생략
  ```

<br>

Bean Validation 애노테이션을 적용한 커맨드 객체를 검증할 수 있는 OptionalValidatorFactoryBean 클래스를 빈으로 등록하자.

* **java/config/MvcConfig.java**

  ```java
  @Configuration
  @EnableWebMvc		// OptionalValidatorFactoryBean을 글로벌 범위 Validator로 등록
  public class MvcConfig implements WebMvcConfigurer {
    ...
  }
  ```

  * @EnableWebMvc 애노테이션이 알아서 등록해준다.

<br>

@Valid 애노테이션을 붙여서 글로벌 범위  Validator로 검증한다.

* **java/controller/RegisterController.java**

  ```java
  @Controller
  public class RegisterController {
  
    ...
    
    @PostMapping("/register/step3")
    public String handleStep3(@Valid RegisterRequest regReq, Errors errors) {
      if (errors.hasErrors())
        return "register/step2";
      try {
        memberRegisterService.regist(regReq);
        return "register/step3";
      } catch (DuplicateMemberDaoException ex) {
        errors.rejectValue("email", "duplicate");
        return "register/step2";
      }
    }
    
  // 어떤 Validator가 커맨드 객체를 검증할지를 정의한다.
  //  @InitBinder
  //  protected void initBinder(WebDataBinder binder) {
  //    // 컨트롤러 범위에 적용할 Validator 를 설정한다.
  //    binder.setValidator(new RegisterRequestValidator());
  //  }
  
  }
  ```

  * 만약 글로벌 범위 Validator를 따로 설정했다면 해당 애노테이션을 삭제하자.

    ```java
    @Configuration
    @EnableWebMvc
    public class MvcConfig implements WebMvcConfigurer {
    
      // 글로벌 범위 Validator를 설정하면
      // OptionalValidatorFactoryBean를 사용하지 않는다.
      @Override
      public Validator getValidator() {
        return new RegisterRequestValidator();
      }
      
      ...
    ```

<br>

스프링  MVC는 에러 코드에 해당하는 메시지가 존재하지 않을 때 Bean Validation 프로바이더가 제공하는 기본 에러 메시지를 출력한다.

기본 에러 메시지 대신 원하는 에러 메시지를 사용하려면 다음 규칙을 따르는 **메시지 코드를 메시지 프로퍼티 파일에 추가하면 된다.**

* 애노테이션이름.커맨드객체모델명.프로퍼티명
* 애노테이션이름.프로퍼티명
* 애노테이션이름

<br>

값을 검사하는 과정에서 @NotBlank 애노테이션으로 지정한 검사를 통과하지 못할 때 사용하는 메시지 코드

* NotBlank.registerRequest.name
* NotBlank.name
* NotBlank

<br>

**메시지 프로퍼티 파일**

```properties
NotBlank=필수 항목입니다. 공백 문자는 허용하지 않습니다.
NotEmpty=필수 항목입니다.
Size.password=암호 길이는 6자 이상이어야 합니다.
Email=올바른 이메일 주소를 입력해야 합니다.
```

<br>

**실행 결과**

![image](https://user-images.githubusercontent.com/43431081/77288335-e82c0e00-6d1a-11ea-84eb-55bd25112055.png)

<br>

# 5.1. Bean Validation의 주요 애노테이션

* **Bean Validation 1.1의 주요 애노테이션 (javax.validation.constraints)**

  | 애노테이션                    | 주요 속성                                                    | 설명                                                         | 지원 타입                                                  |
  | ----------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ---------------------------------------------------------- |
  | @AssertTrue<br />@AssertFalse | -                                                            | 값이 true 인지 또는 false 인지 검사한다.<br />null은 유효하다고 판단한다. | boolean<br />Boolean                                       |
  | @DecimalMax<br />@DecimalMin  | String value<br />- 최대값 또는 최솟값<br />boolean inclusive<br />- 지정값 포함 여부<br />- 기본값 : true | 지정한 값보다 작거나 같은지 또는 크거나 같은지 검사한다.<br />inclusive가 false면 value로 지정한 값은<br />포함하지 않는다.<br />null은 유효하다고 판단한다. | BigDecimal<br />BigInteger<br />CharSequence<br />정수타입 |
  | @Max<br />@Min                | long value                                                   | 지정한 값보다 작거나 같은지 또는<br />크거나 같은지 검사한다.<br />null은 유효하다고 판단한다. | BigDecimal<br />BigInteger<br />정수타입                   |
  | @Digits                       | int integer<br />- 최대 정수 자릿수<br />int fraction<br />- 최대 소수점 자릿수 | 길이나 크기가 지정한 값 범위에 있는지 검사한다.<br />null은 유효하다고 판단한다. | CharSequence<br />Collection<br />Map<br />배열            |
  | @Size                         | int min<br />- 최소 크기<br />- 기본값 : 0<br />int max<br />- 최대 크기<br />- 기본값 : 정수 최댓값 | 길이나 크기가 지정한 값 범위에<br />있는지 검사한다.<br />null은 유효하다고 판단한다. | CharSequence<br />Collection<br />Map<br />배열            |
  | @Null<br />@NotNull           | -                                                            | 값이 null 인지 또는 null이 아닌지<br />검사한다.             | -                                                          |
  | @Pattern                      | String regexp<br />- 정규표현식                              | 값이 정규표현식에 일치하는지 검사한다.<br />null은 유효하다고 판단한다. | CharSequence                                               |

  * @NotNull을 제외한 나머지 애노테이션은 검사 대상 값이 null인 경우 유효한 것으로 판단한다.

  * 따라서 필수 입력 값을 검사할 때에는 @NotNull과 @Size를 함께 사용해야 한다.

    ```java
    // @NotNull만 사용하면 title의 값이 빈 문자열("")일 경우 값 검사를 통과한다.
    @NotNull
    @Size(min=1)
    private String title;
    ```

<br>

* **Bean Validation 2.0이 추가 제공하는 애노테이션**

  | 애노테이션                     | 설명                                                         | 지원 타입                                       |
  | ------------------------------ | ------------------------------------------------------------ | ----------------------------------------------- |
  | @NotEmpty                      | 문자열이나 배열의 경우 null이 아니고 길이가 0이 아닌지 검사한다.<br />콜렉션의 경우 null이 아니고 크기가 0이 아닌지 검사한다. | CharSequence<br />Collection<br />Map<br />배열 |
  | @NotBlank                      | null이 아니고 최소한 한 개 이상의 공백아닌 문자를 포함하는지 검사한다. | CharSequence                                    |
  | @Positive<br />@PositiveOrZero | 양수인지 검사한다.<br />OrZero가 붙은 것은 0 또는 양수인지 검사한다.<br />null은 유효하다고 판단한다. | BigDecimal<br />BigInteger<br />정수타입        |
  | @Negative<br />@NegativeOrZero | 음수인지 검사한다.<br />OrZero가 붙은 것은 0 또는 음수인지 검사한다.<br />null은 유효하다고 판단한다. | BigDecimal<br />BigInteger<br />정수타입        |
  | @Email                         | 이메일 주소가 유효한지 검사한다.<br />null은 유효하다고 판단한다. | CharSequence                                    |
  | @Future<br />@FutureOrPresent  | 해당 시간이 미래 시간인지 검사한다.<br />OrPresent가 붙은 것은 현재 또는 미래 시간인지 검사한다.<br />null은 유효하다고 판단한다. | 시간 관련 타입                                  |
  | @Past<br />@PastOrPresent      | 해당 시간이 과거 시간인지 검사한다.<br />OrPresent가 붙은 것은 현재 또는 과거 시간인지 검사한다.<br />null은 유효하다고 판단한다. | 시간 관련 타입                                  |