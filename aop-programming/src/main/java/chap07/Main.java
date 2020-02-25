package chap07;

public class Main {

  public static void main(String[] args) {
//    chap07.Calculator calculator = new chap07.ImpeCalculator();
//    long start = System.currentTimeMillis();
//    calculator.factorial(3);
//    long end = System.currentTimeMillis();
//    System.out.printf("chap07.ImpeCalculator.factorial(3) 실행 시간 = %d\n", (end - start));
//
//    calculator = new chap07.RecCalculator();
//    start = System.currentTimeMillis();
//    calculator.factorial(3);
//    end = System.currentTimeMillis();
//    System.out.printf("chap07.RecCalculator.factorial(3) 실행 시간 = %d\n", (end - start));
//    chap07.ExeTimeCalculator calculator1 = new chap07.ExeTimeCalculator(new chap07.ImpeCalculator());
//    chap07.ExeTimeCalculator calculator2 = new chap07.ExeTimeCalculator(new chap07.RecCalculator());
//    calculator1.factorial(3);
//    calculator2.factorial(3);
    Calculator calculator = new RecCalculator();
    calculator.factorial(5);
//    ExeTimeCalculator calculator =
//        new ExeTimeCalculator(new ImpeCalculator());
//    long result = calculator.factorial(3);
//    System.out.println(result);
  }

}
