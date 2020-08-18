package securitytutorial.tutorial.security.service;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;
import securitytutorial.tutorial.domain.entity.AccessIp;
import securitytutorial.tutorial.domain.entity.Resources;
import securitytutorial.tutorial.repository.AccessIpRepository;
import securitytutorial.tutorial.repository.ResourcesRespository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecurityResourceService  {

  private ResourcesRespository resourcesRepository;
  private AccessIpRepository accessIpRepository;

  public SecurityResourceService(ResourcesRespository resourcesRespository, AccessIpRepository accessIpRepository) {
    this.resourcesRepository = resourcesRespository;
    this.accessIpRepository = accessIpRepository;
  }

  // DB로부터 데이터를 가져와서 맵 객체를 만들어서 반환해준다.
  public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getResourceList() {
    LinkedHashMap<RequestMatcher, List<ConfigAttribute>> result = new LinkedHashMap<>();
    // DB에서 모든 리소스를 가져온다.
    List<Resources> resourcesList = resourcesRepository.findAllResources();
    resourcesList.forEach(re -> {
      List<ConfigAttribute> configAttributeList = new ArrayList<>();
      re.getRoleSet().forEach(role -> {
        configAttributeList.add(new SecurityConfig(role.getRoleName()));
      });
        result.put(new AntPathRequestMatcher(re.getResourceName()), configAttributeList);
    });
    return result;
  }

  public LinkedHashMap<String, List<ConfigAttribute>> getMethodResourceList() {
    LinkedHashMap<String, List<ConfigAttribute>> result = new LinkedHashMap<>();
    // DB에서 모든 리소스를 가져온다.
    List<Resources> resourcesList = resourcesRepository.findAllMethodResources();
    resourcesList.forEach(re -> {
      List<ConfigAttribute> configAttributeList = new ArrayList<>();
      re.getRoleSet().forEach(role -> {
        configAttributeList.add(new SecurityConfig(role.getRoleName()));
      });
        result.put(re.getResourceName(), configAttributeList);
    });
    return result;
  }

  public List<String> getAccessIpList() {
    return accessIpRepository.findAll().stream().map(AccessIp::getIpAddress).collect(Collectors.toList());
  }

  public LinkedHashMap<String, List<ConfigAttribute>> getPointcutResourceList() {
    LinkedHashMap<String, List<ConfigAttribute>> result = new LinkedHashMap<>();
    List<Resources> resourcesList = resourcesRepository.findAllPointcutResources();
    resourcesList.forEach(re -> {
      List<ConfigAttribute> configAttributeList = new ArrayList<>();
      re .getRoleSet().forEach(ro -> {
         configAttributeList.add(new SecurityConfig(ro.getRoleName()));
      });
      result.put(re.getResourceName(),configAttributeList);
    });
    return result;
  }

}
