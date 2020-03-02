package project;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class PaymentAspect {

  @Pointcut("execution(* project.service..*())")
  public void targetMethod() { }

  @Around("targetMethod()")
  public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
    try {
      System.out.println("결제 시스템 연결중..");
      return joinPoint.proceed();
    } finally {
      System.out.println("결제 완료.");
    }
  }

}
