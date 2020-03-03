package project;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import project.service.CellPhonePay;
import project.service.PaymentService;
import project.service.SamsungPay;

@Configuration
@EnableAspectJAutoProxy
public class ProjectCtx {

  @Bean
  public PaymentAspect paymentAspect() {
    return new PaymentAspect();
  }

  @Bean
  public PaymentService paymentCellphoneService() {
    return new CellPhonePay();
  }

  @Bean
  public PaymentService paymentSamsungPayService() {
    return new SamsungPay();
  }

}
