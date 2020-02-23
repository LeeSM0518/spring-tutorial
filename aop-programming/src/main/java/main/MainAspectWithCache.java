package main;

import chap07.Calculator;
import config.AppCtxWithCache;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainAspectWithCache {

  public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx =
      new AnnotationConfigApplicationContext(AppCtxWithCache.class);

    Calculator calculator = ctx.getBean("calculator", Calculator.class);
    calculator.factorial(7);
    calculator.factorial(7);
    calculator.factorial(5);
    calculator.factorial(5);
    ctx.close();
  }

}
