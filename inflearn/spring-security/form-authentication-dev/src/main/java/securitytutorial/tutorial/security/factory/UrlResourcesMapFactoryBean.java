package securitytutorial.tutorial.security.factory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;
import securitytutorial.tutorial.security.service.SecurityResourceService;

import java.util.LinkedHashMap;
import java.util.List;

// 리소스 빈을 생성한다.
public class UrlResourcesMapFactoryBean implements FactoryBean<LinkedHashMap<RequestMatcher, List<ConfigAttribute>>> {

  private SecurityResourceService securityResourceService;
  // 빈으로 만들것이다.
  // 순서를 보장해서 저장한다.
  private LinkedHashMap<RequestMatcher, List<ConfigAttribute>> resourceMap;

  public void setSecurityResourceService(SecurityResourceService securityResourceService) {
    this.securityResourceService = securityResourceService;
  }

  @Override
  public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getObject() throws Exception {
    if (resourceMap == null) {
      init();
    }
    return resourceMap;
  }

  private void init() {
    resourceMap = securityResourceService.getResourceList();
  }

  @Override
  public Class<?> getObjectType() {
    return LinkedHashMap.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
