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