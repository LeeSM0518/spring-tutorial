# 어노테이션 권한 설정

* **보안이 필요한 메소드에 설정한다.**
* **@PreAuthorize, @PostAuthorize**
  * SpEL 지원
  * `@PreAuthorize("hasRole('ROLE_USER') and (#account.username == principal.username)")`
  * *PrerPostAnnotationSecurityMetadataSource* 가 담당
* **@Secured, @RolesAllowed**
  * SpEL 미지원
  * `@Secured("ROLE_USER")` , `@RolesAllowed("ROLE_USER")`
  * *SecuredAnnotationSecurityMetadataSource* , *Jsr250MethodSecurityMetadataSource* 가 담당
* **@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)**

<br>

![image](https://user-images.githubusercontent.com/43431081/90362578-e6812f00-e09b-11ea-957e-1aa34bba6f6d.png)

<br>

## 실제 코드

* **AopSecurityController**

  ```java
  @Controller
  public class AopSecurityController {
  
    @GetMapping("/preAuthorize")
    @PreAuthorize("hasRole('ROLE_USER') and #account.username == principal.username")
    public String preAuthorize(AccountDto account, Model model, Principal principal) {
      model.addAttribute("method", "Success PreAuthorize");
  
      return "aop/method";
    }
  
  }
  ```

* **UserServiceImple**

  ```java
  @Slf4j
  @Service("userService")
  public class UserServiceImple implements UserService {
  
    ...
  
    @Override
    @Secured("ROLE_MANAGER")
    public void order() {
      System.out.println("order");
    }
  
  }
  ```

* **SecurityConfig**

  ```java
  @Configuration
  @EnableWebSecurity
  @Slf4j
  @Order(1)
  // 메소드 인증 방식을 적용하기 위한 어노테이션
  @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
    ...
  ```