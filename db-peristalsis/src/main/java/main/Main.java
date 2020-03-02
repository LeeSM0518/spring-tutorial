//package main;
//
//import config.AppCtx;
//import org.springframework.context.annotation.AnnotationConfigApplicationContext;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//
//public class Main {
//
//  private static AnnotationConfigApplicationContext ctx = null;
//
//  public static void main(String[] args) {
//    ctx = new AnnotationConfigApplicationContext(AppCtx.class);
//
//    BufferedReader reader =
//        new BufferedReader(new InputStreamReader(System.in));
//
//    while (true) {
//      System.out.println("명령어를 입력하세요: ");
//      String command = reader.readLine();
//      if (command.equalsIgnoreCase("exit")) {
//        System.out.println("종료합니다.");
//        break;
//      }
//      if (command.startsWith("new ")) {
//
//      }
//    }
//  }
//
//  private static void processNewCommand(String[] arg) {
//    if (arg.length != 5) {
//
//    }
//  }
//
//
//}
