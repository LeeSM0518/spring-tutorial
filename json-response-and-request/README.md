# Chapter 16. JSON 응답과 요청 처리

* 이 장에서 다룰 내용
  * JSON 개요
  * @RestController를 이용한 JSON 응답 처리
  * @RequestBody를 이용한 JSON 요청 처리
  * ResponseEntity

<br>

웹 요청에도 쿼리 문자열 대신에 JSON이나 XML을 데이터로 보내기도 한다. GET이나 POST만 사용하지 않고 PUT, DELETE와 같은 다른 방식도 사용한다.

스프링 MVC를 사용하면 이를 위한 웹 컨트롤러를 쉽게 만들 수 있다.

<br>

# 1. JSON 개요

**JSON(JavaScript Object Notation)은 간단한 형식을 갖는 문자열로 데이터 교환에 주로 사용한다.**

* JSON 형식으로 표현한 데이터 예

  ```json
  {
    "name": "유관순",
    "birthday": "1902-12-16",
    "age": 17,
    "related": ["남동순", "류예도"],
    "edu": [
      {
        "title": "정보통신공학과",
        "year": 1916
      },
      {
        "title": "전자제어공학과",
        "year": 1916
      }
    ]
  }
  ```

  * 중괄호를 사용해서 객체를 표현한다.
  * 객체는 (이름, 값) 쌍을 갖는다.
  * 이때 이름과 같은 콜론(:)으로 구분한다.
    * **값 예시)** 문자열, 숫자, 불리언, null, 배열, 다른 객체
  * 문자열은 큰따옴표나 작음따옴표 사이에 위치한 값이다. 역슬래시를 이용해서 특수 문자를 표시한다.

<br>

# 2. Jackson 의존 설정

**Jackson은** 자바 객체의 JSON 형식 문자열 간 변화를 처리하는 라이브러리이다.

* **gradle 의존 추가**

  ```java
  compile group: 'com.fasterxml.jackson.core', name: 'jackson-databind', version: '2.0.1'
  compile group: 'com.fasterxml.jackson.datatype', name: 'jackson-datatype-jsr310', version: '2.10.3'
  ```

<br>

Jackson은 자바 객체와 JSON 사이의 변환을 처리한다.

![image](https://user-images.githubusercontent.com/43431081/77736477-9c959f00-704f-11ea-8d24-49ed2718e74b.png)

* Jackson은 프로퍼티의 이름과 값을 JSON 객체의 (이름, 값) 쌍으로 사용한다.
* 프로퍼티 타입이 배열이나 List인 경우 JSON 배열로 변환된다.

<br>

# 3. @RestController로 JSON 형식 응답

스프링 MVC에서 JSON 형식으로 데이터를 응답하는 방법은 @Controller 애노테이션 대신 **@RestController 애노테이션을** 사용하면 된다.

* **java/controller/RestMemberController.java**

  ```java
  // @Controller 애노테이션 대신 @RestController 애노테이션 사용
  @RestController
  public class RestMemberController {
  
    private MemberDao memberDao;
    private MemberRegisterService registerService;
  
    @GetMapping("/api/members")
    // 요청 매핑 애노테이션 적용 메서드의 리턴 타입으로
    //  일반 객체 사용
    public List<Member> members() {
      return memberDao.selectAll();
    }
  
    @GetMapping("/api/members/{id}")
    // 요청 매핑 애노테이션 적용 메서드의 리턴 타입으로
    //  일반 객체 사용
    public Member member(@PathVariable Long id,
                         HttpServletResponse response) throws IOException {
      Member member = memberDao.selectById(id);
      if (member == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
      }
      return member;
    }
  
    public void setMemberDao(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
  
    public void setRegisterService(MemberRegisterService registerService) {
      this.registerService = registerService;
    }
  
  }
  ```

  * @RestController 애노테이션을 붙인 경우 스프링 MVC는 요청 매핑 애노테이션을 붙인 메서드가 리턴한 객체를 **알맞은 JSON 형식으로 변환해서 응답 데이터로 전송한다.**

* **java/config/ControllerConfig.java**

  ```java
  @Configuration
  public class ControllerConfig {
  
    @Bean
    public RestMemberController restApi() {
      RestMemberController cont = new RestMemberController();
      cont.setMemberDao(memberDao);
      cont.setRegisterService(memberRegSvc);
      return cont;
    }
    
    ...
      
  }
  ```

* **실행 결과**

  ![image](https://user-images.githubusercontent.com/43431081/77737492-6eb15a00-7051-11ea-806a-10959e48b482.png)

  * 크롬에서 json-formatter 플러그인을 사용하면 JSON 응답을 보기 좋게 표시해준다.

<br>

## 3.1. @JsonIgnore를 이용한 제외 처리

보통 암호와 같이 민감한 데이터는 응답 결과에 포함시키면 안되므로 password 데이터를 응답 결과에서 제외시켜야 한다.

Jackson이 제공하는 **@JsonIgnore 애노테이션을 사용하면** 이를 간단히 처리할 수 있다.

다음과 같이 **JSON 응답에 포함시키지 않을 대상에** @JsonIgnore 애노테이션을 붙인다.

* **java/spring/Member.java**

  ```java
  public class Member {
  
    private Long id;
    private String email;
    @JsonIgnore
    private String password;
    private String name;
    private LocalDateTime registerDateTime;
    
    ...
  ```

<br>

## 3.2. 날짜 형식 변환 처리: @JsonFormat 사용

보통 날짜나 시간은 배열이나 숫자보다는 "2018-03-01 11:07:49" 와 같이 특정 형식을 갖는 문자열로 표현하는 것을 선호한다.

Jackson에서 날짜나 시간 값을 특정한 형식으로 표현하는 가장 쉬운 방법은 **@JsonFormat 애노테이션을** 사용하는 것이다.

* **java/spring/Member.java**

  ```java
  public class Member {
  
    private Long id;
    private String email;
    @JsonIgnore
    private String password;
    private String name;
    // ISO-8601 형식으로 변환
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime registerDateTime;
    
    ...
  ```

* **실행 결과**

  ![image](https://user-images.githubusercontent.com/43431081/77846827-c8518a00-71f3-11ea-96bc-64a224227304.png)

  * ISO-8601 형식이 아닌 원하는 형식으로 변환해서 출력하고 싶다면 pattern 속성을 사용하면 된다.

    ```java
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    ```

<br>

## 3.3. 날짜 형식 변환 처리 : 기본 적용 설정

스프링 MVC는 자바 객체를 HTTP 응답으로 변환할 때 **HttpMessageConvert라는** 것을 사용한다.

Jackson을 이용해서 자바 객체를 JSON으로 변환할 때에는 **MappingJackson2HttpMessageConverter를 새롭게 등록해서** 날짜 형식을 원하는 형식으로 변환하도록 설정하면 모든 날짜 형식에 동일한 변환 규칙을 적용할 수 있다.

<br>

* **java/config/MvcConfig.java**

  ```java
  @Configuration
  // @EnableWebMvc를 통해 스프링 MVC는
  //  여러 형식으로 변환할 수 있는 HttpMessageConverter를 미리 등록한다.
  @EnableWebMvc
  public class MvcConfig implements WebMvcConfigurer {
  
    @Override
    // HttpMessageConverter를 추가로 설정할 때 사용하는 메서드이다.
    // 등록된 HttpMessageConverter 목록을 파라미터로 받는다.
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
      ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
        .json()
        // Jackson이 날짜 형식을 출력할 때 유닉스 타임 스탬프로 출력하는
        //  기능을 비활성화한다.
        // 이 기능을 비활성화하면 ObjectMapper는 날짜 타입의 값을
        //  ISO-8601 형식으로 출력한다.
        .featuresToDisable(
        SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
        .build();
      // 새로 생성한 HttpMessageConverter는 목록의 제일 앞에 위치시켜야 한다.
      converters.add(0,
                     new MappingJackson2HttpMessageConverter(objectMapper));
    }
    ...
  ```

<br>

모든 java.util.Date 타입의 값을 원하는 형식으로 출력하도록 설정하고 싶으면 **Jackson2ObjectMapperBuilder#simpleDateFormat() 메서드를** 이용해서 패턴을 지정한다.

```java
ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
  .json()
  .simpleDateForamt("yyyyMMddHHmmss")
  .build();
converters.add(0,
               new MappingJackson2HttpMessageConverter(objectMapper));
```

* simpleDateForamt()으로 Date 타임을 변환할 때 사용할 패턴을 지정해도 LocalDateTime 타입 변환에는 해당 패턴을 사용하지 않는다.

<br>

LocalDateTime 타입에 대해 ISO-8601 형식 대신 원하는 패턴을 설정하고 싶다면 **scrializerByType() 메서드를 이용해서 LocalDateTime 타입에 대한 JsonSerializer를 직접 설정하면 된다.**

```java
DateTimeFormatter formatter =
  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
  .json()
  .serializerByType(LocalDateTime.class,
                    new LocalDateTimeSerializer(formatter))
  .build();
converters.add(0,
              new MappingJackson2HttpMessageConverter(objectMapper));
```

<br>

## 3.4. 응답 데이터의 컨텐츠 형식

![image](https://user-images.githubusercontent.com/43431081/77848441-97775200-71ff-11ea-9597-24824f088454.png)

* JSON 응답의 Content-Type은 application/json 이다.

<br>

# 4. @RequestBody로 JSON 요청 처리

JSON 형식의 요청 데이터를 자바 객체로 변환하는 기능에 대해 살펴보자.

JSON 형식으로 전송된 요청 데이터를 커맨드 객체로 전달받는 방법은 매우 간단하다.

**커맨드 객체에 @RequestBody 애노테이션을 붙이기만 하면 된다.**

* **java/controller/RestMemberController.java**

  ```java
  @RestController
  public class RestMemberController {
  
    private MemberDao memberDao;
    private MemberRegisterService registerService;
  
    @PostMapping("/api/members")
    public void newMember(
        // @RequestBody 애노테이션을 커맨드 객체에 붙이면
        //  JSON 형식의 문자열을 해당 자바 객체로 변환한다.
        @RequestBody @Valid RegisterRequest regReq,
        HttpServletResponse response) throws IOException {
      try {
        Long newMemberId = registerService.regist(regReq);
        // 응답 헤더에 "Location"을 추가
        //	회원의 아이디를 URL에 담아 응답 결과로 포함시킨다.
        response.setHeader("Location", "/api/members/" + newMemberId);
        response.setStatus(HttpServletResponse.SC_CREATED);
      } catch (DuplicateMemberDaoException dupEx) {
        // 중복된 ID를 전송할 경우 응답 상태 코드로 409(CONFLICT)를 리턴한다.
        response.sendError(HttpServletResponse.SC_CONFLICT);
      }
    }
    
    ...
  ```

* **실행 결과 (Postman App)**

  ![image-20200329211556060](../../Library/Application Support/typora-user-images/image-20200329211556060.png)

  * 응답 코드로 **201(CREATED)가** 전송된 것을 확인할 수 있다.

<br>

## 4.1. JSON 데이터의 날짜 형식 다루기

별도 설정을 하지 않으면 다음 패턴(시간대가 없는 JSR-8601 형식)의 문자열을 LocalDateTime과 Date로 변환한다.

```
yyyy-MM-ddTHH:mm:ss
```

<br>

특정 패턴을 가진 문자열을 LocalDateTime이나 Date 타입으로 변환하고 싶다면 **@JsonFormat 애노테이션의 pattern 속성을 사용해서** 패턴을 지정한다.

```java
@JsonFormat(pattern = "yyyyMMddHHmmss")
private LocalDateTime birthDateTime;

@JsonFormat(pattern = "yyyyMMdd HHmmss")
private Date birthDate;
```

<br>

특정 속성이 아니라 해당 타입을 갖는 모든 속성에 적용하고 싶다면 스프링 MVC 설정을 추가하면 된다,

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
  ... 생략
  
  @Override
  public void extendMessageConverters(
    List<HttpMessageConverter<?>> converters) {
    DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
      .json()
      .featuresToEnable(SerializationFeature.INDENT_OUTPUT)
      .deserializerByType(LocalDateTime.class,
                         new LocalDateTimeDeserializer(formatter))
      .simpleDateFormat("yyyyMMdd HHmmss")
      .build();
    
    converters.add(0,
                  new MappingJackson2HttpMessageConverter(objectMapper));
  }
}
```

* **deserializerByType()는** JSON 데이터를 LocalDateTime 타입으로 변환할 때 사용할 패턴을 지정하고 simpleDateFormat()은 Date 타입으로 변환할 때 사용할 패턴을 지정한다.
* **simpleDateFormat()은** Date 타입을 JSON 데이터로 변환할 때에도 사용된다.

<br>

## 4.2. 요청 객체 검증하기

JSON 형식으로 전송할 데이터를 변환한 객체도 @Valid 애노테이션이나 별도 Validator를 이용해서 검증할 수 있다.

@Valid 애노테이션을 사용한 경우 검증에 실패하면 400(Bad Request) 상태 코드를 응답한다.

* **Validator를 사용할 경우**

  ```java
  @PostMapping("/api/members")
  public void newMember(
    @RequestBody RegisterRequest regReq, Errors errors,
    HttpServletResponse response) throws IOException {
    try {
      new RegisterRequestValidator().validate(regReq, errors);
      if (errors.hasErrors()) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      ...
    } catch (DuplicateMemberException dupEx) {
      response.sendError(HttpServletResponse.SC_CONFLICT);
    }
  }
  ```

<br>

# 5. ResponseEntity로 객체 리턴하고 응답 코드 지정하기

```java
@GetMapping("/api/members/{id}")
public Member member(@PathVariable Long id,
                    HttpServletResponse response) throws IOException {
  Member member = memberDao.selectById(id);
  if (member == null) {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
    return null;
  }
  return member;
}
```

* 위와 같이 HttpServletResponse를 이용해서 404 응답을 하면 **JSON 형식이 아닌** 서버가 기본으로 제공하는 HTML을 응답 결과로 제공한다.
* 404나ㅏ 500과 같이 처리에 실패한 경우 **HTML 응답 데이터 대신에 JSON 형식의 응답 데이터를 전송해야** API 호출 프로그램이 일관된 방법으로 응답을 처리할 수 있다.

<br>

## 5.1. ResponseEntity를 이용한 응답 데이터 처리

정산인 경우와 비정상인 경우 모두 JSON 응답을 전송하는 방법은 **ResponseEntity를 사용하는 것이다.**

먼저 에러 상황일 때 응답으로 사용할 ErrorResponse 클래스를 작성해보자.

* **java/controller/ErrorResponse.java**

  ```java
  public class ErrorResponse {
    
    private String message;
  
    public ErrorResponse(String message) {
      this.message = message;
    }
  
    public String getMessage() {
      return message;
    }
    
  }
  ```

* **java/controller/RestMemberController.java**

  ```java
  @RestController
  public class RestMemberController {
  
    public ResponseEntity<Object> member(@PathVariable Long id,
                                         HttpServletResponse response) throws IOException {
      Member member = memberDao.selectById(id);
      if (member == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        // 404(NOT_FOUND)를 상태 코드로 응답
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
          // member 가 null 이면 ErrorResponse 를 JSON 으로 변환한다.
          .body(new ErrorResponse("no member"));
      }
  
      // 200(OK)을 상태 코드로 응답
      return ResponseEntity.status(HttpStatus.OK)
        // member 객체를 JSON 으로 변환한다.
        .body(member);
    }
  
    ...
  ```

  * 스프링 MVC는 리턴 타입이 ResponseEntity이면 ResponseEntity의 body로 지정한 객체를 사용해서 변환을 처리한다.
  * ResponseEntity의 status로 지정한 값을 응답 상태 코드로 사용한다.

<br>

ResponseEntity를 생성하는 기본 방법은 **status와 body를** 이용해서 **상태코드와 JSON으로** 변환할 객체를 지정하는 것이다.

```java
ResponseEntity.status(상태코드).body(객체)
```

> 상태 코드는 HttpStatus 열거 타입에 정의된 값을 이용해서 정의한다.

<br>

200(OK) 응답 코드와 몸체 데이터를 생성할 경우 **ok() 메서드를** 이용해서 생성할 수도 있다.

```java
ResponseEntity.ok(member)
```

<br>

만약 몸체 내용이 없다면 body를 지정하지 않고 **build()로** 바로 생성한다.

```java
ResponseEntity.status(HttpStatus.NOT_FOUND).build()
```

<br>

* 몸체가 없을 때 status() 대신 사용할 수 있는 메서드
  * **noContent()** : 204
  * **badRequest()** : 400
  * **notFound()** : 404

<br>

* **이전의 상태 코드와 헤더를 함께 전송하던 코드**

  ```java
  response.setHeader("Location", "/api/members/" + newMemberId);
  response.setStatus(HttpServletResponse.SC_CREATED);
  ```

* **ResponseEntity로 구현한 코드**

  ```java
  @PostMapping("/api/members")
  public ResponseEntity<Object> newMember(
    @RequestBody @Valid RegisterRequest regReq) throws IOException {
    try {
      Long newMemberId = registerService.regist(regReq);
      URI uri = URI.create("/api/members/" + newMemberId);
      return ResponseEntity.created(uri).build();
    } catch (DuplicateMemberDaoException dupEx) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }
  ```

  * Location 헤더로 전달할 URI를 전달하면 된다.

<br>

## 5.2. @ExceptionHandler 적용 메서드에서 ResponseEntity로 응답하기

@ExceptionHandler 애노테이션을 적용한 메서드에서 에러 응답을 처리하도록 구현하면 코드의 중복을 없앨 수 있다.

```java
@GetMapping("/api/members/{id}")
public Member member(@PathVariable Long id,
                     HttpServletResponse response) throws IOException {
  Member member = memberDao.selectById(id);
  if (member == null) {
    throw new MemberNotFoundException();
  }
  return member;
}

@ExceptionHandler(MemberNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNoData() {
  return ResponseEntity
    .status(HttpStatus.NOT_FOUND)
    .body(new ErrorResponse("no member"));
}
```

<br>

@RestControllerAdvice 애노테이션을 이용해서 에러 처리 코드를 별도 클래스로 분리할 수도 있다.

이 애노테이션은 응답을 JSON이나 XML과 같은 형식으로 변환한다.

* **java/controller/ApiExceptionAdvice.java**

  ```java
  @RestControllerAdvice("controller")
  public class ApiExceptionAdvice {
  
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoData() {
      return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("no member"));
    }
  
  }
  ```

  * 이 애노테이션을 사용하면 에러 처리 코드가 한 곳에 모여 효과적으로 에러 응답을 관리할 수 있다.

<br>

## 5.3. @Valid 에러 결과를 JSON으로 응답하기

@Valid 애노테이션을 붙인 커맨드 객체가 값 검증에 실패하면 400 상태 코드를 응답한다.

문제는 커맨드 값 검증이 실패해서 실패 코드를 응답할 때 **HTML 응답으로 전송한다는 점이다.**

@Valid 애노테이션을 이용한 검증에 실패했을 때 HTML 응답 데이터 대신에  JSON 형식 응답을 제공하고 싶다면 **Errors 타입 파라미터를 추가해서 직접 에러 응답을 생성하면 된다.**

```java
@PostMapping("/api/members")
public ResponseEntity<Object> newMember(
  @RequestBody @Valid RegisterRequest regReq,
  Errors errors) {
  if (errors.hasErrors()) {
    String errorCodes = errors.getAllErrors()  // List<ObjectError>
      .stream()
      .map(error -> error.getCodes()[0])       // error는 ObjectError
      .collect(Collectors.joining(","));
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(new ErrorResponse("errorCodes = " + errorCodes));
  }
  ... 생략
}
```

* hasErrors() 메서드를 이용해서 검증 에러가 존재하는지 확인한다.
* 검증 에러가 존재하면 getAllErrors() 메서드로 모든 에러 정보를 구한다.
* 각 에러의 코드 값을 연결한 문자열을 생성해서 errorCodes 변수에 할당한다.

<br>

@RequestBody 애노테이션을 붙인 경우 @Valid 애노테이션을 붙인 객체의 검증에 실패했을 때 Errors 타입 파라미터가 존재하지 않으면 **MethodArgumentNotValidException이** 발생한다.

@ExceptionHandler 애노테이션을 이용해서 검증 실패시 에러 응답을 생성해도 된다.

```java
@RestControllerAdvice("controller")
public class ApiExceptionAdvice {
  
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleBindException(
    MethodArgumentNotValidException ex) {
    String errorCodes = ex.getBindingResult().getAllErrors()
      .stream
      .map(error -> error.getCodes()[0])
      .collect(Collectors.joining(","));
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(new ErrorResponse("errorCodes = " + errorCodes));
  }
  
}
```

