package securitytutorial.tutorial.service;

import securitytutorial.tutorial.domain.entity.Role;

import java.util.List;

public interface RoleService {

  Role getRole(long id);

  List<Role> getRoles();

  void createRole(Role role);

  void deleteRole(long id);

}
