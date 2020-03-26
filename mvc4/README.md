# Chapter 14. MVC 4 : 날짜 값 변환, @PathVariable, 익셉션 처리

* 이 장에서 다룰 내용
  * @DataTimeFormat
  * @PathVariabler
  * 익셉션 처리

<br>

# 1. 프로젝트 준비

13장에서 작성한 예제를 이어서 사용한다.

<br>

# 2. 날짜를 이용한 회원 검색 기능

회원 가입 일자를 기준으로 검색하는 기능을 구현해보자.

* **java/spring/MemberDao.java**

  ```java
  public class MemberDao {
  
    private JdbcTemplate jdbcTemplate;
  
    public MemberDao(DataSource dataSource) {
      this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
  
    ...
  
    public List<Member> selectByRegdate(
      Date from, Date to) {
      return jdbcTemplate.query(
        "select * from MEMBER where REGDATE between ? and ? " +
        "order by REGDATE desc",
        (rs, rowNum) -> {
          Member member = new Member(
            rs.getString("EMAIL"),
            rs.getString("PASSWORD"),
            rs.getString("NAME"),
            rs.getTimestamp("REGDATE").toLocalDateTime());
          member.setId(rs.getLong("ID"));
          return member;
        },
        from, to);
    }
  
  }
  ```
  
  * REGDATE 값이 두 파라미터로 전달받은 from과 to 사이에 있는 Member 목록을 구한다.

<br>

# 3. 커맨드 객체 Date 타입 프로퍼티 변환 처리: @DateTimeFormat

검색 기준 시간을 표현하기 위해 커맨드 클래스를 구현해보자.

* **java/controller/ListCommand.java**

  ```java
  import java.util.Date;
  
  public class ListCommand {
  
    private Date from;
    private Date to;
  
    public Date getFrom() {
      return from;
    }
  
    public void setFrom(Date from) {
      this.from = from;
    }
  
    public Date getTo() {
      return to;
    }
  
    public void setTo(Date to) {
      this.to = to;
    }
  
  }
  ```

<br>

검색을 위한 입력 폼은 다음처럼 이름이 from과 to인 \<input> 태그를 정의한다.

```jsp
<input type="text" name="from"/>
<input type="text" name="to"/>
```

* 여기서 문제는 \<input>에 입력한 문자열을 LocalDateTime 타입으로 변환해야 한다는 것이다.

<br>

스프링은 Long이나 int와 같은 기본 데이터 타입으로의 변환은 기본적으로 처리해주지만 **LocalDateTime 타입으로의 변환은 추가 설정이 필요하다.**

앞서 작성한 ListCommand 클래스의 두 필드에 **@DateTimeFormat 애노테이션을 적용하면 된다.**

* **java/controller/ListCommand.java**

  ```java
  import java.util.Date;
  
  public class ListCommand {
  
    @DateTimeFormat(pattern = "yyyyMMddHH")
    private Date from;
    @DateTimeFormat(pattern = "yyyyMMddHH")
    private Date to;
    
    ...
      
  }
  ```

  * "2018030115"의 문자열을 "2018년 3월 1일 15시" 값을 갖는 LocalDateTime 객체로 변환해준다.

* **java/controller/MemberListController.java**

  ```java
  import java.util.List;
  
  @Controller
  public class MemberListController {
  
    private MemberDao memberDao;
  
    public void setMemberDao(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
  
    @RequestMapping("/members")
    public String list(
      @ModelAttribute("cmd") ListCommand listCommand,
      Model model) {
      if (listCommand.getFrom() != null && listCommand.getTo() != null) {
        List<Member> members = memberDao.selectByRegdate(
          listCommand.getFrom(), listCommand.getTo());
        model.addAttribute("members", members);
      }
      return "member/memberList";
    }
  
  }
  ```

* **java/config/ControllerConfig.java**

  ```java
  @Configuration
  public class ControllerConfig {
  
    @Autowired
    private MemberDao memberDao;
    
    @Bean
    public MemberListController memberListController() {
      MemberListController memberListController 
          = new MemberListController();
      memberListController.setMemberDao(memberDao);
      return memberListController;
    }
    
    ...
      
  }
  ```

<br>

뷰 코드를 작성해보자. 먼저 LocalDateTime 값을 원하는 형식으로 출력해주는 커스텀 태그 파일을 작성하자.

* **webapp/WEB-INF/tags/formatDateTime.tag**

  ```jsp
  <%@ tag body-content="empty" pageEncoding="utf-8" %>
  <%@ tag import="java.time.format.DateTimeFormatter" %>
  <%@ attribute name="value" required="true" type="java.time.temporal.TemporalAccessor" %>
  <%@ attribute name="pattern" type="java.lang.String" %>
  <%
  if (pattern == null) pattern = "yyyy-MM-dd";
  %>
  <%= DateTimeFormatter.ofPattern(pattern).format(value) %>
  ```

<br>

뷰 코드에서 ListCommand 객체를 위한 폼을 제공하고 members 속성을 이용해서 회원 목록을 출력하도록 구현하면 된다.

* **webapp/WEB-INF/view/member/memberList.jsp**

  ```jsp
  <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tf" tagdir="/WEB-INF/tags" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title>회원 조회</title>
    </head>
    <body>
      <form:form modelAttribute="cmd">
        <p>
          <%-- <form:input> 태그를 이용해서 --%>
          <%-- 커맨드 객체의 from 프로퍼티와 to 프로퍼티를 위한 <input> 태그 생성--%>
          <label>from: <form:input path="from"/></label>
          ~
          <label>to:<form:input path="to"/></label>
          <input type="submit" value="조회">
          <%-- from과 to는 @DateTimeFormat 애노테이션에 설정한 패턴인 
    "yyyyMMddHH" 패턴에 맞춰서 값을 입력해줘야 한다.--%>
        </p>
      </form:form>
  
      <c:if test="${! empty members}">
        <table>
          <tr>
            <th>아이디</th>
            <th>이메일</th>
            <th>이름</th>
            <th>가입일</th>
          </tr>
          <c:forEach var="mem" items="${members}">
            <tr>
              <td>${mem.id}</td>
              <td><a href="<c:url value="/members/${mem.id}"/>">
                ${mem.email}
                </a></td>
              <td>${mem.name}</td>
              <td><tf:formatDateTime value="${mem.registerDateTime}" pattern="yyyy-MM-dd"/></td>
            </tr>
          </c:forEach>
        </table>
      </c:if>
    </body>
  </html>
  ```
  

<br>

## 3.1. 변환 에러 처리

form이나 to에 원래 지정한 형식인 "yyyyMMddHH"에 맞지 않는 값을 입력 후 조회를 실행하면 **400 에러가 발생한다.**

400 에러 대신 폼에 알맞은 에러 메시지를 보여주고 싶다면 **Errors 타입 파라미터를 요청 매핑 애노테이션 적용 메서드에 추가하면 된다(Error 타입 파라미터를 listCommand 파라미터 바로 뒤에 위치시킨 것에 유의).**

* **java/controller/MemberListController.java**

  ```java
  @Controller
  public class MemberListController {
  
    private MemberDao memberDao;
  
    public void setMemberDao(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
  
    @RequestMapping("/members")
    public String list(
      @ModelAttribute("cmd") ListCommand listCommand,
      Errors errors, Model model) {
      if (errors.hasErrors()) {
        return "member/memberList";
      }
      if (listCommand.getFrom() != null && listCommand.getTo() != null) {
        List<Member> members = memberDao.selectByRegdate(
          listCommand.getFrom(), listCommand.getTo());
        model.addAttribute("members", members);
      }
      return "member/memberList";
    }
  
  }
  ```

  * 요청 매핑 애노테이션 적용 메서드가 Errors 타입 파라미터를 가질 경우 **@DateTimeFormat에 지정한 형식에 맞지 않으면 Errors 객체에 "typeMismatch" 에러 코드를 추가한다.**

<br>

에러 코드로 "typeMismatch"를 추가하므로 **메시지 프로퍼티 파일에 해당 메시지를 추가하면** 에러 메시지를 보여줄 수 있다.

* **resources/message/label.properties**

  ```properties
  ...
  
  typeMismatch.java.util.Date=잘못된 형식
  ```

<br>

\<form:errors> 태그를 사용해서 에러 메시지를 출력하는 코드를 추가한다.

* **webapp/WEB-INF/view/member/memberList.jsp**

  ```jsp
  <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <%@ taglib prefix="tf" tagdir="/WEB-INF/tags" %>
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
  <head>
      <title>회원 조회</title>
  </head>
  <body>
  <form:form modelAttribute="cmd">
      <p>
          <label>from: <form:input path="from"/></label>
          <form:errors path="from"/>
          ~
          <label>to:<form:input path="to"/></label>
          <form:errors path="to"/>
          <input type="submit" value="조회">
      </p>
  </form:form>
    ... 생략
  ```

<br>

# 4. 변환 처리에 대한 이해

@DateTimeFormat 애노테이션을 사용하면 **WebDataBinder** 가 문자열로 입력된 값을 LocalDateTime 타입으로 변환해준다. 

스프링 MVC는 요청 매핑 애노테이션 적용 메서드와 DispatcherServlet 사이를 연결하기 위해 RequestMappingHandlerAdapter 객체를 사용한다. 이 핸들러 어댑터 객체는 요청 파라미터와 커맨드 객체 사이의 변환 처리를 위해 WebDataBinder를 이용한다.

WebDataBinder는 커맨드 객체를 생성한다. 그리고 커맨드 객체의 프로퍼티와 같은 이름을 갖는 요청 파라미터를 이용해서 프로퍼티 값을 생성한다.

* **WebDataBinder가 커맨드 객체의 프로퍼티를 초기화한다.**

  ![image](https://user-images.githubusercontent.com/43431081/77627862-e5375480-6f8a-11ea-8891-523e0ef88dea.png)

  * WebDataBinder는 직접 타입을 변환하지 않고 ConversionService에 그 역할을 위임한다.

<br>

WebDataBinder는 \<form:input> 에도 사용된다. \<form:input> 태그를 사용하면 path 속성에 지정한 프로퍼티 값을 String으로 변환해서 \<input> 태그의 value 속성값으로 생성한다.

* **WebDataBinder의 ConversionService를 사용해서 프로퍼티 값을 String으로 변환**

  ![image](https://user-images.githubusercontent.com/43431081/77628565-90480e00-6f8b-11ea-9e36-869e3499ff1c.png)

<br>

# 5. MemberDao 클래스 중복 코드 정리 및 메서드 추가

MemberDao 코드를 보면 다음과 같이 RowMapper 객체를 생성하는 부분의 코드가 중복되어 있다.

```java
public Member selectByEmail(String email) {
  List<Member> results = jdbcTemplate.query(
    "select * from MEMBER where EMAIL = ?",
    new RowMapper<Member>() {
      @Override
      public Member mapRow(ResultSet rs, int rowNum)
        throws SQLException {
        Member member = new Member(
          rs.getString("EMAIL"),
          rs.getString("PASSWORD"),
          rs.getString("NAME"),
          rs.getTimestamp("REGDATE").toLocalDateTime());
        member.setId(rs.getLong("ID"));
        return member;
      }
    }, email);

  return results.isEmpty() ? null : results.get(0);
}

public List<Member> selectAll() {
  List<Member> results = jdbcTemplate.query(
    "select * from MEMBER",
    new RowMapper<Member>() {
      @Override
      public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        Member member = new Member(
          rs.getString("EMAIL"),
          rs.getString("PASSWORD"),
          rs.getString("NAME"),
          rs.getTimestamp("REGDATE").toLocalDateTime());
        member.setId(rs.getLong("ID"));
        return member;
      }
    });
  return results;
}
```

RowMapper를 생성하는 코드의 중복을 제거하기 위해 임의 객체를 필드에 할당하고 그 필드를 사용하도록 수정하자.

<br>

* **java/spring/rowmapper/MemberRowMapper.java**

  ```java
  public class MemberRowMapper implements RowMapper<Member> {
    @Override
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
      Member member = new Member(
        rs.getString("EMAIL"),
        rs.getString("PASSWORD"),
        rs.getString("NAME"),
        rs.getTimestamp("REGDATE").toLocalDateTime());
      member.setId(rs.getLong("ID"));
      return member;
    }
  }
  ```

* **java/spring/MemberDao.java**

  ```java
  public class MemberDao {
  
    ...
  
    public Member selectByEmail(String email) {
      List<Member> results = jdbcTemplate.query(
        "select * from MEMBER where EMAIL = ?",
        rowMapper
        , email);
      return results.isEmpty() ? null : results.get(0);
    }
  
  
    public List<Member> selectAll() {
      return jdbcTemplate.query("select * from MEMBER",
                                rowMapper);
    }
  
    public List<Member> selectByRegdate(
      Date from, Date to) {
      return jdbcTemplate.query(
        "select * from MEMBER where REGDATE between ? and ? " +
        "order by REGDATE desc",
        rowMapper, from, to);
    }
  
  }
  ```

<br>

# 6. @PathVariable을 이용한 경로 변수 처리

다음은 ID가 10인 회원의 정보를 조회하기 위한 URL이다.

```j
http://localhost:8080/members/10
```

* 경로의 일부가 고정되어 있지 않고 달라질 때 사용할 수 있는 것이 **@PathVariable 애노테이션이다.**
* **@PathVariable** 애노테이션을 사용하면 **가변 경로를 처리할 수 있다.**

<br>

* **java/controller/MemberDetailController.java**

  ```java
  @Controller
  public class MemberDetailController {
  
    private MemberDao memberDao;
  
    public void setMemberDao(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
  
    @GetMapping("/members/{id}")
    public String detail(@PathVariable("id") Long memId, Model model) {
      Member member = memberDao.selectById(memId);
      if (member == null) {
        throw new MemberNotFoundException();
      }
      model.addAttribute("member", member);
      return "member/memberDetail";
    }
  
  }
  ```

  * 매핑 경로에 **"{경로변수}"** 와 같이 중괄호로 둘러 쌓인 부분을 경로 변수라고 부른다.
  * "{경로변수}"에 해당하는 값은 같은 경로 변수이름을 지정한 **@PathVariable 파라미터에 전달된다.**

<br>

ControllerConfig 설정 클래스에 빈으로 등록한다.

* **java/config/ControllerConfig.java**

  ```java
  @Configuration
  public class ControllerConfig {
  
    @Autowired
    private MemberDao memberDao;
    
    @Bean
    public MemberDetailController memberDetailController() {
      MemberDetailController controller = new MemberDetailController();
      controller.setMemberDao(memberDao);
      return controller;
    }
    
    ...
  
  }
  ```

<br>

JSP 파일을 작성해보자.

* **webapp/WEB-INF/view/member/memberDetail.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <%@ taglib prefix="tf" tagdir="/WEB-INF/tags" %>
  <html>
    <head>
      <title>회원 정보</title>
    </head>
    <body>
      <p>아이디: ${member.id}</p>
      <p>이메일: ${member.email}</p>
      <p>이름: ${member.name}</p>
      <p>가입일: <tf:formatDateTime value="${member.registerDateTime}" pattern="yyyy-MM-dd HH:mm"/> </p>
    </body>
  </html>
  ```

<br>

# 7. 컨트롤러 익셉션 처리하기

없는 ID를 경로변수로 사용하면 **MemberNotFoundException이 발생한다.**

MemberDetailController가 사용하는 경로 변수는 Long 타입인데 실제 요청 경로에 숫자가 아닌 문자를 입력하면 **400 에러가 발생한다.**

익셉션 화면이 보이는 것보다 알맞게 익셉션 처리해서 사용자에게 더 적합한 안내를 해 주는 것이 좋다.

MemberNotFoundException은 **try-catch로 잡은 뒤 안내 화면을 보여주는 뷰를 보여주면 된다.**

타입 변환 실패에 따른 익셉션은 **@ExceptionHandler 애노테이션을** 사용해서 처리하는 것이 좋다.

<br>

같은 컨트롤러에 @ExceptionHandler 애노테이션을 적용한 메서드가 존재하면 그 메서드가 익셉션을 처리한다.

따라서 **컨트롤러에서 발생한 익셉션을 직접 처리하고 싶다면 @ExceptionHandler 애노테이션을 적용한 메서드를 구현하면 된다.**

<br>

* **java/controller/MemberDetailController.java**

  ```java
  @Controller
  public class MemberDetailController {
  
    ...
    
    @ExceptionHandler(TypeMismatchException.class)
    public String handleTypeMismatchException() {
      return "member/invalidId";
    }
    
    @ExceptionHandler(MemberNotFoundException.class)
    public String handleMemberNotFoundException() {
      return "member/noMember";
    }
  
  }
  ```

* **webapp/WEB-INF/view/member/invalidId.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title>에러</title>
    </head>
    <body>
      잘못된 요청입니다.
    </body>
  </html>
  ```

* **webapp/WEB-INF/view/member/noMember.jsp**

  ```jsp
  <%@ page contentType="text/html;charset=UTF-8" language="java" %>
  <html>
    <head>
      <title>에러</title>
    </head>
    <body>
      존재하지 않는 회원입니다.
    </body>
  </html>
  ```

<br>

익셉션 객체에 대한 정보를 알고 싶다면 **메서드의 파라미터로 익셉션 객체를 전달받아** 사용하면 된다.

```java
@ExceptionHandler(TypeMismatchException.class)
public String handleTypeMismatchException(TypeMismatchException ex) {
  // ex를 사용해서 로그 남기는 등 작업
  return "member/invalidId";
}
```

<br>

## 7.1. @ControllerAdvice를 이용한 공통 익셉션 처리

여러 컨트롤러에서 동일하게 처리할 익셉션이 발생하면 **@ControllerAdvice 애노테이션을 이용해서 중복을 없앨 수 있다.**

```java
@ControllerAdvice("spring")
public class CommonExceptionHandler {
  
  @ExceptionHandler(RuntimeException.class)
  public String handleRuntimeException() {
    return "error/commonException";
  }
  
}
```

* **"spring"** : "spring" 패키지와 그 하위 패키지에 속한 컨트롤러에서 RuntimeException이 발생하면 메서드가 호출되도록 범위 지정
* @ControllerAdvice 적용 클래스가 동작하려면 해당 클래스를 스프링에 **빈으로 등록해야 한다.**

<br>

## 7.2. @ExceptionHandler 적용 메서드의 우선 순위

@ControllerAdvice 클래스에 있는 @ExceptionHandler 메서드와 컨트롤러 클래스에 있는 @ExceptionHandler 메서드 중 **컨트롤러 클래스에 적용된 @ExceptionHandler 메서드가 우선한다.**

즉 컨트롤러의 메서드를 실행하는 과정에서 익셉션이 발생하면 다음의 순서로 익셉션을 처리할 @ExceptionHandler 메서드를 찾는다.

* *같은 컨트롤러에 위치한 @ExceptionHandler 메서드 중 해당 익셉션을 처리할 수 있는 메서드를 검색*
* *같은 클래스에 위치한 메서드가 익셉션을 처리할 수 없을 경우 @ControllerAdvice 클래스에 위치한 @ExceptionHandler 메서드를 검색*

<br>

@ControllerAdvice 애노테이션은 공통 설정을 적용할 컨트롤러 대상을 지정하기 위해 여러 속성을 제공한다.

* **속성들**

  | 속성                    | 타입                            | 설명                                                  |
  | ----------------------- | ------------------------------- | ----------------------------------------------------- |
  | value<br />basePackages | String[ ]                       | 공통 설정을 적용할 컨트롤러가<br />속하는 기준 패키지 |
  | annotations             | Class\<? Extends Annotation>[ ] | 특정 애노테이션이 적용된<br />컨트롤러 대상           |
  | assignableTypes         | Class\<?>[ ]                    | 특정 타입 또는 그 하위 타입인<br />컨트롤러 대상      |

<br>

## 7.3. @ExceptionHandler 애노테이션 적용 메서드의 파라미터와 리턴 타입

* **@ExceptionHandler 애노테이션을 붙인 메서드는 다음 파라미터를 가질 수 있다.**
  * HttpServletRequest, HttpServletResponse, HttpSession
  * Model
  * 익셉션
* **리턴 가능한 타입**
  * ModelAndView
  * String (뷰 이름)
  * (@ResponseBody 애노테이션을 붙인 경우) 임의 객체
  * ResponseEntity