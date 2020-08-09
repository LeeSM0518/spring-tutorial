# 웹 계층 개발

**목차**

* 홈 화면과 레이아웃
* 회원 등록
* 회원 목록 조회
* 상품 등록
* 상품 목록
* 상품 수정
* 변경 감지와 병합(merge)
* 상품 주문
* 주문 목록 검색, 취소

<br>

## 홈 화면과 레이아웃

### 홈 컨트롤러 등록

```java
package japbook.jpashop.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j      // Logger log = LoggerFactory.getLogger(getClass()); 이 코드와 같다.
public class HomeController {

  @RequestMapping("/")
  public String home() {
    log.info("home controller");
    return "home"; // home.html을 반환
  }

}

```

<br>

### 스프링 부트 타임리프 기본 설정

```yaml
spring:
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
```

<br>

### 타임리프 템플릿 등록

* **home.html**

  ```html
  <!DOCTYPE HTML>
  <html xmlns:th="http://www.thymeleaf.org">
    <!-- fragment: import와 같다.-->
    <head th:replace="fragments/header :: header">
      <title>Hello</title>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    </head>
    <body>
      <div class="container">
        <div th:replace="fragments/bodyHeader :: bodyHeader"/>
        <div class="jumbotron"><h1>HELLO SHOP</h1>
          <p class="lead">회원 기능</p>
          <p>
            <a class="btn btn-lg btn-secondary" href="/members/new">회원 가입</a>
            <a class="btn btn-lg btn-secondary" href="/members">회원 목록</a></p>
          <p class="lead">상품 기능</p>
          <p>
            <a class="btn btn-lg btn-dark" href="/items/new">상품 등록</a>
            <a class="btn btn-lg btn-dark" href="/items">상품 목록</a></p>
          <p class="lead">주문 기능</p>
          <p>
            <a class="btn btn-lg btn-info" href="/order">상품 주문</a>
            <a class="btn btn-lg btn-info" href="/orders">주문 내역</a></p>
        </div>
        <div th:replace="fragments/footer :: footer"/>
      </div> <!-- /container -->
    </body>
  </html>
  ```

* **fragments/header**

  ```html
  <!DOCTYPE html>
  <html xmlns:th="http://www.thymeleaf.org">
    <head th:fragment="header">
      <!-- Required meta tags -->
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1, shrink- to-fit=no">
      <!-- Bootstrap CSS -->
      <link rel="stylesheet" href="/css/bootstrap.min.css" integrity="sha384- ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
      <!-- Custom styles for this template -->
      <link href="/css/jumbotron-narrow.css" rel="stylesheet"> <title>Hello, world!</title>
    </head>
  ```

* **fragments/bodyHeader**

  ```html
  <!DOCTYPE html>
  <html xmlns:th="http://www.thymeleaf.org">
    <div class="header" th:fragment="bodyHeader">
      <ul class="nav nav-pills pull-right">
        <li><a href="/">Home</a></li>
      </ul>
      <a href="/"><h3 class="text-muted">HELLO SHOP</h3></a></div>
  ```

* **fragments/footer**

  ```html
  <!DOCTYPE html>
  <html xmlns:th="http://www.thymeleaf.org">
  <div class="footer" th:fragment="footer">
      <p>&copy; Hello Shop V2</p>
  </div>
  ```

<br>

### view 리소스 등록

예쁜 디자인을 위해 부트스트랩을 사용한다. (https://getbootstram.com)

* `resources/static` 하위에 `css` , `js` 추가
* `resources/static/css/jumbotron-narrow.css` 추가

<br>

### jumbotron-narrow.css 파일

```css
/* Space out content a bit */
body {
    padding-top: 20px;
    padding-bottom: 20px;
}

/* Everything but the jumbotron gets side spacing for mobile first views */
.header, .marketing, .footer {
    padding-left: 15px;
    padding-right: 15px;
}

/* Custom page header */
.header {
    border-bottom: 1px solid #e5e5e5;
}

/* Make the masthead heading the same height as the navigation */
.header h3 {
    margin-top: 0;
    margin-bottom: 0;
    line-height: 40px;
    padding-bottom: 19px;
}

/* Custom page footer */
.footer {
    padding-top: 19px;
    color: #777;
    border-top: 1px solid #e5e5e5;
}

/* Customize container */
@media (min-width: 768px) {
    .container {
        max-width: 730px;
    }
}

.container-narrow > hr {
    margin: 30px 0;
}

/* Main marketing message and sign up button */
.jumbotron {
    text-align: center;
    border-bottom: 1px solid #e5e5e5;
}

.jumbotron .btn {
    font-size: 21px;
    padding: 14px 24px;
}

/* Supporting marketing content */
.marketing {
    margin: 40px 0;
}

.marketing p + h4 {
    margin-top: 28px;
}

/* Responsive: Portrait tablets and up */
@media screen and (min-width: 768px) {
    /* Remove the padding we set earlier */
    .header,
    .marketing,
    .footer {
        padding-left: 0;
        padding-right: 0;
    }

    /* Space out the masthead */
    .header {
        margin-bottom: 30px;
    }

    /* Remove the bottom border on the jumbotron for visual effect */
    .jumbotron {
        border-bottom: 0;
    }
}
```

<br>

## 회원 등록

**회원 가입 DTO**

```java
package japbook.jpashop.controller;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class MemberForm {

  @NotEmpty(message = "회원 이름은 필수 입니다.")
  private String name;

  private String city;
  private String street;
  private String zipcode;

}

```

<br>

 **회원 등록 컨트롤러**

```java
package japbook.jpashop.controller;

import japbook.jpashop.domain.Member;
import japbook.jpashop.domain.item.Address;
import japbook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  @GetMapping("/members/new")
  public String createForm(Model model) {
    model.addAttribute("memberForm", new MemberForm());
    return "members/createMemberForm";
  }

  @PostMapping("/members/new")
  public String create(@Valid MemberForm form, BindingResult result) {

    if (result.hasErrors()) {
      return "members/createMemberForm";
    }

    Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

    Member member = new Member();
    member.setName(form.getName());
    member.setAddress(address);

    memberService.join(member);
    return "redirect:/";
  }

}

```

<br>

**회원 등록 폼 화면(templates/members/createMemberForm.html)**

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="fragments/header :: header"></head>
<style>
    .fieldError {
        border-color: #bd2130;
    } </style>
<body>
<div class="container">
    <div th:replace="fragments/bodyHeader :: bodyHeader"></div>

    <form role="form" action="/members/new" th:object="${memberForm}"
          method="post">
        <div class="form-group">
            <label th:for="name">이름</label>
            <input type="text" th:field="*{name}" class="form-control" placeholder="이름을 입력하세요"
                   th:class="${#fields.hasErrors('name')}? 'form-control fieldError' : 'form-control'">
            <p th:if="${#fields.hasErrors('name')}" th:errors="*{name}">Incorrect date</p>
        </div>
        <div class="form-group">
            <label th:for="city">도시</label>
            <input type="text" th:field="*{city}" class="form-control" placeholder="도시를 입력하세요">
        </div>
        <div class="form-group">
            <label th:for="street">거리</label>
            <input type="text" th:field="*{street}" class="form-control"
                   placeholder="거리를 입력하세요"></div>
        <div class="form-group">
            <label th:for="zipcode">우편번호</label>
            <input type="text" th:field="*{zipcode}" class="form-control" placeholder="우편번호를 입력하세요">
        </div>
        <button type="submit" class="btn btn-primary">Submit</button>
    </form>

    <br/>
    <div th:replace="fragments/footer :: footer"></div>
</div> <!-- /container -->
</body>
</html>

```

<br>

## 회원 목록 조회

```java
package japbook.jpashop.controller;

import japbook.jpashop.domain.Member;
import japbook.jpashop.domain.item.Address;
import japbook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  @GetMapping("/members/new")
  public String createForm(Model model) {
    model.addAttribute("memberForm", new MemberForm());
    return "members/createMemberForm";
  }

  @PostMapping("/members/new")
  public String create(@Valid MemberForm form, BindingResult result) {

    if (result.hasErrors()) {
      return "members/createMemberForm";
    }

    Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

    Member member = new Member();
    member.setName(form.getName());
    member.setAddress(address);

    memberService.join(member);
    return "redirect:/";
  }

  @GetMapping("/members")
  public String list(Model model) {
    List<Member> members = memberService.findMembers();
    model.addAttribute("members", members);
    return "members/memberList";
  }

}
```

* 화면에 대한 DTO 객체는 따로 만들어주고, 엔티티는 핵심 비지니스 로직만 관리하도록 한다.
* API를 만들 때는 절대로 엔티티를 넘기면 안된다!!
  * 엔티티에 필드를 추가했을때, API 스펙이 변하기 때문이다.

<br>

**회원 목록 뷰(templates/members/memberList.html)**

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
  <head th:replace="fragments/header :: header"/>
  <body>
    <div class="container">
      <div th:replace="fragments/bodyHeader :: bodyHeader"/>
      <div>
        <table class="table table-striped">
          <thead>
            <tr>
              <th>#</th>
              <th>이름</th>
              <th>도시</th>
              <th>주소</th>
              <th>우편번호</th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="member : ${members}">
              <td th:text="${member.id}"></td>
              <td th:text="${member.name}"></td>
              <td th:text="${member.address?.city}"></td>
              <td th:text="${member.address?.street}"></td>
              <td th:text="${member.address?.zipcode}"></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div th:replace="fragments/footer :: footer"/>
    </div> <!-- /container -->
  </body>
</html>

```

> 타임리프에서 ?를 사용하면 `null` 을 무시한다.

<br>

### 폼 객체 vs 엔티티 직접 사용

실무에는 **엔티티는 핵심 비즈니스 로직만 가지고 있고, 화면을 위한 로직은 없어야 한다.** 화면이나 API에 맞는 폼 객체나 DTO를 사용하자. 그래서 화면이나 API 요구사항을 이것들로 처리하고, 엔티티는 최대한 순수하게 유지하자.

<br>

## 상품 등록

**상품 등록 폼**

```java
package japbook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookForm {

  private Long id;

  private String name;
  private int price;
  private int stockQuantity;

  private String author;
  private String isbn;

}

```

<br>

**상품 등록 컨트롤러**

```java
package japbook.jpashop.controller;

import japbook.jpashop.domain.item.Book;
import japbook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  @GetMapping("/items/new")
  public String createForm(Model model) {
    model.addAttribute("form", new BookForm());
    return "items/createItemForm";
  }

  @PostMapping("/items/new")
  public String create(BookForm form) {
    Book book = new Book();
    book.setName(form.getName());
    book.setPrice(form.getPrice());
    book.setStockQuantity(form.getStockQuantity());
    book.setAuthor(form.getAuthor());
    book.setIsbn(form.getIsbn());

    itemService.saveItem(book);
    return "redirect:/items";
  }

}

```

<br>

**상품 등록 뷰(items/createItemForm.html)**

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
  <head th:replace="fragments/header :: header"/>
  <body>
    <div class="container">
      <div th:replace="fragments/bodyHeader :: bodyHeader"/>
      <form th:action="@{/items/new}" th:object="${form}" method="post">
        <div class="form-group">
          <label th:for="name">상품명</label>
          <input type="text" th:field="*{name}" class="form-control"
                 placeholder="이름을 입력하세요"></div>
        <div class="form-group">
          <label th:for="price">가격</label>
          <input type="number" th:field="*{price}" class="form-control" placeholder="가격을 입력하세요">
        </div>
        <div class="form-group">
          <label th:for="stockQuantity">수량</label>
          <input type="number" th:field="*{stockQuantity}" class="form-
                                                                  control" placeholder="수량을 입력하세요"></div>
        <div class="form-group">
          <label th:for="author">저자</label>
          <input type="text" th:field="*{author}" class="form-control" placeholder="저자를 입력하세요">
        </div>
        <div class="form-group">
          <label th:for="isbn">ISBN</label>
          <input type="text" th:field="*{isbn}" class="form-control" placeholder="ISBN을 입력하세요">
        </div>
        <button type="submit" class="btn btn-primary">Submit</button>
      </form>
      <br/>
      <div th:replace="fragments/footer :: footer"/>
    </div> <!-- /container -->
  </body>
</html>

```

<br>

## 상품 목록

**상품 목록 컨트롤러**

```java
package japbook.jpashop.controller;

import japbook.jpashop.domain.item.Book;
import japbook.jpashop.domain.item.Item;
import japbook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  @GetMapping("/items/new")
  public String createForm(Model model) {
    model.addAttribute("form", new BookForm());
    return "items/createItemForm";
  }

  @PostMapping("/items/new")
  public String create(BookForm form) {
    Book book = new Book();
    book.setName(form.getName());
    book.setPrice(form.getPrice());
    book.setStockQuantity(form.getStockQuantity());
    book.setAuthor(form.getAuthor());
    book.setIsbn(form.getIsbn());

    itemService.saveItem(book);
    return "redirect:/items";
  }

  @GetMapping("/items")
  public String list(Model model) {
    List<Item> items = itemService.findItems();
    model.addAttribute("items", items);
    return "items/itemList";
  }

}

```

<br>

**상품 목록 뷰**

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org"> <head th:replace="fragments/header :: header" />
  <body>
    <div class="container">
      <div th:replace="fragments/bodyHeader :: bodyHeader"/>
      <div>
        <table class="table table-striped">
          <thead> <tr>
            <th>#</th> <th>상품명</th> <th>가격</th> <th>재고수량</th> <th></th>
            </tr>
          </thead>
          <tbody>
            <tr th:each="item : ${items}">
              <td th:text="${item.id}"></td>
              <td th:text="${item.name}"></td>
              <td th:text="${item.price}"></td>
              <td th:text="${item.stockQuantity}"></td> <td>
              <a href="#" th:href="@{/items/{id}/edit (id=${item.id})}" class="btn btn-primary" role="button">수정</a>
              </td> </tr>
          </tbody>
        </table>
      </div>
      <div th:replace="fragments/footer :: footer"/>
    </div> <!-- /container -->
  </body>
</html>
```

<br>

## 상품 수정

**상품 수정과 관련된 컨트롤러 코드**

```java
package japbook.jpashop.controller;

import japbook.jpashop.domain.item.Book;
import japbook.jpashop.domain.item.Item;
import japbook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  @GetMapping("/items/new")
  public String createForm(Model model) {
    model.addAttribute("form", new BookForm());
    return "items/createItemForm";
  }

  @PostMapping("/items/new")
  public String create(BookForm form) {
    Book book = new Book();
    book.setName(form.getName());
    book.setPrice(form.getPrice());
    book.setStockQuantity(form.getStockQuantity());
    book.setAuthor(form.getAuthor());
    book.setIsbn(form.getIsbn());

    itemService.saveItem(book);
    return "redirect:/items";
  }

  @GetMapping("/items")
  public String list(Model model) {
    List<Item> items = itemService.findItems();
    model.addAttribute("items", items);
    return "items/itemList";
  }

  @GetMapping("/items/{itemId}/edit")
  public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
    Book item = (Book) itemService.findOne(itemId);

    BookForm form = new BookForm();
    form.setId(item.getId());
    form.setName(item.getName());
    form.setPrice(item.getPrice());
    form.setStockQuantity(item.getStockQuantity());
    form.setAuthor(item.getAuthor());
    form.setIsbn(item.getIsbn());

    model.addAttribute("form", form);
    return "items/updateItemForm";
  }

  @PostMapping("/items/{itemId}/edit")
  public String updateItem(@PathVariable String itemId, @ModelAttribute("form") BookForm form) {

    // itemId 를 요청한 사용자가 조작할 권한이 있는지
    //  확인 필요하다.

    Book book = new Book();
    book.setId(form.getId());
    book.setName(form.getName());
    book.setPrice(form.getPrice());
    book.setStockQuantity(form.getStockQuantity());
    book.setAuthor(form.getAuthor());
    book.setIsbn(form.getIsbn());

    itemService.saveItem(book);
    return "redirect:/items";
  }

}
```

<br>

**상품 수정 폼 화면(items/updateItemForm)**

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
  <head th:replace="fragments/header :: header"/>
  <body>
    <div class="container">
      <div th:replace="fragments/bodyHeader :: bodyHeader"/>
      <form th:object="${form}" method="post"> <!-- id -->
        <input type="hidden" th:field="*{id}"/>
        <div class="form-group">
          <label th:for="name">상품명</label>
          <input type="text" th:field="*{name}" class="form-control"
                 placeholder="이름을 입력하세요"/></div>
        <div class="form-group">
          <label th:for="price">가격</label>
          <input type="number" th:field="*{price}" class="form-control" placeholder="가격을 입력하세요"/>
        </div>
        <div class="form-group">
          <label th:for="stockQuantity">수량</label>
          <input type="number" th:field="*{stockQuantity}" class="form- control" placeholder="수량을 입력하세요"/>
        </div>
        <div class="form-group">
          <label th:for="author">저자</label>
          <input type="text" th:field="*{author}" class="form-control"
                 placeholder="저자를 입력하세요"/></div>
        <div class="form-group">
          <label th:for="isbn">ISBN</label>
          <input type="text" th:field="*{isbn}" class="form-control"
                 placeholder="ISBN을 입력하세요"/></div>
        <button type="submit" class="btn btn-primary">Submit</button>
      </form>
      <div th:replace="fragments/footer :: footer"/>
    </div> <!-- /container -->
  </body>
</html>
```

<br>

## 변경 감지와 병합(merge)

* **준영속 엔티티?**

  * 만약 엔티티가 영속 상태로 관리가 되면, 그 엔티티의 값만 바꾸면 JPA가 트랜잭션 Commit 시점에 변경된 점을 DB에 반영한다. => **변경 감지 (dirty checking)**

  * 준영속 엔티티란 JPA 영속성 컨텍스트가 더 이상 관리하지 않는 엔티티이다.

    ```java
    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@PathVariable String itemId, @ModelAttribute("form") BookForm form) {
    
      /**
         * 준영속 엔티티 이다.
         * 데이터베이스로부터 불러온 것이기 때문이다.
         * 즉, 임의의로 만들어낸 엔티티도 기존 식별자를 가지고 있으면
         * 준영속 엔티티로 볼 수 있다.
         * 영속성 컨텍스트가 관리하지 않는다.
         */
      Book book = new Book();
      book.setId(form.getId());
      book.setName(form.getName());
      book.setPrice(form.getPrice());
      book.setStockQuantity(form.getStockQuantity());
      book.setAuthor(form.getAuthor());
      book.setIsbn(form.getIsbn());
    
      itemService.saveItem(book);
      return "redirect:/items";
    }
    ```

<br>

* **준영속 엔티티를 수정하는 2가지 방법**
  * 변경 감지 기능 사용
  * 병합( `merge` ) 사용

<br>

### 변경 감지 기능 사용

```java
@Transcational // transaction을 통해 commit이 되고 flush()가 된다.
public void updateItem(Long itemId, Book param) {
  // 같은 엔티티를 조회해서 영속 상태가 된다.
  Item findItem = itemRepository.findOne(itemId); 
  findItem.setPrice(param.getPrice());
  findItem.setName(param.getName()); // 데이터를 수정한다.
  findItem.setStockQuantity(param.getStockQuantity());
  ...
}
```

1. 트랜잭션 안에서 엔티티를 다시 조회, 변경할 값 선택
2. 트랜잭션 커밋 시점에 변경 감지(Dirty Checking) => 이 동작에서 데이터베이스에 UPDATE SQL 실행

<br>

### 병합 사용

병합은 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능이다.

```java
@Transactional
void update(Item itemParam) { // itemParam: 파라미터로 넘어온 준영속 상태의 엔티티
  Item mergeItem = em.merge(item);
}
```

<br>

#### 병합: 기존에 있는 엔티티

![image](https://user-images.githubusercontent.com/43431081/89731764-91825f00-da84-11ea-9994-e1caf68db939.png)

**병합 동작 방식**

1. `merge()` 를 실행한다.
2. 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다.
   1. 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 1차 캐시에 저장한다.
3. 조회한 영속 엔티티( `mergeMember` )에 `member` 엔티티의 값을 채워 넣는다. (member 엔티티의 모든 값을  mergeMember에 밀어 넣는다. 이때 mergeMember의  "회원1" 이라는 이름이  "회원명변경"으로 바뀐다.)
4. 영속 상태인  mergeMember를 반환한다.

<br>

**병합시 동작 방식을 간단히 정리**

1. 준영속 엔티티의 식별자 값으로 영속 엔티티를 조회한다.
2. 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체한다.(병합한다.)
3. 트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 데이터베이스에 UPDATE SQL이 실행

> **주의**
>
> 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이 변경된다.
>
> 병합시 값이 없으면 `null` 로 업데이트 할 위험도 있다. (병합은 모든 필드를 교체한다.)
>
> 왠만하면  `merge` 를 사용하지 않는다!!

<br>

실무에서는 보통 업데이트 기능이 매우 제한적이다. 그런데 병합은 모든 필드를 변경해버리고, 데이터가 없으면 `null` 로 업데이트 해버린다. 병합을 사용하면서 이 문제를 해결하려면, 변경 폼 화면에서 모든 데이터를 항상 유지해야 한다. 실무에서는 보통 변경가능한 데이터만 노출하기 때문에, **병합을 사용하는 것이 오히려 번거롭다.**

<br>

### 업데이트의 가장 좋은 해결 방법

**엔티티를 변경할 때는 항상 변경 감지를 사용한다.**

* 컨트롤러에서 어설프게 엔티티를 생성하지 말라.
* 트랜잭션이 있는 서비스 계층에 식별자( `id` )와 변경할 데이터를 명확하게 전달하라(파라미터 or DTO).
* 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하라.
* 트랜잭션 커밋 시점에 변경 감지가 실행된다.

```java
@Controller
@RequiredArgsConstructor
public class ItemController {
  
  private final ItemService itemService;
  
  @PostMapping(value = "/items/{itemId}/edit")
  public String updateItem(@ModelAttribute("form"))
  
}
```

<br>

## 상품 주문

**상품 주문 컨트롤러**

```java
package japbook.jpashop.controller;

import japbook.jpashop.domain.Member;
import japbook.jpashop.domain.item.Item;
import japbook.jpashop.repository.OrderSearch;
import japbook.jpashop.service.ItemService;
import japbook.jpashop.service.MemberService;
import japbook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final MemberService memberService;
  private final ItemService itemService;

  @GetMapping("/order")
  public String createForm(Model model) {
    List<Member> members = memberService.findMembers();
    List<Item> items = itemService.findItems();

    model.addAttribute("members", members);
    model.addAttribute("items", items);

    return "order/orderForm";
  }

  @PostMapping("/order")
  // @RequestParam : form 형식으로부터 넘어온 값
  public String order(@RequestParam("memberId") Long memberId,
                      @RequestParam("itemId") Long itemId,
                      @RequestParam("count") int count) {
    orderService.order(memberId, itemId, count);
    return "redirect:/orders";
  }

}

```

<br>

**상품 주문 폼**

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
  <head th:replace="fragments/header :: header"/>
  <body>
    <div class="container">
      <div th:replace="fragments/bodyHeader :: bodyHeader"/>
      <form role="form" action="/order" method="post">
        <div class="form-group">
          <label for="member">주문회원</label>
          <select name="memberId" id="member" class="form-control">
            <option value="">회원선택</option>
            <option th:each="member : ${members}"
                    th:value="${member.id}" th:text="${member.name}"/>
          </select>
        </div>
        <div class="form-group">
          <label for="item">상품명</label>
          <select name="itemId" id="item" class="form-control">
            <option value="">상품선택</option>
            <option th:each="item : ${items}" th:value="${item.id}"
                    th:text="${item.name}"/>
          </select>
        </div>
        <div class="form-group">
          <label for="count">주문수량</label>
          <input type="number" name="count" class="form-control" id="count" placeholder="주문 수량을 입력하세요">
        </div>
        <button type="submit" class="btn btn-primary">Submit</button>
      </form>
      <br/>
      <div th:replace="fragments/footer :: footer"/>
    </div> <!-- /container --> </body>
</html>

```

<br>

## 주문 목록 검색, 취소

**주문 목록 검색 컨트롤러**

```java
package japbook.jpashop.controller;

import japbook.jpashop.domain.Member;
import japbook.jpashop.domain.Order;
import japbook.jpashop.domain.item.Item;
import japbook.jpashop.repository.OrderSearch;
import japbook.jpashop.service.ItemService;
import japbook.jpashop.service.MemberService;
import japbook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final MemberService memberService;
  private final ItemService itemService;

  @GetMapping("/order")
  public String createForm(Model model) {
    List<Member> members = memberService.findMembers();
    List<Item> items = itemService.findItems();

    model.addAttribute("members", members);
    model.addAttribute("items", items);

    return "order/orderForm";
  }

  @PostMapping("/order")
  // @RequestParam : form 형식으로부터 넘어온 값
  public String order(@RequestParam("memberId") Long memberId,
                      @RequestParam("itemId") Long itemId,
                      @RequestParam("count") int count) {
    orderService.order(memberId, itemId, count);
    return "redirect:/orders";
  }

  @GetMapping("/orders")
  public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
    List<Order> orders = orderService.findOrders(orderSearch);
    model.addAttribute("orders", orders);
//    model.addAttribute("orderSearch", orderSearch); 이 코드가 생략이되서 작성되어 있는 것이다.
    return "order/orderList";
  }

  @PostMapping("/orders/{orderId}/cancel")
  public String cancelOrder(@PathVariable("orderId") Long orderId) {
    orderService.cancelOrder(orderId);
    return "redirect:/orders";
  }

}

```

<br>

**주문 목록 검색 화면(order/orderList)**

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head th:replace="fragments/header :: header"/>
<body>
<div class="container">
    <div th:replace="fragments/bodyHeader :: bodyHeader"/>
    <div>
        <div>
            <form th:object="${orderSearch}" class="form-inline">
                <div class="form-group mb-2">
                    <input type="text" th:field="*{memberName}" class="form- control" placeholder="회원명"/>
                </div>
                <div class="form-group mx-sm-1 mb-2">
                    <select th:field="*{orderStatus}" class="form-control">
                        <option value="">주문상태</option>
                        <option th:each="status : ${T(japbook.jpashop.domain.OrderStatus).values()}"
                                th:value="${status}"
                                th:text="${status}">option
                        </option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary mb-2">검색</button>
            </form>
        </div>
        <table class="table table-striped">
            <thead>
            <tr>
                <th>#</th>
                <th>회원명</th>
                <th>대표상품 이름</th>
                <th>대표상품 주문가격</th>
                <th>대표상품 주문수량</th>
                <th>상태</th>
                <th>일시</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <tr th:each="item : ${orders}">
                <td th:text="${item.id}"></td>
                <td th:text="${item.member.name}"></td>
                <td th:text="${item.orderItems[0].item.name}"></td>
                <td th:text="${item.orderItems[0].orderPrice}"></td>
                <td th:text="${item.orderItems[0].count}"></td>
                <td th:text="${item.status}"></td>
                <td th:text="${item.orderDate}"></td>
                <td>
                    <a th:if="${item.status.name() == 'ORDER'}" href="#" th:href="'javascript:cancel('+${item.id}+')'"
                       class="btn btn-danger">CANCEL</a></td>
            </tr>
            </tbody>
        </table>
    </div>
    <div th:replace="fragments/footer :: footer"/>
</div> <!-- /container -->
</body>
<script>
    function cancel(id) {
        var form = document.createElement("form");
        form.setAttribute("method", "post");
        form.setAttribute("action", "/orders/" + id + "/cancel");
        document.body.appendChild(form);
        form.submit();
    } </script>
</html>

```



