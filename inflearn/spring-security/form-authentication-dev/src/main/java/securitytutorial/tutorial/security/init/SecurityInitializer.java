package securitytutorial.tutorial.security.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.stereotype.Component;
import securitytutorial.tutorial.service.RoleHierarchyService;

@Component
// 어플리케이션이 시작될 때 초기화 작업
public class SecurityInitializer implements ApplicationRunner {

  @Autowired
  private RoleHierarchyService roleHierarchyService;

  @Autowired
  private RoleHierarchyImpl roleHierarchy;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    String allHierarchy = roleHierarchyService.findAllHierarchy();
    roleHierarchy.setHierarchy(allHierarchy);
  }

}
