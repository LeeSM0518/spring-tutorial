package aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

@Aspect
public class CacheAspect {

  private Map<Long, Object> cache = new HashMap<>();

  @Around("aspect.ExeTimeAspect.publicTarget()")
  public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
    // 첫 번째 인자를 Long 타입으로 구한다.
    Long num = (Long) joinPoint.getArgs()[0];
    // 위에서 구한 키값이 cache에 존재하면 키에 해당하는 값을 구해서
    //  리턴한다.
    if (cache.containsKey(num)) {
      System.out.printf("CacheAspect: Cache에서 구함[%d]\n", num);
      return cache.get(num);
    }

    // 처음에 구한 키값이 cache에 존재하지 않으면
    //  프록시 대상 객체를 실행한다.
    Object result = joinPoint.proceed();
    // 프록시 대상 객체를 실행한 결과를 cache에 추가
    cache.put(num, result);
    System.out.printf("CacheAspect: Cache에 추가[%d]\n", num);
    // 프록시 대상 객체의 실행 결과를 리턴한다.
    return result;
  }

}
