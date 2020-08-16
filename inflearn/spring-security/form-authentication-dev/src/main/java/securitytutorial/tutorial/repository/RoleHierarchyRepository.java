package securitytutorial.tutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import securitytutorial.tutorial.domain.entity.RoleHierarchy;

public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, Long> {

  RoleHierarchy findByChildName(String roleName);

}
