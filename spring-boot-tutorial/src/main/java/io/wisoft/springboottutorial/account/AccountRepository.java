package io.wisoft.springboottutorial.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> { // <연결객체, primary key 타입>

}
