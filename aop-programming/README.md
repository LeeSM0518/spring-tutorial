# Chapter 07. AOP 프로그래밍

# 1. 프로젝트 준비

* **aspectjweaver 의존 추가**

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

  * aspectjweaver 라는 AOP 모듈 의존을 추가한다.
  * spring-aop 모듈은 spring-context 모듈을 의존 대상에 추가하면 spring-aop 모듈도 함께 의존 대상에 포함된다.
  * **aspectjweaver 모듈은** AOP를 설정하는데 필요한 애노테이션을 제공하므로 의존을 추가해줘야 한다.

<br>

factorial 예시를 작성해보자.

* **Calculator.java**

  ```java
  public interface Calculator {
  
    public long factorial(long num);
  
  }
  ```

* **ImpeCalculator.java**

  ```java
  public class ImpeCalculator implements Calculator {
  
    @Override
    public long factorial(long num) {
      long result = 1;
      for (long i = 1; i <= num; i++) {
        result *= i;
      }
      return result;
    }
  
  }
  ```

  * for문을 이용해서 계승 값을 구한다.

* **RecCalculator.java**

  ```java
  public class RecCalculator implements Calculator {
  
    @Override
    public long factorial(long num) {
      if (num == 0)
        return 1;
      else
        return num * factorial(num - 1);
    }
  
  }
  ```

  * 재귀호출을 이용해서 계승 값을 구한다.

<br>

# 2. 프록시와 AOP

앞에서 구현한 계승 구현 클래스의 실행 시간을 출력시키기 위해 메서드의 시작과 끝에서 시간을 구하고 이 두 시간의 차이를 출력해보자.

* **ImpeCalculator.java**

  ```java
  public class ImpeCalculator implements Calculator {
  
    @Override
    public long factorial(long num) {
      long start = System.currentTimeMillis();
      long result = 1;
      for (long i = 1; i <= num; i++) {
        result *= i;
      }
      long end = System.currentTimeMillis();
      System.out.printf("ImpeCalculator.factorial(%d) 실행 시간 = %d\n",
          num, (end - start));
      return result;
    }
  
  }
  ```

* **RecCalculator.java**

  ```java
  public class RecCalculator implements Calculator {
  
    @Override
    public long factorial(long num) {
      long start = System.currentTimeMillis();
      try {
        if (num == 0)
          return 1;
        else
          return num * factorial(num - 1);
      } finally {
        long end = System.currentTimeMillis();
        System.out.printf("RecCalculator.factorial(%d) 실행 시간 = %d\n",
            num, (end - start));
      }
    }
  
  }
  ```

  * ImpeCalculator는 출력하기 쉽지만 RecCalculator 클래스는 factorial() 메서드가 재귀 호출로 구현되어있기 때문에 복잡해진다.

* **Main.java**

  ```java
  public class Main {
  
    public static void main(String[] args) {
      Calculator calculator = new ImpeCalculator();
      calculator.factorial(3);
  
      calculator = new RecCalculator();
      calculator.factorial(3);
    }
  
  }
  ```

* **실행 결과**

  ```
  ImpeCalculator.factorial(3) 실행 시간 = 0
  RecCalculator.factorial(0) 실행 시간 = 0
  RecCalculator.factorial(1) 실행 시간 = 0
  RecCalculator.factorial(2) 실행 시간 = 0
  RecCalculator.factorial(3) 실행 시간 = 1
  ```

<br>

위와 같은 실행 결과는 불필요한 출력이 존재한다. 그래서 실행 전후에 값을 구해보자.

* **Main.java**

  ```java
  public class Main {
  
    public static void main(String[] args) {
      Calculator calculator = new ImpeCalculator();
      long start = System.currentTimeMillis();
      calculator.factorial(3);
      long end = System.currentTimeMillis();
      System.out.printf("ImpeCalculator.factorial(3) 실행 시간 = %d\n", (end - start));
  
      calculator = new RecCalculator();
      start = System.currentTimeMillis();
      calculator.factorial(3);
      end = System.currentTimeMillis();
      System.out.printf("RecCalculator.factorial(3) 실행 시간 = %d\n", (end - start));
    }
  
  }
  ```

  * 위의 코드도 문제가 있다. 실행 시간을 밀리초가 아닌 나노초 단위로 바꿔야 한다면 밀리초를 구하던 코드를 모두 변경해야 한다.

<br>

위와 같은 문제들을 위해 기존 코드를 수정하지 않고 코드 중복도 피할 수 있도록 하는 프록시 객체를 사용해보자.

* **ExeTimeCalculator.java**

  ```java
  public class ExeTimeCalculator implements Calculator {
    
    private Calculator delegate;
  
    public ExeTimeCalculator(Calculator delegate) {
      this.delegate = delegate;
    }
  
    @Override
    public long factorial(long num) {
      long start = System.nanoTime();
      long result = delegate.factorial(num);
      long end = System.nanoTime();
      System.out.printf("%s.factorial(%d) 실행 시간 = %d\n",
          delegate.getClass().getSimpleName(), num, (end - start));
      return result;
    }
    
  }
  ```

  * 생성자를 통해 다른 Calculator 객체를 전달받아 delegate 필드에 할당한다.
  * delegate.factorial() 를 실행하기 전후에 현재 시간을 구해 차이를 출력한다.

* **Main.java**

  ```java
  public class Main {
  
    public static void main(String[] args) {
      ExeTimeCalculator calculator = 
        new ExeTimeCalculator(new ImpeCalculator());
     	long result = calculator.factorial(3);
    }
  
  }
  ```

* **ExeTimeCalculator의 실행 흐름**

  ![image](https://user-images.githubusercontent.com/43431081/75109723-71431d00-5669-11ea-8e67-568cb8529b6d.png)

* **실행 결과**

  ```
  ImpeCalculator.factorial(3) 실행 시간 = 3166
  ```

<br>

* **MainProxy.java**

  ```java
  public class MainProxy {
  
    public static void main(String[] args) {
      ExeTimeCalculator cal1 = new ExeTimeCalculator(new ImpeCalculator());
      System.out.println(cal1.factorial(20));
  
      ExeTimeCalculator cal2 = new ExeTimeCalculator(new RecCalculator());
      System.out.println(cal2.factorial(20));
    }
  
  }
  ```

* **실행 결과**

  ```
  ImpeCalculator.factorial(20) 실행 시간 = 3142
  2432902008176640000
  RecCalculator.factorial(20) 실행 시간 = 4226
  2432902008176640000
  ```

  * 기존 코드를 변경하지 않고 실행 시간을 출력할 수 있게 되었다.
  * 실행 시간을 구하는 코드의 중복을 제거했다.

<br>

위와 같은 수행이 가능한 이유

* factorial() 기능 자체를 직접 구현하기보다는 **다른 객체에 factorial()의 실행을 위임한다.**
* 계산 기능 외에 **다른 부가적인 기능을 실행한다**

이렇게 **핵심 기능의 실행은 다른 객체에 위임하고 부가적인 기능을 제공하는 객체를 프록시(proxy)라고** 부른다.

<br>

프록시의 특징은 **핵심 기능은 구현하지 않는다는 점이다.** 프록시는 핵심 기능을 구현하지 않는 대신 **여러 객체에 공통으로 적용할 수 있는 기능을 구현한다.**

이렇게 **공통 기능 구현과 핵심 기능 구현을 분리하는 것이** AOP의 핵심이다.

<br>

## 2.1. AOP

AOP는 Aspect Oriented Programming의 약자로, **여러 객체에 공통으로 적용할 수 있는 기능을 분리해서 재사용성을 높여주는** 프로그래밍 기법이다.

AOP는 **핵심 기능과 공통 기능의 구현을 분리함으로써** 핵심 기능을 구현한 코드의 수정 없이 공통 기능을 적용할 수 있게 만들어 준다.

> Aspect Oriented Programming: **'기능(관심) 지향 프로그래밍'**

<br>

AOP의 기본 개념은 핵심 기능에 공통 기능을 삽입하는 것이다. 즉 **핵심 기능의 코드를 수정하지 않으면서 공통 기능의 구현을 추가하는 것이** AOP 이다.

<br>

* **핵심 기능에 공통 기능을 삽입하는 방법**

  1. 컴파일 시점에 코드에 공통 기능을 삽입하는 방법

  2. 클래스 로딩 시점에 바이트 코드에 공통 기능을 삽입하는 방법

  3. 런타임에 프록시 객체를 생성해서 공통 기능을 삽입하는 방법

> 1,2 방식은 스프링 AOP에서는 지원하지 않으며 AspectJ와 같이 **AOP 전용 도구를 사용해서 적용한다.**
>
> 3 방식은 스프링이 제공하는 프록시를 이용한 방식이다.
>
> 프록시 방식은 중간에 프록시 객체를 생성하고 실제 객체의 기능을 실행하기 **전, 후에 공통 기능을 호출한다.**

<br>

* **프록시 기반의 AOP**

  ![image](https://user-images.githubusercontent.com/43431081/75109761-fb8b8100-5669-11ea-926c-03a64e5d80f1.png)

  * 스프링 AOP는 프록시 객체를 자동으로 만들어준다.
  * 즉, 상위 타입의 인터페이스를 상속받은 프록시 클래스를 직접 구현할 필요가 없다.
  * 단지 공통 기능을 구현한 클래스만 알맞게 구현하면 된다.

<br>

* **AOP 주요 용어**

| 용어      | 의미                                                         |
| --------- | ------------------------------------------------------------ |
| Advice    | 언제 공통 관심 기능을 핵심 로직에 적용할 지를 정의하고 있다. |
| Joinpoint | Advice를 적용 가능한 지점을 의미한다. 메서드 호출, 필드 값 변경 등. 스프링은 메서드 호출에 대한 Joinpoint만 지원한다. |
| Pointcut  | Joinpoint의 부분 집합으로서 실제 Advice가 적용되는 Joinpoint를 나타낸다. |
| Weaving   | Advice를 핵심 로직 코드에 적용하는 것                        |
| Aspect    | 여러 객체에 공통으로 적용되는 기능                           |

<br>

## 2.2. Advice의 종류

스프링은 프록시를 이용해서 메서드 호출 시점에 Aspect를 적용한다.

* **구현 가능한 Advice의 종류**

| 종류                   | 설명                                                         |
| ---------------------- | ------------------------------------------------------------ |
| Before Advice          | 대상 객체의 메서드 호출 전에 공통 기능을 실행                |
| After Returning Advice | 대상 객체의 메서드가 익셉션 없이 실행된 이후에 공통 기능을 실행 |
| After Throwing Advice  | 대상 객체의 메서드를 실행하는 도중 익셉션이 발생한 경우에 공통 기능을 실행 |
| After Advice           | 익셉션 발생 여부에 상관없이 메서드 실행 후 공통 기능 실행    |
| Around Advice          | 메서드 실행 전, 후 또는 익셉션 발생 시점에 공통 기능 실행    |

이중에 널리 사용되는 것은 **Around Advice** 이다. 왜냐하면 대상 객체의 메서드를 실행 하기 전/후, 익셉션 발생 시점 등 다양한 시점에 원하는 기능을 삽입할 수 있기 때문이다.

<br>

# 3. 스프링 AOP 구현

* **구현 절차**
  * Aspect로 사용할 클래스에 **@Aspect 애노테이션을** 붙인다.
  * **@Pointcut 애노테이션으로** 공통 기능을 적용할 Pointcut을 정의한다.
  * 공통 기능을 구현한 메서드에 **@Around 애노테이션을** 적용한다.

<br>

## 3.1. @Aspect, @Pointcut, @Around를 이용한 AOP 구현

개발자는 공통 기능을 제공하는 **Aspect 구현 클래스를 만들고** 자바 설정을 이용해서 Aspect를 어디에 적용할지 설정하면 된다. Aspect는 **@Aspect 애노테이션을** 이용해서 구현한다. 프록시는 스프링 프레임워크가 알아서 만들어준다.

<ber>

* **aspect/ExeTimeAspect.java**

  ```java
  @Aspect
  public class ExeTimeAspect {
    
    @Pointcut("execution(public * chap07..*(..))")
    public void publicTarget() {
    }
    
    @Around("publicTarget()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
      long start = System.nanoTime();
      try {
        return joinPoint.proceed();
      } finally {
        long finish = System.nanoTime();
        Signature sig = joinPoint.getSignature();
        System.out.printf("%s.%s(%s) 실행 시간 : %d ns\n",
            joinPoint.getTarget().getClass().getSimpleName(),
            sig.getName(), Arrays.toString(joinPoint.getArgs()),
            (finish - start));
      }
    }
    
  }
  ```

  * @Aspect 애노테이션을 적용한 클래스는 Advice와 Pointcout을 함께 제공한다.
  * **@Pointcut** : 공통 기능을 적용할 대상을 설정한다.
    * **"execution(public * chap07..*(..))"** : chap07 패키지와 그 하위 패키지에 위치한 타입의 public 메서드를 Pointcut으로 설정한다.
  * **@Around** : Around Advice를 설정한다.
    * **"publicTarget()"** : publicTarget() 메서드에 정의한 Pointcut에 공통 기능을 적용한다는 것을 의미한다.
    * publicTarget() 메서드는 명시자에 해당하는 패키지에 속한 **빈 객체의 public 메서드에 @Around가 붙은 measure() 메서드를 적용한다.**
  * **measure() 메서드의 ProceedingJoinPoint 타입 파라미터** : 프록시 대상 객체의 메서드를 호출할 때 사용한다. proceed() 메서드를 사용해서 실제 대상 객체의 메서드를 호출한다.
  * **getSignature(), getTarget(), getArgs()** : 호출한 메서드의 시그니처, 대상 객체, 인자 목록을 구하는데 사용한다.

<br>

* **config/AppCtx.java**

  ```java
  @Configuration
  @EnableAspectJAutoProxy
  public class AppCtx {
    
    @Bean
    public ExeTimeAspect exeTimeAspect() {
      return new ExeTimeAspect();
    }
    
    @Bean
    public Calculator calculator() {
      return new RecCalculator();
    }
    
  }
  ```

  * **@EnableAspectJAutoProxy** : 스프링은 @Aspect 애노테이션이 붙은 빈 객체를 찾아서 빈 객체의 @Pointcut 설정과 @Around 설정을 사용한다.
  * Calculator 타입이 chap07 패키지에 속하므로 **calculator 빈에 ExeTimeAspect 클래스에 정의한 공통 기능인 measure()을 적용한다.**

<br>

> **@Enable 류 애노테이션**
>
> 스프링은 Enable로 시작하는 다양한 애노테이션을 제공한다. 
>
> @Enable로 시작하는 애노테이션은 관련 기능을 적용하는데 필요한 다양한 스프링 설정을 대신 처리한다.
>
> **ex)** 웹 개발과 관련된 @EnableWebMvc 애노테이션 역시 웹 개발과 관련된 다양한 설정을 등록한다.

<br>

공통 기능이 적용되었는지 확인해보자.

* **main/MainAspect.java**

  ```java
  public class MainAspect {
  
    public static void main(String[] args) {
      AnnotationConfigApplicationContext ctx =
          new AnnotationConfigApplicationContext(AppCtx.class);
  
      Calculator cal = ctx.getBean("calculator", Calculator.class);
      long fiveFact = cal.factorial(5);
      System.out.println("cal.factorial(5) = " + fiveFact);
      System.out.println(cal.getClass().getName());
      ctx.close();
    }
  
  }
  ```

* **실행 결과**

  ```
  RecCalculator.factorial([5]) 실행 시간 : 33468 ns
  cal.factorial(5) = 120
  com.sun.proxy.$Proxy21
  ```

  * 결과의 첫 번째 줄은 ExeTimeAspect 클래스의 measure() 메서드가 출력한 것이다.
  * 결과의 마지막 줄을 보면 calculator 타입이 RecCalculator 클래스가 아닌 $Proxy21 이다.
  * 이 타입은 스프링이 생성한 프록시 타입이다.

* **실행되는 과정**

  ![image](https://user-images.githubusercontent.com/43431081/75110636-e3b8fa80-5673-11ea-8b62-8a89d91800d7.png)

<br>

## 3.2. ProceedingJoinPoint의 메서드

Around Advice에서 사용할 공통 기능 메서드는 대부분 **파라미터로 전달받은 ProceedingJoinPoint의 Proceed()** 메서드만 호출하면 된다

```java
public class ExeTimeAspect {
  public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.nanoTime();
    try {
      Object result = joinPoint.proceed();
      return result;
    } finally {
      ... 
    }
  }
}
```

* 호출되는 대상 객체에 대한 정보, 실행되는 메서드에 대한 정보, 메서드를 호출할 때 전달된 인자에 대한 정보에 접근할 수 있도록 ProceedingJoinPoint 인터페이스는 다음 메서드를 제공한다.
  * **Signature getSignature()** : 호출되는 메서드에 대한 정보를 구한다.
  * **Object getTarget()** : 대상 객체를 구한다.
  * **Object[ ] getArgs()** : 파라미터 목록을 구한다.
* org.aspect.lang.Signature 인터페이스가 제공하는 메서드
  * **String getName()** : 호출되는 메서드의 이름을 구한다.
  * **String toLongString()** : 호출되는 메서드를 완전하게 표현한 문장을 구한다.
  * **String toShortString()** : 호출되는 메서드를 축약해서 표현한 문장을 구한다.

<br>

# 4. 프록시 생성 방식

MainAspect 클래스를 수정해보자.

* **main/MainAspect.java**

  ```java
  public class MainAspect {
  
    public static void main(String[] args) {
      AnnotationConfigApplicationContext ctx =
          new AnnotationConfigApplicationContext(AppCtx.class);
  
  //    Calculator cal = ctx.getBean("calculator", Calculator.class);
      RecCalculator cal = ctx.getBean("calculator", RecCalculator.class);
      long fiveFact = cal.factorial(5);
      System.out.println("cal.factorial(5) = " + fiveFact);
      System.out.println(cal.getClass().getName());
      ctx.close();
    }
  
  }
  ```

* **실행 결과**

  ```
  Exception in thread "main" org.springframework.beans.factory.BeanNotOfRequiredTypeException: Bean named 'calculator' is expected to be of type 'chap07.RecCalculator' but was actually of type 'com.sun.proxy.$Proxy21'
  ```

  * RecCalculator 타입을 기대했는데 실제 타입은 $Proxy21 이라는 에러 메세지이다.

  * $Proxy21 클래스는 RecCalculator 클래스가 상속받은 Calculator 인터페이스를 상속받게 된다.

    ![image](https://user-images.githubusercontent.com/43431081/75111004-617f0500-5678-11ea-9849-af1ce63f6e4f.png)

  * 스프링은 AOP를 위한 프록시 객체를 생성할 때 실제 생성할 빈 객체가 **인터페이스를 상속하면 인터페이스를 이용해서 프록시를 생성한다.**

    ```java
    // 설정 클래스
    // AOP 적용시 RecCalculator가 상속받은 Calculator 인터페이스를 이용해서 프록시 생성
    @Bean
    public Calculator calculator() {
      return new RecCalculator();
    }
    
    // 자바ㅋ 코드
    // "calculator" 빈의 실제 타입은 Calculator를 상속한 프록시 타입이므로
    // RecCaclulator로 타입 변환을 할 수 없기 때문에 익셉션 발생
    RecCalculator cal = ctx.getBean("calculator", RecCalculator.class);
    ```

<br>

빈 객체가 인터페이스를 상속할 때 **인터페이스가 아닌 클래스를 이용해서 프록시를 생성하고 싶다면** 아래와 같은 속성을 이용하면 된다.

```java
@Configuration
@EnableAspectAutoProxy(proxyTargetClass = true)
public class AppCtx { ...
```

* **@EnableAspectAutoProxy(proxyTargetClass = true)** : 인터페이스가 아닌 자바 클래스를 상속받아 프록시를 생성한다.

```java
@Configuration
@EnableAspectAutoProxy(proxyTargetClass = true)
public class AppCtx {
  ...
}

// 자바 코드, "calculator" 프록시의 실제 타입은 RecCalculator를 상속받았으므로
// RecCalculator로 타입 변환 가능
RecCalculator cal = ctx.getBean("calculator", RecCalculator.class);
```

<br>

## 4.1. execution 명시자 표현식

Aspect를 적용할 위치를 지정할 때 사용한 Pointcut 설정을 보면 **execution 명시자를 사용했다.**

```java
@Pointcut("execution(public * chap07..*(..))")
private void publicTarget() {
}
```

execution 명시자는 **Advice를 적용할 메서드를 지정할 때 사용한다.**

* **기본 형식**

  ```java
  execution(수식어패턴? 리턴타입패턴 클래스이름패턴?메서드이름패턴(파라미터패턴))
  ```

  * **수식어패턴** : public
  * **리턴타입패턴** : 리턴 타입을 명시
  * **클래스이름패턴/메서드이름패턴** : 클래스 이름 및 메서드 이름을 명시
  * **파라미터패턴** : 매칭될 파라미터에 대해서 명시
  * 각 패턴은 '*' 을 이용해서 모든 값을 표현할 수 있다.
  * '..' 을 이용하여 0개 이상이라는 의미를 표현할 수 있다. 

<br>

* **execution 명시자 예시**

| 예                              | 설명                                                         |
| ------------------------------- | ------------------------------------------------------------ |
| execution(public void set*(..)) | 리턴 타입: void<br />메서드 이름: set으로 시작<br />파라미터: 0개 이상 |
| execution(* chap07.\*.\*())     |                                                              |
|                                 |                                                              |
|                                 |                                                              |
|                                 |                                                              |
|                                 |                                                              |
|                                 |                                                              |

