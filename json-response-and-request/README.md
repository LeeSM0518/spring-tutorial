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
  // @Controller 애노테이션 대신 @RestController 애노테이션 사
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

## 3.1. @JsonIgnore를 이용한 제외 처리ㄷ

