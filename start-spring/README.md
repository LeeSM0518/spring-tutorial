# Chapter 02. 스프링 시작하기

* **Content**
  * 스프링 프로젝트 생성
  * 간단한 스프링 예제
  * 스프링 컨테이너

<br>

# 1. 스프링 프로젝트 시작하기

## 1.1. 메이븐 프로젝트 생성

* **maven-tutorial/pom.xml**

  ```xml
  <?xml version="1.0" encoding="UTF-8" ?>
  <project xmlns="http://maven.apache.org/POM/4.0.0"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>tutorial</groupId>
    <artifactId>maven-tutorial</artifactId>
    <version>0.0.1-SNAPSHOT</version>
  
    <dependencies>
      <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>5.2.3.RELEASE</version>
      </dependency>
    </dependencies>
  
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.8.1</version>
          <configuration>
            <source>1.8</source>
            <target>1.8</target>
            <encoding>utf-8</encoding>
          </configuration>
        </plugin>
      </plugins>
    </build>
  </project>
  ```

  * **07행** : 프로젝트의 식별자를 지정한다. 여기서는 프로젝트 폴더와 동일한 이름인 maven-tutorial 을 사용한다.
  * **11~15행** : 프로젝트에서 5.2.3. RELEASE 버전의 spring-context 모듈을 사용한다고 설정한다.
  * **20~29행** : 1.8 버전을 기준으로 자바 소스를 컴파일하고 결과 클래스를 생성한다. 자바 컴파일러가 소스 코드를 읽을 때 사용할 인코딩은 UTF-8로 설정한다.

> 뒤쪽에서 메이븐에 대해서 더 자세하게 배워보도록 해보자.

<br>

### 1.1.1. 메이븐 의존 설정

pom.xml 파일에서 의존 설정을 하였다.

```xml
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-context</artifactId>
  <version>5.2.3.RELEASE</version>
</dependency>
```

> 메이븐은 한 개의 모듈을 아티팩트라는 단위로 관리한다.

* **spring-context** : 모듈 식별자
* **5.2.3.RELEASE** : 아티팩트 버전
* 위의 의존 설정은 메이븐 프로젝트의 소스 코드를 컴파일하고 실행할 때 사용할 클래스 패스에 **spring-context-5.2.3.RELEASE.jar** 파일을 추가한다는 의미이다.

<br>

### 1.1.2. 메이븐 리포지토리

pom.xml 파일에 의존 설정을 추가했지만 아직 spring-context-5.2.3.RELEASE.jar 파일을 다운로드하지 않았다. 이 파일을 다운받기 위해서는 **원격 리포지토리나 로컬 리포지토리로 부터 다운받아야 한다.**

메이븐은 코드를 컴파일하거나 실행할 때 **\<dependency>로** 설정한 아티팩트 파일을 사용한다. 아티팩트 파일은 다음 과정을 거쳐 구한다

* **메이븐 로컬 리포지토리에서** [그룹ID]\\[아티팩트ID]\\[버전] 폴더에 아티팩트ID-버전.jar 형식의 이름을 갖는 파일이 있는지 검사한다. 파일이 존재하면 이 파일을 사용한다.
* 로컬 리포지토리에 파일이 없으면 **메이븐 원격 중앙 리포지토리로부터** 해당 파일을 다운로드하여 로컬 리포지토리에 복사한 뒤 그 파일을 사용한다.

<br>

이제 우리는 Terminal 에서 앞서 생성한 프로젝트 폴더에서 **'mvn compile'** 명령을 실행해보자.

```bash
~/spring-tutorial/start-spring/maven-tutorial $ mvn compile
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------------< tutorial:maven-tutorial >-----------------------
[INFO] Building maven-tutorial 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.8.1/maven-compiler-plugin-3.8.1.pom
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.8.1/maven-compiler-plugin-3.8.1.pom (12 kB at 11 kB/s)
...
```

* repo.maven.apache.org가 메이븐 중앙 리포지토리이며, 이곳에서 필요한 파일을 다운로드 한 뒤에 로컬 리포지토리에 복사한다.
* 이후에는 원격 리포지토리에서 다운로드하지 않는다.

<br>

### 1.1.3. 의존 전이(Transitive Dependencies)

**의존(\<dependency>)에서** 설정한 아티팩트도 의존하는 파일이 있을 수 있기 때문에 다시 의존하는 파일도 포함된다.

예를 들어 spring-context-5.2.3.RELEASE.jar 파일을 다운로드하기 전에 spring-context-5.2.3.pom 파일을 다운로드한다. 그 후 이 파일이 의존하는 파일들을 다운로드 받는다.

즉, 한 아티팩트를 다운로드 받을 때 의존한 아티팩트가 또다시 의존하고 있는 다른 아티팩트가 있다면 그 아티팩트도 함께 다운로드한다. 이렇게 의존 대상이 다시 의존하는 대상까지도 의존 대상에 포함하기 때문에 이를 **의존 전이(Transitive Dependencies)** 라고 한다.

<br>

### 1.1.4. 메이븐 기본 폴더 구조

```
└── maven-tutorial
    ├── lib
    │   ├── aopalliance-1.0.jar
    │   ...
    │   └── spring-tx-5.2.3.RELEASE.jar
    ├── pom.xml
    └── src
        └── main
            ├── java
            ├── resources
            └── webapp
                ├── WEB-INF
                └── web.xml
```

* **src/main/java** : 자바 소스 코드가 위치하는 디렉토리
* **src/main/resources** : XML이나 프로퍼티 파일과 같이 자바 소스 이외의 다른 자원 파일이 위치하는 디렉토리
* **src/main/webapp** : JSP 소스 코드나 WEB-INF/web.xml 파일 등이 위치하는 디렉토리
* pom.xml 은 프로젝트 폴더에 위치한다.

<br>

## 1.2. 그레이들 프로젝트 생성

* **Intellij gradle 프로젝트 만들기**

  ![image](https://user-images.githubusercontent.com/43431081/74120721-d9c0e180-4c07-11ea-9ee2-ee8b95213000.png)

* **Dependency 추가**

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
      compile 'org.springframework:spring-context:5.3.2'
  }
  ```

<br>

## 1.3. 예제 코드 작성

* **gradle-tutorial2/src/main/java/chap02/Greeter.java**

  ```java
  public class Greeter {
  
    private String format;
  
    public String greet(String guest) {
      return String.format(format, guest);
    }
  
    public void setFormat(String format) {
      this.format = format;
    }
  
  }
  
  ```

* **gradle-tutorial2/src/main/java/chap02/AppContext.java**

  ```java
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  
  @Configuration
  public class AppContext {
  
    @Bean
    public Greeter greeter() {
      Greeter g = new Greeter();
      g.setFormat("%s, 안녕하세요!");
      return g;
    }
  
  }
  
  ```

  * 스프링은 객체를 생성하고 초기화하는 기능을 제공한다.
    * 스프링이 생성하는 객체를 **빈(Bean)** 객체라고 부른다.
      * 위의 예제에서 빈 객체에 대한 정보를 담고 있는 메서드가 **greeter() 메서드이다.**
    * **@Bean 애노테이션을** 메서드에 붙이면 해당 메서드가 생성한 객체를 스프링이 관리하는 빈 객체로 등록한다.

* **gradle-tutorial2/src/main/java/chap02/Main.java**

  ```java
  // AnnotationConfigApplicationContext : 자바 설정에서 정보를 읽어와 빈 객체를 생성하고 관리한다.
  import org.springframework.context.annotation.AnnotationConfigApplicationContext;
  
  public class Main {
  
    public static void main(String[] args) {
      // AnnotationConfigApplicationContext 객체를 생성할 때
      //  앞서 작성한 AppContext 클래스를 파라미터로 넘긴다.
      //  AppContext 에 정의한 @Bean 설정 정보를 읽어와 Greeter 객체를 생성하고 초기화한다.
      AnnotationConfigApplicationContext ctx =
          new AnnotationConfigApplicationContext(AppContext.class);
      
      // AnnotationConfigApplicationContext 가 자바 설정을 읽어와 생성한 빈 객체를 검색
      //  첫 번째 파라미터 : 빈 객체 이름
      //  두 번째 파라미터 : 빈 객체의 타입
      //    Greeter 객체를 가져온다. 
      Greeter g = ctx.getBean("greeter", Greeter.class);
      
      String msg = g.greet("스프링");
      System.out.println(msg);
      ctx.close();
    }
  
  }
  ```

* **실행 결과**

  ```
  4:16:45 오후: Executing task 'Main.main()'...
  
  > Task :compileJava
  > Task :processResources NO-SOURCE
  > Task :classes
  
  > Task :Main.main()
  스프링, 안녕하세요!
  
  BUILD SUCCESSFUL in 1s
  2 actionable tasks: 2 executed
  4:16:47 오후: Task execution finished 'Main.main()'.
  ```

<br>

# 2. 스프링은 객체 컨테이너

위의 예제에서 핵심은 **AnnotationConfigApplicationContext 클래스이다.** 스프링의 핵심 기능은 **객체를 생성하고 초기화하는 것이다.** 

AnnotationConfigApplicationContext 클래스는 자바 클래스에서 정보를 읽어와 객체 생성과 초기화를 수행한다.

* **AnnotationConfigApplicationContext 클래스의 게층도**
  * 맨 위 : **BeanFactory**
    * 객체 생성과 검색에 대한 기능을 정의
  * 중간 : **ApplicationContext**
    * 메시지, 프로필/환경 변수 등을 처리할 수 있는 기능을 추가로 정의
  * 맨 밑
    * **AnnotationConfigApplicationContext** 
      * 자바 애노테이션을 이용한 클래스로부터 객체 설정 정보를 가져온다.
    * **GenericXmlApplicationContext**
      * XML로부터 객체 설정 정보를 가져온다.
    * **GenericGroovyApplicationContext**
      * 그루비 코드를 이용해 설정 정보를 가져온다.

<br>

ApplicationContext(또는 BeanFactory)는 각 구현 클래스의 설정 정보로부터 빈(Bean)이라고 불리는 객체로 만들고 내부에 보관한다. 즉, 빈 객체의 생성, 초기화, 보관, 제거 등을 관리한다. 그래서 이것을 **컨테이너(Container)** 라고도 부른다.

* **스프링 컨테이너**
  * ApplicationContext
  * BeanFactory

<br>

## 2.1. 싱글톤(Singleton) 객체

* **gradle-tutorial3/src/main/java/chap02/Main2.java**

  ```java
  import org.springframework.context.annotation.AnnotationConfigApplicationContext;
  
  public class Main2 {
  
    public static void main(String[] args) {
      AnnotationConfigApplicationContext ctx =
          new AnnotationConfigApplicationContext(AppContext.class);
      
      // 빈 객체를 구해서 각각 g1과 g2 변수에 할당
      Greeter g1 = ctx.getBean("greeter", Greeter.class);
      Greeter g2 = ctx.getBean("greeter", Greeter.class);
     
      System.out.println("(g1 == g2) = " + (g1 == g2));
      ctx.close();
    }
  
  }
  ```

* **실행 결과**

  ```
  4:50:49 오후: Executing task 'Main2.main()'...
  
  > Task :compileJava
  > Task :processResources NO-SOURCE
  > Task :classes
  
  > Task :Main2.main()
  (g1 == g2) = true
  
  BUILD SUCCESSFUL in 0s
  2 actionable tasks: 2 executed
  4:50:50 오후: Task execution finished 'Main2.main()'.
  ```

  * 위의 결과의 의미는 **getBean() 메서드는 같은 객체를 리턴한다는 의미이다.**
  * 별도 설정을 하지 않을 경우 스프링은 한 개의 빈 객체만을 생성한다.
    * 스프링은 기본적으로 한 개의 **@Bean 애노테이션에** 대해 한 개의 빈 객체를 생성한다.

<br>

아래와 같은 설정을 사용하면 두 개의 빈 객체가 생성된다.

```java
@Bean
public Greeter greeter() {
  Greeter g = new Greeter();
  g.setFormat("%s, 안녕하세요!");
  return g;
}

@Bean
public Greeter greeter1() {
  Greeter g = new Greeter();
  g.setFormat("%s, 안녕하세요!");
  return g;
}
```