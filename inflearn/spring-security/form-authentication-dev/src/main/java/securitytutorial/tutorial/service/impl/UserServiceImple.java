package securitytutorial.tutorial.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import securitytutorial.tutorial.domain.entity.Account;
import securitytutorial.tutorial.repository.UserRepository;
import securitytutorial.tutorial.service.UserService;

@Service("userService")
public class UserServiceImple implements UserService {

  @Autowired
  private UserRepository userRepository;

  @Transactional
  @Override
  public void createUser(Account account) {
    userRepository.save(account);
  }

}
