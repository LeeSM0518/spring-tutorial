package config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import spring.*;

@Configuration
@ComponentScan(basePackages = {"spring", "spring2"})
public class AppCtx {

  @Bean
  public MemberDao memberDao() {
    System.out.println("설정 클래스에서 생성");
    MemberDao memberDao = new MemberDao();
    memberDao.setName("설정 클래스");
    return memberDao;
  }

  @Bean
  @Qualifier("printer")
  public MemberPrinter memberPrinter1() {
    return new MemberPrinter();
  }

  @Bean
  public ChangePasswordService changePasswordService() {
    return new ChangePasswordService();
  }

  @Bean
  @Qualifier("summaryPrinter")
  public MemberSummaryPrinter memberPrinter2() {
    return new MemberSummaryPrinter();
  }

  @Bean
  public VersionPrinter versionPrinter() {
    VersionPrinter versionPrinter = new VersionPrinter();
    versionPrinter.setMajorVersion(5);
    versionPrinter.setMinorVersion(0);
    return versionPrinter;
  }

}
