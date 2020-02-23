# Chapter 05. 컴포넌트 스캔

자동 주입과 함께 사용하는 추가 기능이 **컴포넌트 스캔이다.** 컴포넌트 스캔은 **스프링이 직접 클래스를 검색해서 빈으로 등록해주는 기능이다.** 설정 클래스에 빈으로 등록하지 않아도 원하는 클래스를 빈으로 등록할 수 있으므로 컴포넌트 스캔 기능을 사용하면 설정 코드가 크게 줄어든다.

<br>

# 1. @Component 애노테이션으로 스캔 대상 지정

스프링이 검색해서 빈으로 등록할 수 있으려면 클래스에 **@Component 애노테이션을** 붙여야 한다.

@Component 애노테이션은 **해당 클래스를 스캔 대상으로 표시한다.**

* **spring/MemberDao.java**

  ```java
  import org.springframework.stereotype.Component;
  
  @Component
  public class MemberDao {
  
    private static long nextId = 0;
  
    private Map<String, Member> map = new HashMap<>();
  
    public Member selectByEmail(String email) {
      return map.get(email);
    }
  
    ...
  
  }
  ```

* **spring/MemberRegisterService.java**

  ```java
  @Component
  public class MemberRegisterService {
  
    @Autowired
    private MemberDao memberDao;
    
    ...
      
  }
  ```

* **spring/MemberInfoPrinter.java**

  ```java
  @Component("infoPrinter")
  public class MemberInfoPrinter {
  
    private MemberDao memberDao;
    private MemberPrinter printer;
  
    ...
  
  }
  ```

* **spring/MemberListPrinter.java**

  ```java
  @Component("listPrinter")
  public class MemberListPrinter {
  
    private MemberDao memberDao;
    private MemberPrinter printer;
  
    ...
    
  }
  ```

* @Component 애노테이션에 값을 주었는지에 따라 빈으로 등록할 때 사용할 이름이 지정된다.

  * @Component 애노테이션에 값을 주지 않으면, 클래스 이름의 첫 글자를 소문자로 바꾼 이름을 빈 이름으로 사용한다.
    * ex) MemberDao => "memberDao", MemberRegisterService => "memberRegisterService"
  * @Component 애노테이션에 값을 주면 그 값을 빈 이름으로 사용한다.
    * ex) MemberInfoPrinter => "infoPrinter", MemberListPrinter => "listPrinter"

<br>

# 2. @ComponentScan 애노테이션으로 스캔 설정

@Component 애노테이션을 붙인 클래스를 스캔해서 **스프링 빈으로 등록하려면 설정 클래스에 @ComponentScan 애노테이션을 적용해야 한다.**

* **conf/AppCtx.java**

  ```java
  @Configuration
  @ComponentScan(basePackages = {"spring"})
  public class AppCtx {
  
    @Bean
    @Qualifier("printer")
    public MemberPrinter memberPrinter1() {
      return new MemberPrinter();
    }
  
    @Bean
    @Qualifier("summaryPrinter")
    public MemberSummaryPrinter memberPrinter2() {
      return new MemberSummaryPrinter();
    }
  
    @Bean
    public VersionPrinter versionPrinter() {
      VersionPrinter versionPrinter = new VersionPrinter();
      versionPrinter.setMajorVersion(5);
      versionPrinter.setMinorVersion(0);
      return versionPrinter;
    }
  
  }
  ```

  * 스프링 컨테이너가 @Component 애노테이션을 붙인 클래스를 검색해서 빈으로 등록해주기 때문에 설정 코드가 줄어든 것을 알 수 있다.
  * @ComponentScan 애노테이션의 basePackages 속성값은 **{"spring"}** 이다. 이 속성은 스캔 대상 패키지 목록을 지정한다.
  * 즉, 위의 코드에서는 spring 패키지와 그 하위 패키지에 속한 클래스를 스캔 대상으로 설정한다.
  * 스캔 대상에 해당하는 클래스 중에서 **@Component 애노테이션이 붙은 클래스의 객체를 생성해서 빈으로 등록한다.**

<br>

# 3. 예제 실행

이전의 빈 객체를 가져왔던 코드를 수정을 해야한다.

* **main/MainForSpring.java**

  ```java
  // processListCommand() 메서드
  MemberRegisterService registerService =
    ctx.getBean("memberRegSvc", MemberRegisterService.class);
  
  // processInfoCommand() 메서드
  ChangePasswordService changePasswordService
    = ctx.getBean("changePwdSvc", ChangePasswordService.class);
  ```

  @Component 애노테이션을 붙일 때 속성값을 주지 않았기 때문에, 빈 객체 이름이 바뀌게 된다. 빈 객체를 불러오는 아래와 같이 빈 타입만으로 빈을 구하도록 코드를 수정한다.

  ```java
  // processListCommand() 메서드
  MemberRegisterService registerService =
    ctx.getBean(MemberRegisterService.class);
  
  // processInfoCommand() 메서드
  ChangePasswordService changePasswordService
    = ctx.getBean(ChangePasswordService.class);
  ```

<br>

# 4. 스캔 대상에서 제외하거나 포함하기

**excludeFilters 속성을** 사용하면 스캔할 때 특정 대상을 자동 등록 대상에서 제외할 수 있다.

* **config/AppCtxWithExclude.java**

  ```java
  @Configuration
  @ComponentScan(basePackages = {"spring"},
                 excludeFilters = 
                 @ComponentScan.Filter(
                   type= FilterType.REGEX, pattern = "spring\\..*Dao"))
  public class AppCtxWithExclude {
  
    @Bean
    public MemberDao memberDao() {
      return new MemberDao();
    }
  
    @Bean
    @Qualifier("printer")
    public MemberPrinter memberPrinter1() {
      return new MemberPrinter();
    }
  
  }
  ```

  * @Filter 애노테이션의 type 속성값으로 **FilterType.REGEX** 를 주었다. 이는 **정규표현식을 사용해서 제외 대상을 지정한다는 것을 의미한다.**
    * **spring\\\\..*Dao** : "spring." 으로 시작하고 Dao로 끝나는 정규표현식
      * spring.MemberDao 클래스를 컴포넌트 스캔 대상에서 제외한다.

<br>

**FilterType.ASPECTJ를** 필터 타입으로 설정할 수도 있다. 이 타입을 사용하면 정규표현식 대신 **AspectJ 패턴을 사용해서 대상을 지정한다.**

* **config/AppCtxWithExclude.java**

  ```java
  @Configuration
  @ComponentScan(basePackages = {"spring"}, 
                 excludeFilters = 
                 @ComponentScan.Filter(
                   type = FilterType.ASPECTJ, pattern = "spring.*Dao"))
  public class AppCtxWithExclude {
  
    @Bean
    public MemberDao memberDao() {
      return new MemberDao();
    }
    
    ...
  ```

  * AspectJ 패턴은 정규표현식과 다르다. 자세한 내용은 7장에서 살펴보자.

  * **spring.*Dao** : spring 패키지의 Dao로 끝나는 타입

    * spring.MemberDao 클래스를 컴포넌트 스캔 대상에서 제외한다.

  * AspectJ 패턴이 동작하려면 의존 대상에 **aspectjweaver 모듈을 추가해야 한다.**

    * **build.gradle**

      ```
      plugins {
          id 'java'
      }
      
      group 'org.example'
      version '1.0-SNAPSHOT'
      
      sourceCompatibility = 1.8
      
      repositories {
          mavenCentral()
      }
      
      dependencies {
          testCompile group: 'junit', name: 'junit', version: '4.12'
          implementation 'org.springframework:spring-context:5.2.3.RELEASE'
          implementation 'org.aspectj:aspectjweaver:1.9.5'
      }
      ```

  * patterns 속성은 String[ ] 타입이므로 배열을 이용해서 **패턴을 한 개 이상 지정할 수 있다.**

<br>

특정 애노테이션을 붙인 타입을 컴포넌트 대상에서 제외할 수도 있다.

* **예시 코드**

  ```java
  @Retention(RUNTIME)
  @Target(TYPE)
  public @interface NoProduct {}
  
  @Retention(RUNTIME)
  @Target(TYPE)
  public @interface ManualBean {}
  ```

  * 여기서 @NoProduct 나 @ManualBean 애노테이션을 붙인 클래스는 컴포넌트 스캔 대상에서 제외하고 싶다고 예를 들어보자.

* **conf/AppCtxWithExclude.java**

  ```java
  @Configuration
  @ComponentScan(basePackages = {"spring"},
      excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = 
                                             {NoProduct.class, ManualBean.class}))
  public class AppCtxWithExclude {
  
    @Bean
    public MemberDao memberDao() {
      return new MemberDao();
    }
  
    ...
  ```

  * 위와 같이 excludeFilters 속성으로 설정하고 type 속성값으로 **FilterType.ANNOTATION을 사용하고 classes 속성에 필터로 사용할 애노테이션 타입을 값으로 준다.** 

    * 제외될 클래스

      ```java
      @ManualBean
      @Component
      public class MemberDao {
        ...
      }
      ```

<br>

특정 타입이나 그 하위 타입을 컴포넌트 스캔 대상에서 제외하려면 **ASSIGNABLE_TYPE을 FilterType으로 사용한다.**

* **config/AppCtxWithExclude.java**

  ```java
  @Configuration
  @ComponentScan(basePackages = {"spring"},
      excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
                                             classes = MemberDao.class))
  public class AppCtxWithExclude { ...
  ```

  * classes 속성에는 제외할 타입 목록을 지정한다.

<br>

설정할 필터가 두 개 이상이면 @ComponentScan 의 excludeFilters 속성에 배열을 사용해서 @Filter 목록을 전달하면 된다.

* **config/AppCtxWithExclude.java**

  ```java
  @Configuration
  @ComponentScan(basePackages = {"spring"},
      excludeFilters = {
        @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE, classes = MemberDao.class)),
        @ComponentScan.Filter(
          type = FilterType.REGEX, pattern = "spring2\\..*"))}
  public class AppCtxWithExclude { ...
  ```

<br>

## 4.1. 기본 스캔 대상

@Component 애노테이션을 붙인 클래스만 컴포넌트 스캔 대상에 포함되는 것은 아니다.

* **컴포넌트 스캔 대상 애노테이션**
  * @Component
  * @Controller
  * @Service
  * @Repository
  * @Aspect
  * @Configuration

<br>

@Aspect 애노테이션을 제외한 나머지 애노테이션은 실제로는 @Component 애노테이션에 대한 특수 애노테이션이다.

* **@Controller 애노테이션 예시**

  ```java
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @Component
  public @interface Controller {
    
    @AliasFor(annotation = Component.class)
    String values() default "";
    
  }
  ```

* @Controller 애노테이션은 웹 MVC와 관련 있다.

* @Repository 애노테이션은 DB 연동과 관련 있다.

<br>

# 5. 컴포넌트 스캔에 따른 충돌 처리

컴포넌트 스캔 기능을 사용해서 자동으로 빈을 등록할 때에는 충돌에 주의해야 한다.

크게 **빈 이름 충돌과 수동 등록에 따른 충돌이** 발생할 수 있다.

<br>

## 5.1. 빈 이름 충돌

spring 패키지와 spring2 패키지에 MemberRegisterService 클래스가 존재하고 두 클래스 모두 @Component 애노테이션을 붙였다고 가정해보자.

* **디렉토리 구조**

  ```
  .
  ├── config
  │   └── AppCtx.java
  ├── main
  │   └── MainForSpring.java
  ├── spring
  │   ...
  │   └── MemberRegisterService.java
  └── spring2
      └── MemberRegisterService.java
  ```

* **config/AppCtx.java (설정 클래스)**

  ```java
  @Configuration
  @ComponentScan(basePackages = {"spring", "spring2"})
  public class AppCtx {
    ...
  }
  ```

* **실행 결과**

  ```
  Caused by: org.springframework.context.annotation.ConflictingBeanDefinitionException: Annotation-specified bean name 'memberRegisterService' for bean class [spring2.MemberRegisterService] conflicts with existing, non-compatible bean definition of same name and class [spring.MemberRegisterService]
  ```

  * spring2.MemberRegisterService 클래스를 빈으로 등록할 때 사용한 빈 이름인 memberRegisterService가 타입이 일치하지 않는 spring.MemberRegisterService 타입의 빈 이름과 충돌난다는 것을 알 수 있다.
  * 이처럼 서로 다른 타입인데 같은 빈 이름을 사용하는 경우가 있다면 둘 중 하나에 **명시적으로 빈 이름을 지정해서 이름 충돌을 피해야 한다.** 

<br>

## 5.2. 수동 등록한 빈과 충돌

* **spring/MemberDao.java**

  ```java
  @Component
  public class MemberDao {
    ...
  }
  ```

  * MemberDao 클래스는 컴포넌트 스캔 대상이다.

  * 자동 등록된 빈의 이름은 "memberDao" 이다.

  * 하지만, 설절 클래스에 직접 MemberDao 클래스를 "memberDao" 라는 이름의 빈으로 등록한다면?

    * **config/AppCtx.java**

      ```java
      @Configuration
      @ComponentScan(basePackages = {"spring"})
      public class AppCtx {
        
        @Bean
      //  public MemberDao memberDao() {
          public MemberDao memberDao2() {
          MemberDao memberDao = new MemberDao();
          return memberDao;
        }
        
      }
      ```

    * 이 경우 스캔을 통해 등록한 "memberDao" 빈과 수동 등록한 "memberDao2" 빈이 모두 존재한다.

    * 이렇게 빈이 두 개가 생성되므로 자동 주입하는 코드는 **@Qualifier 애노테이션을** 사용해서 알맞은 빈을 선택해야 한다.