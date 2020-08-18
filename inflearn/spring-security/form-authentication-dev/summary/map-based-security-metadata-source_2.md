# MapBasedSecurityMetadataSource (2)

![image](https://user-images.githubusercontent.com/43431081/90380086-96639600-e0b6-11ea-8b00-4841c07bd149.png)

* **MapBasedMethodSecurityMetadataSource**
  * 어노테이션 설정 방식이 아닌 맵 기반으로 권한 설정
  * 기본적인 구현이 완성되어 있고 DB로부터 자원과 권한 정보를 매핑한 데이터를 전달하면 메소드 방식의 인가 처리가 이루어지는 클래스

<br>

## 처리 과정

![image](https://user-images.githubusercontent.com/43431081/90380319-e04c7c00-e0b6-11ea-90e2-f8a030250519.png)

* **MethodMap**
  * Key : 메소드 명
  * Value : 권한 목록

<br>

## 사용 예시

```java
@Configuration
// 메소드 보안 활성 어노테이션
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
// GlobalMethodSecurityConfiguration : 스프링 시큐리티가 초기화될 때 빈들을 초기화한다.
class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
  @Override
  protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
    return new MapBasedMethodSecurityMetadataSource();
  }
}
```

<br>

## 실제 코드

* **MethodSecurityConfig**

  ```java
  @Configuration
  // 맵 기반 인증을 사용할 것이기 때문에, 속성들은 default(false)로 둔다.
  @EnableGlobalMethodSecurity
  // 맵 기반 인증 처리를 위한 GlobalMethodSecurityConfiguration 상속
  public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
  
    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
      // MapBasedMethodSecurityMetadataSource : 맵 기반 메소드 인증 처리를 위한 클래스
      return new MapBasedMethodSecurityMetadataSource();
    }
  }
  ```