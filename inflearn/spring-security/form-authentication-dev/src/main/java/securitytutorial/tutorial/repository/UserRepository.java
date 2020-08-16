package securitytutorial.tutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import securitytutorial.tutorial.domain.entity.Account;

public interface UserRepository extends JpaRepository<Account, Long> {

  Account findByUsername(String username);
  int countByUsername(String username);

}
