package main;

import config.AppCtx;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spring.ChangePasswordService;
import spring.MemberNotFoundException;
import spring.WrongPasswordException;

public class MainForCPS {

  public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(AppCtx.class);

    ChangePasswordService cps =
        ctx.getBean("changePwdSvc", ChangePasswordService.class);

    try {
      cps.changePassword("nalsm98@test.com", "1234", "1111");
    } catch (MemberNotFoundException e) {
      System.out.println("회원 데이터가 존재하지 않습니다.");
    } catch (WrongPasswordException e) {
      System.out.println("암호가 올바르지 않습니다.");
    }

    ctx.close();
  }

}
