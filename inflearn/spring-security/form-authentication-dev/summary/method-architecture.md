# Method 방식 - 주요 아키텍처

## 인가 처리를 위한 초기화 과정과 진행

![image](https://user-images.githubusercontent.com/43431081/90365499-d1f36580-e0a0-11ea-86ce-53ac222de3c6.png)

* *초기화 과정*
  1. 초기화 시 전체 빈을 검사하면서 보안이 설정된 메소드가 있는지 탐색
  2. 빈의 프록시 객체를 생성
  3. 보안 메소드에 인가처리(권한심사) 기능을 하는 Advice 를 등록
  4. 빈 참조시 실제 빈이 아닌 프록시 빈 객체를 참조
* *진행 과정*
  1. 메소드 호출 시 프록시 객체를 통해 메소드를 호출
  2. Advice가 등록되어 있다면 Advice를 작동하게 하여 인가 처리
  3. 권한 심사 통과하면 실제 빈의 메서드를 호출

<br>

## 인가 처리를 위한 초기화 과정

![image](https://user-images.githubusercontent.com/43431081/90365845-737ab700-e0a1-11ea-89be-f09ad9e08c82.png)

<br>

## 처리 과정

![image](https://user-images.githubusercontent.com/43431081/90365881-83929680-e0a1-11ea-875f-f58fe3708ad8.png)

<br>

## AOP 이해

![image](https://user-images.githubusercontent.com/43431081/90365966-a91fa000-e0a1-11ea-99f4-da5c7df99977.png)