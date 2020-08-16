package securitytutorial.tutorial.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import securitytutorial.tutorial.domain.entity.Role;
import securitytutorial.tutorial.repository.RoleRepository;
import securitytutorial.tutorial.service.RoleService;

import java.util.List;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

  @Autowired
  private RoleRepository roleRepository;

  @Override
  @Transactional
  public Role getRole(long id) {
    return roleRepository.findById(id).orElse(new Role());
  }

  @Override
  @Transactional
  public List<Role> getRoles() {
    return roleRepository.findAll();
  }

  @Override
  @Transactional
  public void createRole(Role role) {
    roleRepository.save(role);
  }

  @Override
  @Transactional
  public void deleteRole(long id) {
    roleRepository.deleteById(id);
  }

}
