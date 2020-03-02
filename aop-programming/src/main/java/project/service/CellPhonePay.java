package project.service;

public class CellPhonePay implements PaymentService {

  @Override
  public void execute() {
    System.out.println("휴대폰 결제를 진행합니다.");
  }

}
