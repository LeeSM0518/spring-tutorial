package securitytutorial.tutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import securitytutorial.tutorial.domain.entity.AccessIp;

public interface AccessIpRepository extends JpaRepository<AccessIp, Long> {

  AccessIp findByIpAddress(String ipAddress);

}
