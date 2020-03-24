package config;

import controller.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spring.AuthService;
import spring.ChangePasswordService;
import spring.MemberRegisterService;

@Configuration
public class ControllerConfig {

  @Autowired
  private MemberRegisterService memberRegSvc;
  @Autowired
  private AuthService authService;
  @Autowired
  private ChangePasswordService changePasswordService;

  @Bean
  public ChangePwdController changePwdController() {
    ChangePwdController controller = new ChangePwdController();
    controller.setChangePasswordService(changePasswordService);
    return controller;
  }

  @Bean
  public RegisterController registerController() {
    RegisterController controller = new RegisterController();
    controller.setMemberRegisterService(memberRegSvc);
    return controller;
  }

  @Bean
  public SurveyController surveyController() {
    return new SurveyController();
  }

  @Bean
  public LoginController loginController() {
    LoginController controller = new LoginController();
    controller.setAuthService(authService);
    return controller;
  }

  @Bean
  public LogoutController logoutController() {
    return new LogoutController();
  }

}
