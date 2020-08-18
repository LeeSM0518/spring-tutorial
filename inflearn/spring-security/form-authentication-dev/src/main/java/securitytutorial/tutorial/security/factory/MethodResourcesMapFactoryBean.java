package securitytutorial.tutorial.security.factory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.RequestMatcher;
import securitytutorial.tutorial.security.service.SecurityResourceService;

import java.util.LinkedHashMap;
import java.util.List;

public class MethodResourcesMapFactoryBean implements FactoryBean<LinkedHashMap<String, List<ConfigAttribute>>> {

  private SecurityResourceService securityResourceService;
  // 빈으로 만들것이다.
  // 순서를 보장해서 저장한다.
  private LinkedHashMap<String, List<ConfigAttribute>> resourceMap;
  private String resourceType;

  public MethodResourcesMapFactoryBean(SecurityResourceService securityResourceService, String resourceType) {
    this.securityResourceService = securityResourceService;
    this.resourceType = resourceType;
  }

  @Override
  public LinkedHashMap<String, List<ConfigAttribute>> getObject() throws Exception {
    if (resourceMap == null) {
      init();
    }
    return resourceMap;
  }

  private void init() {
    if ("method".equals(resourceType)) {
    resourceMap = securityResourceService.getMethodResourceList();
    } else if("pointcut".equals(resourceType)) {
      resourceMap = securityResourceService.getPointcutResourceList();
    }
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
