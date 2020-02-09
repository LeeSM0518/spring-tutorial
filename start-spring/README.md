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

