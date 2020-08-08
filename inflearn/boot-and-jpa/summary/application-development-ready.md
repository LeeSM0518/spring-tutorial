# 애플리케이션 구현 준비

## 구현 요구사항

* 회원 기능
  * 회원 등록
  * 회원 조회
* 상품 기능
  * 상품 등록
  * 상품 수정
  * 상품 조회
* 주문 기능
  * 상품 주문
  * 주문 내역 조회
  * 주문 취소

<br>

## 애플리케이션 아키텍처

![image](https://user-images.githubusercontent.com/43431081/89708713-6b8d8980-d9b4-11ea-8109-1d99b1129aae.png)

**계층형 구조 사용**

* controller, web : 웹 계층
* service : 비즈니스 로직, 트랜잭션 처리
* repositoty : JPA를 직접 사용하는 계층, 엔티티 매니저 사용
* domain: 엔티티가 모여 있는 계층, 모든 계층에서 사용

<br>

**패키지 구조**

* jpabook.jpashop
  * domain
  * exception
  * repository
  * service
  * web

<br>

**개발 순서: 서비스, 리포지토리 계층을 개발하고, 테스트 케이스를 작성해서 검증, 마지막에 웹 계층 적용**