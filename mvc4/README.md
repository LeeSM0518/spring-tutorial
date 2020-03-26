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
      LocalDateTime from, LocalDateTime to) {
      List<Member> results = jdbcTemplate.query(
        "select from MEMBER where REGDATE between ? and ? " +
        "order by REGDATE desc",
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
        },
        from, to);
      return results;
    }
  
  }
  ```

  * REGDATE 값이 두 파라미터로 전달받은 from과 to 사이에 있는 Member 목록을 구한다.

<br>

# 3. 커맨드 객체 Date 타입 프로퍼티 변환 처리: @DateTimeFormat

검색 기준 시간을 표현하기 위해 커맨드 클래스를 구현해보자.

* **java/controller/ListCommand.java**

  ```java
  public class ListCommand {
  
    private LocalDateTime from;
    private LocalDateTime to;
  
    public LocalDateTime getFrom() {
      return from;
    }
  
    public void setFrom(LocalDateTime from) {
      this.from = from;
    }
  
    public LocalDateTime getTo() {
      return to;
    }
  
    public void setTo(LocalDateTime to) {
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
  public class ListCommand {
  
    @DateTimeFormat(pattern = "yyyyMMddHH")
    private LocalDateTime from;
    @DateTimeFormat(pattern = "yyyyMMddHH")
    private LocalDateTime to;
    
    ...
  }
  ```

  * "2018030115"의 문자열을 "2018년 3월 1일 15시" 값을 갖는 LocalDateTime 객체로 변환해준다.

* **java/controller/MemberListController.java**

  ```java
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
  
  ```

  