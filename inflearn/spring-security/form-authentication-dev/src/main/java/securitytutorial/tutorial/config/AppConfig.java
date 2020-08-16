package securitytutorial.tutorial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import securitytutorial.tutorial.repository.AccessIpRepository;
import securitytutorial.tutorial.repository.ResourcesRespository;
import securitytutorial.tutorial.security.service.SecurityResourceService;

@Configuration
public class AppConfig {

  @Bean
  public SecurityResourceService securityResourceService(ResourcesRespository resourcesRespository,
                                                         AccessIpRepository accessIpRepository) {
    return new SecurityResourceService(resourcesRespository, accessIpRepository);
  }

}
