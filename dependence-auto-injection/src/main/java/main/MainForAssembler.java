package main;

import assembler.Assembler;
import spring.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainForAssembler {

  public static void main(String[] args) throws IOException {
    // 콘솔에서 입력받기 위해 System.in 을 이용해서 BufferedReader 생성
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(System.in));
    
    while (true) {
      System.out.println("명령어를 입력하세요.");
      // 한 줄 입력
      String command = reader.readLine();
      
      // 입력한 문자열이 "exit"이면 프로그램 종료
      if (command.equalsIgnoreCase("exit")) {
        System.out.println("종료합니다.");
        break;
      }

      // 입력한 문자열이 "new" 로 시작하면 processNewCommand() 메서드를 찾아 실행
      if (command.startsWith("new")) {
        processNewCommand(command.split(" "));
        continue;
      } 
      // 입력한 문자열이 "change "로 시작하면 processChangeCommand() 메서드를 찾아 실행
      else if (command.startsWith("change ")) {
        processChangeCommand(command.split(" "));
        continue;
      }
      printHelp();
    }
  }

  // Assembler 객체가 생성되면서
  //  필요한 객체를 생성하고 의존을 주입한다.
  private static Assembler assembler = new Assembler();

  // 새로운 회원 정보를 생성
  private static void processNewCommand(String[] arg) {
    if (arg.length != 5) {
      printHelp();
      return;
    }
    // Assembler 객체 사용
    MemberRegisterService regSvc = assembler.getMemberRegisterService();
    RegisterRequest req = new RegisterRequest();
    req.setEmail(arg[1]);
    req.setName(arg[2]);
    req.setPassword(arg[3]);
    req.setConfirmPassword(arg[4]);

    // 입력한 암호 값이 올바른지 확인
    if (!req.isPasswordEqualToConfirmPassword()) {
      System.out.println("암호와 확인이 일치하지 않습니다.\n");
      return;
    }
    // 이미 동일한 이메일을 가진 회원 데이터가
    //  존재하면 에러 메시지 출력
    try {
      regSvc.regist(req);
      System.out.println("등록했습니다.\n");
    } catch (DuplicateMemberDaoException e) {
      System.out.println("이미 존재하는 이메일입니다.\n");
    }
  }


  private static void processChangeCommand(String[] arg) {
    if (arg.length != 4) {
      printHelp();
      return;
    }

    // Assembler 객체 사용
    ChangePasswordService changePasswordService =
        assembler.getChangePasswordService();
    try {
      changePasswordService.changePassword(arg[1], arg[2], arg[3]);
      System.out.println("암호를 변경했습니다.\n");
    } catch (MemberNotFoundException e) {
      System.out.println("존재하지 않는 이메일입니다.\n");
    } catch (WrongPasswordException e) {
      System.out.println("이메일과 암호가 일치하지 않습니다.\n");
    }
  }


  private static void printHelp() {
    System.out.println();
    System.out.println("잘못된 명령입니다. 아래 명령어 사용법을 확인하세요.");
    System.out.println("명령어 사용법:");
    System.out.println("new 이메일 이름 암호 암호확인");
    System.out.println("change 이메일 현재비번 변경비번");
    System.out.println();
  }

}
