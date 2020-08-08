# 라이브러리 살펴보기

## 스프링 부트 라이브러리

* spring-boot-starter-web
  * sprint-boot-starter-tomcat: **톰캣 (웹서버)**
  * spring-webmvc: **스프링 웹 MVC**
* spring-boot-starter-thymeleaf: **타임리프 템플릿 엔진(View)**
* spring-boot-starter-data-jpa
  * spring-boot-starter-aop
  * spring-boot-starter-jdbc
    * HikariCP 커넥션 풀 (부트 2.0 기본)
  * hibernate + JPA: 하이버네이트 + JPA
  * spring-data-jpa: 스프링 데이터 JPA
* spring-boot-starter(공통): 스프링 부트 + 스프링 코어 + 로깅
  * spring-boot
    * spring-core
  * spring-boot-starter-logging
    * logback, slf4j

<br>

## 테스트 라이브러리

* spring-boot-starter-test
  * junit: 테스트 프레임워크
  * mockito: 목 라이브러리(테스트 프레임워크). 이 프레임 워크를 사용하면 테스트 중심 개발 또는 동작 기반 개발을 위해 자동화 된 단위 테스트에서 테스트 이중 오브젝트를 작성할 수 있다.
  * assertj: 테스트 코드를 좀 더 편하게 작성하게 도와주는 라이브러리
  * spring-test: 스프링 통합 테스트 지원

<br>

## 라이브러리 정리

* **핵심 라이브러리**
  * 스프링 MVC
  * 스프링 ORM
  * JPA, 하이버네이트
  * 스프링 데이터 JPA
* **기타 라이브러리**
  * H2 데이터베이스 클라이언트
  * 커넥션 풀: 부트 기본은 HikariCP
  * WEB(thymeleaf)
  * 로깅 SLF4J & LogBack
  * 테스트