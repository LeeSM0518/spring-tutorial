package aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import java.util.Arrays;

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
