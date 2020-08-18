# 실시간 메소드 보안 구현

* **개선점**
  * 메소드 보안은 스프링 시큐리티 초기화 시점에 보안 적용 대상 빈의 프록시 생성 및 어드바이스 적용이 이루어짐
  * DB에 자원을 실시간으로 업데이트 하더라도 AOP가 바로 적용되지 않음
* **보안 메소드 실시간 적용 처리 과정**
  1. 메소드 보안 최초 설정 시 대상 빈의 프록시 객체 생성하고 메소드에 Advice 등록하여 AOP 적용
  2. MapBasedMethodSecurityMetadataSource 에 자원 및 권한 정보 전달
  3. DefaultSingleBeanRegistry로 실제 빈을 삭제하고 프록시 객체를 빈 참조로 등록한다.
  4. 보안이 적용된 메소드 호출 시 Advice가 작동한다.
  5. 메소드 보안 해제 메소드에 등록된 Advice를 제거한다.
  6. 메소드 보안 재 설정 시 메소드에 등록된 Advice를 다시 등록한다.

<br>

## 처리 과정

![image](https://user-images.githubusercontent.com/43431081/90455390-ebdc8900-e130-11ea-9384-22c034af076e.png)