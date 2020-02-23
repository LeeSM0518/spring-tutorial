# Chapter 06. 빈 라이프사이클과 범위

* 이 장에서 다룰 내용
  * 컨테이너 초기화와 종료
  * 빈 객체의 라이프사이클
  * 싱글톤과 프로토타입 범위

<br>

# 1. 컨테이너 초기화와 종료

스프링 컨테이너는 초기화와 종료라는 라이프사이클을 갖는다.

* **Main 코드의 일부**

  ```java
  // 1. 컨테이너 초기화
  AnnotationConfigApplicationContext ctx =
    new AnnotationConfigApplicationContext(AppContext.class);
  
  // 2. 컨테이너에서 빈 객체를 구해서 사용
  Greeter g = ctx.getBean("greeter", Greeter.class);
  String msg = g.greet("스프링");
  System.out.println(msg);
  
  // 3. 컨테이너 종료
  ctx.close();
  ```

  * AnnotationContextApplicationContext의 생성자를 이용해서 컨텍스트 객체를 생성하는데 이 시점에 **스프링 컨테이너를 초기화한다.**
    * 스프링 컨테이너는 설정 클래스에서 정보를 읽어와 알맞은 **빈 객체를 생성하고 각 빈을 연결(의존 주입)하는** 작업을 수행한다.
  * 컨테이너 초기화가 완료되면 **컨테이너를 사용할 수 있다.**
    * getBean() 메서드 호출이 가능하다.
  * 컨테이너 사용이 끝나면 **컨테이너를 종료한다.**
    * close() 메서드가 컨테이너를 종료시킨다.
    * close() 메서드는 AbstractApplicationContext 클래스에 정의되어 있다.
  * 컨테이너를 초기화하고 종료할 때의 작업들
    * 컨테이너 초기화 => **빈 객체의 생성, 의존 주입, 초기화**
    * 컨테이너 종료 => **빈 객체의 소멸**

  <br>

# 2. 스프링 빈 객체의 라이프사이클

스프링 컨테이너는 빈 객체의 라이프사이클을 관리한다.

* **빈 객체의 라이프사이클**

  ![image](https://user-images.githubusercontent.com/43431081/75103886-db959680-5644-11ea-8663-f2110b698f70.png)

  * 스프링 컨테이너를 초기화할 때 스프링 컨테이너는 가장 먼저 빈 객체를 생성하고 의존을 설정한다.
  * 모든 의존 설정이 완료되면 빈 객체의 초기화를 수행한다.
  * 스프링 컨테이너를 종료하면 스프링 컨테이너는 빈 객체의 소멸을 처리한다.

<br>

## 2.1. 빈 객체의 초기화와 소멸 : 스프링 인터페이스

스프링 컨테이너는 빈 객체를 초기화하고 소멸하기 위해 빈 객체의 지정한 메서드를 호출한다.

* **스프링의 생성과 소멸 인터페이스**

  * org.springframework.beans.factory.InitializingBean
  * org.springframework.beans.factory.DisposableBean

  ```java
  public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
  }
  
  public interface DisposableBean {
    void destroy() throws Exception;
  }
  ```

* 빈 객체가 InitializingBean 인터페이스를 구현하면 스프링 컨테이너는 **초기화 과정에서 빈 객체의 afterPropertiesSet() 메서드를 실행한다.**

* 스프링 컨테이너는 빈 객체가 DisposableBean 인터페이스를 구현한 경우 **소멸 과정에서 빈 객체의 destroy() 메서드를 실행한다.**

<br>

간단한 예시를 통해 실제로 초기화 메서드와 소멸 메서드가 언제 실행되는지 확인해보자.

* **spring/Client.java**

  ```java
  public class Client implements InitializingBean, DisposableBean {
  
    private String host;
  
    public void setHost(String host) {
      this.host = host;
    }
  
    @Override
    public void afterPropertiesSet() throws Exception {
      System.out.println("Client.afterPropertiesSet() 실행");
    }
  
    public void send() {
      System.out.println("Client.send() to " + host);
    }
  
    @Override
    public void destroy() throws Exception {
      System.out.println("Client.destroy() 실행");
    }
  
  }
  ```

* **config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
  
    @Bean
    public Client client() {
      Client client = new Client();
      client.setHost("host");
      return client;
    }
  
  }
  ```

* **main/Main.java**

  ```java
  public class Main {
    
    public static void main(String[] args) throws IOException {
      AbstractApplicationContext ctx =
          new AnnotationConfigApplicationContext(AppCtx.class);
  
      Client client = ctx.getBean(Client.class);
      client.send();
  
      ctx.close();
    }
  
  }
  ```

* **실행 결과**

  ```
  Client.afterPropertiesSet() 실행
  Client.send() to host
  Client.destroy() 실행
  ```

  1. afterPropertiesSet() 메서드를 실행. 즉, 스프링 컨테이너는 빈 객체를 생성을 마무리한 뒤에 초기화 메서드를 실행한다.
  2. destroy() 메서드를 실행. 스프링 컨테이너를 종료하면 호출된다는 것을 알 수 있다.

<br>

## 2.2. 빈 객체의 초기화와 소멸 : 커스텀 메서드

직접 구현한 클래스가 아닌 외부에서 제공받은 클래스를 스프링 빈 객체로 설정하고 싶을 때가 있다. 이 경우에는 스프링 설정에서 직접 메서드를 지정할 수 있다.

방법은 **@Bean 태그에서 initMethod 속성과 destroyMethod 속성을 사용해서** 초기화 메서드와 소멸 메서드의 이름을 지정하면 된다.

* **spring/Client2.java**

  ```java
  public class Client2 {
  
    private String host;
  
    public void setHost(String host) {
      this.host = host;
    }
    
    public void connect() {
      System.out.println("Client2.connect() 실행");
    }
    
    public void send() {
      System.out.println("Client2.send() to " + host);
    }
    
    public void close() {
      System.out.println("Client2.close() 실행");
    }
    
  }
  ```

  * Client2 클래스를 빈으로 사용하려면 초기화 과정에서 connect() 메서드를 실행하고 소멸 과정에서 close() 메서드를 실행해야 한다.

* **config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
  
    ...
    
    @Bean(initMethod ="connect", destroyMethod = "close")
    public Client2 client2() {
      Client2 client2 = new Client2();
      client2.setHost("host");
      return client2;
    }
  
  }
  ```

  * @Bean 애노테이션의 **initMethod 속성과 destroyMethod 속성에** 초기화와 소멸 과정에서 사용할 메서드 이름을 지정해주면 된다.

<br>

빈 설정 메서드에서 직접 초기화를 수행해도 된다.

* **config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
  
  	...
  
    @Bean(destroyMethod = "close")
    public Client2 client2() {
      Client2 client2 = new Client2();
      client2.setHost("host");
      client2.connect();
      return client2;
    }
  
  }
  ```

  * 주의할 점은 초기화 메서드가 두 번 불리지 않도록 하는 것이다.

* **잘못 초기화한 예시**

  ```java
  @Bean
  public Client client() {
    Client client = new Client();
    client.setHost("host");
    client.afterPropertiesSet();
    return client;
  }
  ```

  * 스프링 컨테이너는 빈 객체를 생성 이후에 afterPropertiesSet() 메서드를 실행한다. 즉 afterPropertiesSet() 메서드가 두 번 호출되는 것이다.

<br>

# 3. 빈 객체의 생성과 관리 범위

스프링 컨테이너는 보통 빈 객체를 한 개만 생성한다.

* **예시**

  ```java
  Client client1 = ctx.getBean("client", Client.class);
  Client client2 = ctx.getBean("client", Client.class);
  // client1 == client2 => true
  ```

  * 한 식별자에 대해 한 개의 객체만 존재하는 빈은 **싱글톤(singleton) 범위(scope)를 갖는다.**
  * 별도 설정을 하지 않으면 빈은 싱글톤 범위를 갖는다.

<br>

사용 빈도가 낮긴 하지만 프로토타입 범위의 빈을 설정할 수도 있다. 프로토타입 빈은 구할 때마다 매번 새로운 객체를 생성한다.

* **예시**

  ```java
  // client 빈의 범위가 프로토타입일 경우, 매번 새로운 객체 생성
  Client client1 = ctx.getBean("client", Client.class);
  Client client2 = ctx.getBean("client", Client.class);
  // client1 != client2 => true
  ```

<br>

특정 빈을 프로토타입 범위로 지정하려면 **"prototype"을** 갖는 @Scope 애노테이션을 @Bean 애노테이션과 함께 사용하면 된다.

* **spring/AppCtxWithPrototype.java**

  ```java
  @Configuration
  public class AppCtxWithPrototype {
    
    @Bean
    @Scope("prototype")
    public Client client() {
      Client client = new Client();
      client.setHost("host");
      return client;
    }
    
  }
  ```

  * 프로토타입 범위를 갖는 빈은 완전한 라이프사이클을 따르지 않는다.
  * 프로토타입 범위의 빈을 사용할 때에는 빈 객체의 소멸 처리를 코드에서 직접 해야 한다.

<br>

싱글톤 범위를 명시적으로 지정하고 싶다면 @Scope 애노테이션 값으로 "singleton"을 주면 된다.

* **spring/AppCtxWithPrototype.java**

  ```java
  @Configuration
  public class AppCtxWithPrototype {
  
    @Bean(initMethod = "connect", destroyMethod = "close")
    @Scope("singleton")
    public Client2 client2() {
      Client2 client2 = new Client2();
      client2.setHost("host");
      return client2;
    }
  
  }
  ```

