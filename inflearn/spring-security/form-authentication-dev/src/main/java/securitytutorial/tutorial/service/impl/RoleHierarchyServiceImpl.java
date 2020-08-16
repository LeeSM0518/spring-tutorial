package securitytutorial.tutorial.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import securitytutorial.tutorial.domain.entity.RoleHierarchy;
import securitytutorial.tutorial.repository.RoleHierarchyRepository;
import securitytutorial.tutorial.service.RoleHierarchyService;

import java.util.List;

@Service
public class RoleHierarchyServiceImpl implements RoleHierarchyService {

  @Autowired
  private RoleHierarchyRepository roleHierarchyRepository;

  @Override
  @Transactional
  public String findAllHierarchy() {
    List<RoleHierarchy> roleHierarchies = roleHierarchyRepository.findAll();

    StringBuilder roles = new StringBuilder();

    roleHierarchies
        .forEach(roleHierarchy -> {
          if (roleHierarchy.getParentName() != null) {
            roles.append(roleHierarchy.getParentName().getChildName());
            roles.append(" > ");
            roles.append(roleHierarchy.getChildName());
            roles.append("\n");
          }
        });

    return roles.toString();
  }

}
