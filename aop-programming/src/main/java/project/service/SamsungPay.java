package project.service;

public class SamsungPay implements PaymentService {

  @Override
  public void execute() {
    System.out.println("삼성 페이 결제를 진행합니다.");
  }

}
