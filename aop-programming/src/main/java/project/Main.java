package project;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import project.service.CellPhonePay;
import project.service.PaymentService;

public class Main {

  public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(ProjectCtx.class);

    PaymentService paymentService = ctx.getBean("paymentCellphoneService", PaymentService.class);
    paymentService.execute();

    paymentService = ctx.getBean("paymentSamsungPayService", PaymentService.class);
    paymentService.execute();
  }

}
