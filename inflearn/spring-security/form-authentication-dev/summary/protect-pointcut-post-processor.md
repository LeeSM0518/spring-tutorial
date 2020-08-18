# ProtectPointcutPostProcessor

* **메소드 방식의 인가처리를 위한 자원 및 권한정보 설정 시 자원에 포인트 컷 표현식을 사용할 수 있도록 지원하는 클래스**
* **빈 후처리기로서 스프링 초기화 과정에서 빈 들을 검사하여 빈이 가진 메소드 중에서 포인트 컷 표현식과 matching 되는 클래스, 메소드, 권한 정보를 MapBasedMethodSecurityMetadataSource 에 전달하여 인가처리가 되도록 제공되는 클래스**
* **DB 저장 방식**
  * *Method 방식*
    * `io.security.service.OrderService.order` : ROLE_USER
  * *Pointcut 방식*
    * `execution( * io.security.service.*Service.*(..))` : ROLE_USER
* **설정 클래스에서 빈 생성시 접근제한자가 package 범위로 되어 있기 때문에 리플렉션을 이용해 생성한다.**

<br>

![image](https://user-images.githubusercontent.com/43431081/90387098-88b30e00-e0c0-11ea-8934-6ab5e8a46b6c.png)

* **MethodResourcesMapFactoryBean**
  * DB로부터 얻은 권한/자원 정보를 ResourceMap 빈으로 생성해서 ProtectPointcutPostProcessor 에 전달

<br>

## 처리 과정

![image](https://user-images.githubusercontent.com/43431081/90387185-b26c3500-e0c0-11ea-94c5-de4609f82ebf.png)