package config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.MemberInfoPrinter;
import spring.MemberInfoPrinter2;
import spring.MemberPrinter;

@Configuration
public class AppCtx2 {

  @Bean
  public MemberPrinter printer() {
    return new MemberPrinter();
  }

  @Bean
  @Qualifier("mprinter")
  public MemberPrinter printer2() {
    return new MemberPrinter();
  }

  @Bean
  public MemberInfoPrinter2 infoPrinter() {
    return new MemberInfoPrinter2();
  }

}
