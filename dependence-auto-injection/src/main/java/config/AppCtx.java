package config;

import org.springframework.beans.factory.annotation.Qualifier;
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
    return new MemberRegisterService();
  }

  @Bean
  public ChangePasswordService changePasswordService() {
    return new ChangePasswordService();
  }

//  @Bean
//  public MemberPrinter memberPrinter() {
//    return new MemberPrinter();
//  }

  @Bean
  @Qualifier("printer")
  public MemberPrinter memberPrinter1() {
    return new MemberPrinter();
  }

  @Bean
  @Qualifier("summaryPrinter")
  public MemberSummaryPrinter memberPrinter2() {
    return new MemberSummaryPrinter();
  }

  @Bean
  public MemberListPrinter listPrinter() {
    return new MemberListPrinter();
  }

  @Bean
  public MemberInfoPrinter infoPrinter() {
    MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
    infoPrinter.setPrinter(memberPrinter2());
//    infoPrinter.setMemberDao(memberDao());
//    infoPrinter.setPrinter(memberPrinter());
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
