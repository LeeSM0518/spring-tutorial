package project;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import project.service.CellPhonePay;
import project.service.PaymentService;

import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(ProjectCtx.class);

    Scanner scanner = new Scanner(System.in);
    PaymentService paymentService = null;

    while (true) {
      System.out.println("========================");
      System.out.println("결제 방식 선택");
      System.out.println("1. 휴대폰 결제");
      System.out.println("2. 삼성 페이 결제");
      System.out.println("3. 종료하기");
      System.out.println("========================");
      String menu = scanner.nextLine();
      switch (menu) {
        case "1":
          paymentService = ctx.getBean("paymentCellphoneService", PaymentService.class);
          paymentService.execute();
          break;
        case "2":
          paymentService = ctx.getBean("paymentSamsungPayService", PaymentService.class);
          paymentService.execute();
          break;
        case "3":
          return;
        default:
          System.out.println("잘못된 입력입니다.");
      }
    }

  }

}
