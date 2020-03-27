# Chapter 15. 간단한 웹 어플리케이션의 구조

* 이 장에서 다룰 내용
  * 구성 요소
  * 서비스 구현
  * 패키지 구성

<br>

# 1. 간단한 웹 어플리케이션의 구성 요소

![image](https://user-images.githubusercontent.com/43431081/77642095-7533c900-6fa0-11ea-9cfc-5a93a6dce2f9.png)

* **프론트 서블릿**
  * 웹 브라우저의 모든 요청을 받는 창구 역할을 한다.
  * 요청을 분석해서 알맞은 컨트롤러에 전달한다.
  * Ex) DispatcherServlet
* **컨트롤러 + 뷰**
  * 컨트롤러는 실제 웹 브라우저의 요청을 처리한다.
  * 컨트롤러는 클라이언트(브라우저)의 요청을 처리하기 위해 알맞은 기능을 실행하고 그 결과를 뷰에 전달한다.
  * *컨트롤러의 주요 역할*
    * *클라이언트가 요구한 기능을 실행*
    * *응답 결과를 생성하는데 필요한 모델 생성*
    * *응답 결과를 생성할 뷰 선택*
  * 컨트롤러는 어플리케이션이 제공하는 기능과 사용자 요청을 연결하는 매개체이다.
  * 컨트롤러는 로직을 직접 수행하지 않고 해당 로직을 제공하는 서비스에 그 처리를 위임한다.
* **서비스**
  * 기능의 로직을 구현한다.
  * DB 연동이 필요하면 DAO(Data Access Object)를 사용한다.
* **DAO**
  * DB와 웹 어플리케이션 간에 데이터를 이동시켜 주는 역할을 맡는다.

<br>

# 2. 서비스의 구현

서비스 로직들은 한 번의 과정으로 끝나기보다는 여러 단계의 과정을 거친다.

중간 과정에서 실패가 나면 이전까지 했던 것을 취소해야 하고, 모든 과정을 성공적으로 진행했을 때 완료해야 한다.

이런 이유로 **서비스 메서드를 트랜잰셕 범위에서 실행한다.**

```java
@Transactional
public void changePassword(String email, String oldPwd, String newPwd) {
  Member member = memberDao.selectByEmail(email);
  if (member == null)
    throw new MemberNotFoundException();
  
  member.changePassword(oldPwd, newPwd);
  
  memberDao.update(member);
}
```

* 스프링의 **@Transactional을** 이용해서 트랜잭션 범위에서 비밀번호 변경 기능을 수행한다.

<br>

각 서비스 클래스는 기능 제공을 위해 한 개의 Public 메서드를 제공한다.

같은 데이터를 사용하는 기능들을 한 개의 서비스 클래스에 모아서 구현할 수도 있다.

MemberService라는 클래스를 만들어서 회원과 관련된 모든 기능을 제공하도록 구현할 수 있다.

```java
public class MemberService {
  ...
  
  @Transactional
  public void regist(RegisterRequest reg) { ... }
  
  @Transactional
  public void changePassword(String email, String oldPwd, String newPwd) { ... }
  
}
```

> 보통 기능별로 서비스 클래스를 작성하는 것을 선호한다. 왜냐하면 한 클래스의 코드 길이을 일정 수준 안에서 유지할 수 있기 때문이다.

<br>

서비스 클래스의 메서드는 기능을 실행하는데 **필요한 값을 파라미터로 전달받는다.**

필요한 데이터를 전달받기 위해 별도 타입을 만들면 **스프링 MVC의 커맨드 객체로** 해당 타입을 사용할 수 있어 편한다.

```java
@PostMapping("/register/step3")
public String handleStep3(RegisterRequest regReq, Errors errors) {
  ...
  memberRegisterService,regist(regReq);
  ...
}
```

<br>

웹 요청 파라미터를 커맨드 객체로 받고 커맨드 객체의 프로퍼티를 서비스 메서드에 인자로 전달할 수도 있다.

```java
@RequestMapping(method = RequestMethod.POST)
public String submit(
  @ModelAttribute("command") ChangePwdCommand pwdCmd,
  Errors errors, HttpSession session) {
  ...
  changePasswordService.changePassword(
    authInfo.getEmail(),
    pwdCmd.getCurrentPassword(),
    pwdCmd.getNewPassword());
  ...
}
```

<br>

커맨드 클래스를 작성한 이유는 **스프링 MVC가 제공하는 폼 값 바인딩과 검증, 스프링 폼 태그와의 연동 기능을** 사용하기 위함이다.

서비스 메서드는 기능을 실행한 후에 결과를 알려주어야 한다. 결과는 크게 두 가지 방식이다.

* **리턴 값을 이용한 정상 결과**
* **익셉션을 이용한 비정상 결과**

```java
public class AuthService {
  
  public AuthInfo authenticate(String email, String password) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) {
      // 비정상 결과
      throw new WrongIdPasswordException();
    }
    if (!member.matchPassword(password)) {
      // 비정상 결과
      throw new WrongIdPasswordException();
    }
    // 정상 결과
    return new AuthInfo(member.getId(), member.getEmail(),
                       member.getName());
  }
  
}
```

* authenticate() 메서드는 인증에 성공할 경우 인증 정보를 담고 있는 AuthInfo 객체를 리턴해서 정상적으로 실행되었음을 알려준다.
* 인증 대상 회원이 존재하지 않거나 비밀번호가 일치하지 않는 경우 WrongIdPasswordException을 발생시킨다.

<br>

```java
@RequestMapping(method = RequestMethod.POST)
public String submit(
  LoginCommand loginCommand, Errors errors, HttpSession session,
  HttpServletResponse response) {
  ...
  try {
    AuthInfo authInfo = authService.authenticate(
      loginCommand.getEmail(),
      loginCommand.getPassword());
    
    session.setAttribute("authInfo", authInfo);
    
    ...
      
    return "login/loginSuccess";
  } catch (WrongIdPasswordException e) {
    // 서비스는 기능 실행에 실패할 경우 익셉션을 발생시킨다.
    errors.reject("idPasswordNotMatching");
    return "login/loginForm";
  }
}
```

<br>

# 3. 컨트롤러에서의 DAO 접근

컨트롤러는 서비스를 사용해야 한다는 압박에서 벗어나 다음과 같이 **DAO에 직접 접근해도** 큰 틀에서 웹 어플리케이션의 계층 구조는 유지된다.

```java
@RequestMapping("/member/detail/{id}")
public String detail(@PathVariable("id") Long id, Model model) {
  Member member = memberDao.selectByEmail(id);
  if (member == null) {
    return "member/notFound";
  }
  model.addAttribute("member", member);
  return "member/memberDetail";
}
```

> 자기 나름대로 서비스의 역할과 DAO의 역할을 정의해나가면서 선호하는 방식을 선택한다.

<br>

# 4. 패키지 구성

**영역의 구분**

![image](https://user-images.githubusercontent.com/43431081/77729433-fdb67600-7041-11ea-8221-41045576f532.png)

* 웹 요청을 처리하기 위한 영역에는 컨트롤러 클래스와 관련 클래스들이 위치한다. 커맨드 객체의 값을 검증하기 위한 Validator도 웹 요청 처리 영역에 위치할 수 있다. 관점에 따라 Validator를 기능 제공 영역에 위치시킬 수도 있다.
* 웹 영역의 패키지는 web.member와 같이 영역에 알맞은 패키지 이름을 사용한다.
* 기능 제공 영역에는 기능 제공을 위해 필요한 서비스, DAO, 그리고 Member와 같은 모델 클래스가 위치한다.

<br>

![image](https://user-images.githubusercontent.com/43431081/77729768-d613dd80-7042-11ea-9e1b-057a8c392a8b.png)

* 기능 제공 영역은 다시 service, dao, model과 같은 세부 패키지로 구분한다.

> 패키지를 구성할 때 중요한 점은 팀 구성원 모두가 동일한 규칙에 따라 일관되게 패키지를 구성해야 한다.

<br>

### 웹 어플리케이션이 복잡해지면

웹 어플리케이션이 복잡해지고 커지면서 코드도 함께 복잡해지는 문제를 완화하는 방법 중 하나는 **도메인 주도 설계를 적용하는 것이다.**

도메인 주도 설계는 컨트롤러-서비스-DAO 구조 대신에 UI-서비스-도메인-인프라의 네 영역으로 어플리케이션을 구성한다. UI는 컨트롤러 영역에 대응하고 인프라는 DAO 영역에 대응한다. 중요한 점은 **주요한 도메인 모델과 업무 로직이 서비스 영역이 아닌 도메인 영역에 위치한다는** 것이다. 또한 **도메인 영역은 정해진 패턴에 따라 모델을 구현한다.**