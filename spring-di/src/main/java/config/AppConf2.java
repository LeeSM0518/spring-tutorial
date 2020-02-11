package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.*;

@Configuration
public class AppConf2 {

  // @Autowired : 스프링의 자동 주입 기능을 위한 것이다.
  //  스프링 설정 클래스의 필드에 @Autowired 애노테이션을 붙이면
  //  해당 타입의 빈을 memberDao 필드에 할당한다.
  //  AppConf1 클래스에 MemberDao 탕비의 빈을 설정했으므로
  //  AppConf2 클래스의 membreDao 필드에는 AppConf1 클래스에서 설정한
  //  빈이 할당된다.
  @Autowired
  private MemberDao memberDao;
  @Autowired
  private MemberPrinter memberPrinter;

  @Bean
  public MemberRegisterService memberRegisterService() {
    return new MemberRegisterService(memberDao);
  }

  @Bean
  public ChangePasswordService changePasswordService() {
    ChangePasswordService changePasswordService = new ChangePasswordService();
    changePasswordService.setMemberDao(memberDao);
    return changePasswordService;
  }

  @Bean
  public MemberListPrinter listPrinter() {
    return new MemberListPrinter(memberDao, memberPrinter);
  }

  @Bean
  public MemberInfoPrinter infoPrinter() {
    MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
    infoPrinter.setMemberDao(memberDao);
    infoPrinter.setPrinter(memberPrinter);
    return infoPrinter;
  }

  @Bean
  public VersionPrinter versionPrinter() {
    VersionPrinter versionPrinter = new VersionPrinter();
    versionPrinter.setMinorVersion(0);
    versionPrinter.setMajorVersion(5);
    return versionPrinter;
  }

}
