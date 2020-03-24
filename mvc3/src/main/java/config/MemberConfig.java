package config;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import spring.*;

@Configuration
@EnableTransactionManagement
public class MemberConfig {

  @Bean(destroyMethod = "close")
  public DataSource dataSource() {
    // DataSource 객체 생성
    DataSource ds = new DataSource();
    // JDBC 드라이버 클래스 지정.
    ds.setDriverClassName("org.postgresql.Driver");
    // DB 연결할 때 사용할 URL, 계정, 암호 설정
    ds.setUrl("jdbc:postgresql://arjuna.db.elephantsql.com:5432/");
    ds.setUsername("kberhhnn");
    ds.setPassword("HYXtqTXqY_vYfqysat4KIyMeNTfFj7cJ");

    ds.setInitialSize(2);
    ds.setMaxActive(10);
    ds.setTestWhileIdle(true);
    ds.setMinEvictableIdleTimeMillis(60000 * 3);
    ds.setTimeBetweenEvictionRunsMillis(10 * 1000);
    return ds;
  }

  @Bean
  public MemberDao memberDao() {
    return new MemberDao(dataSource());
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    DataSourceTransactionManager tm = new DataSourceTransactionManager();
    tm.setDataSource(dataSource());
    return tm;
  }

  @Bean
  public MemberRegisterService memberRegSvc() {
    return new MemberRegisterService(memberDao());
  }

  @Bean
  public ChangePasswordService changePwdSvc() {
    ChangePasswordService pwdSvc = new ChangePasswordService();
    pwdSvc.setMemberDao(memberDao());
    return pwdSvc;
  }

  @Bean
  public AuthService authService() {
    AuthService authService = new AuthService();
    authService.setMemberDao(memberDao());
    return authService;
  }

}
