package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.*;

// @Configuration : 스프링 설정 클래스를 의미한다.
@Configuration
public class AppCtx {

  // @Bean : 해당 메서드가 생성한 객체를 스프링 빈이라고 부른다.
  //  memberDao 라는 이름으로 스프링에 등록된다.
  @Bean
  public MemberDao memberDao() {
    return new MemberDao();
  }

  @Bean
  public MemberRegisterService memberRegisterService() {
    // memberDao() 가 생성한 객체를 생성자를 통해 주입한다.
    return new MemberRegisterService(memberDao());
  }

  @Bean
  public ChangePasswordService changePasswordService() {
    // 의존 객체 주입
    ChangePasswordService passwordService = new ChangePasswordService();
    passwordService.setMemberDao(memberDao());
    return passwordService;
  }

  @Bean
  public MemberPrinter memberPrinter() {
    return new MemberPrinter();
  }

  @Bean
  public MemberListPrinter listPrinter() {
    return new MemberListPrinter(memberDao(), memberPrinter());
  }

  @Bean
  public MemberInfoPrinter infoPrinter() {
    MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
    // 의존 객체 주입
    infoPrinter.setMemberDao(memberDao());
    infoPrinter.setPrinter(memberPrinter());
    return infoPrinter;
  }

  @Bean
  public VersionPrinter versionPrinter() {
    VersionPrinter versionPrinter = new VersionPrinter();
    versionPrinter.setMajorVersion(5);
    versionPrinter.setMinorVersion(0);
    return versionPrinter;
  }

}
