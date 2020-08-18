# MapBasedSecurityMetadataSource (1)

![image](https://user-images.githubusercontent.com/43431081/90378686-a11d2b80-e0b4-11ea-8a69-c459c64cf7a2.png)

* **Filter 기반 URL 방식** => *Filter 기반*
  * 인증이 필요한 URL이면 그에 해당하는 Filter가 가로채서 인증을 한다.
* **AOP 기반 Method 방식** => *Proxy 기반*
  * 인증이 필요한 메서드를 갖고 있는 프록시 객체를 사용하여 인증한다.

> 둘 다 AccessDecisionManager 에게 권한 목록을 보내주는 것은 같다.