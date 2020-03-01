package spring;

public class MemberInfoPrinter {

  private MemberDao memberDao;
  private MemberPrinter printer;

  public void printMemberInfo(String email) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) {
      System.out.println("데이터 없음\n");
      return;
    }
    printer.print(member);
    System.out.println();
  }

  // 의존 주입을 위한 세터 메서드
  public void setMemberDao(MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  // 의존 주입을 위한 세터 메서드
  public void setPrinter(MemberPrinter printer) {
    this.printer = printer;
  }
}
