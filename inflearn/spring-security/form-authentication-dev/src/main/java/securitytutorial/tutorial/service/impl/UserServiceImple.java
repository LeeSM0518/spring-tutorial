package securitytutorial.tutorial.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import securitytutorial.tutorial.domain.dto.AccountDto;
import securitytutorial.tutorial.domain.entity.Account;
import securitytutorial.tutorial.domain.entity.Role;
import securitytutorial.tutorial.repository.RoleRepository;
import securitytutorial.tutorial.repository.UserRepository;
import securitytutorial.tutorial.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service("userService")
public class UserServiceImple implements UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Transactional
  @Override
  public void createUser(Account account) {
    Role role = roleRepository.findByRoleName("ROLE_USER");
    Set<Role> roles = new HashSet<>();
    roles.add(role);
    account.setUserRoles(roles);
    userRepository.save(account);
  }

  @Override
  @Transactional
  public void modifyUser(AccountDto accountDto) {
    ModelMapper modelMapper = new ModelMapper();
    Account account = modelMapper.map(accountDto, Account.class);

    if (accountDto.getRole() != null) {
      Set<Role> roles = new HashSet<>();
      accountDto.getRole().forEach(role -> {
        Role r = roleRepository.findByRoleName(role);
        roles.add(r);
      });
      account.setUserRoles(roles);
    }
    account.setPassword(passwordEncoder.encode(accountDto.getPassword()));
    userRepository.save(account);
  }

  @Override
  @Transactional
  public List<Account> getUsers() {
    return userRepository.findAll();
  }

  @Override
  @Transactional
  public AccountDto getUser(Long id) {
    Account account = userRepository.findById(id).orElse(new Account());
    ModelMapper modelMapper = new ModelMapper();
    AccountDto accountDto = modelMapper.map(account, AccountDto.class);
    List<String> roles = account.getUserRoles()
        .stream()
        .map(Role::getRoleName)
        .collect(Collectors.toList());
    accountDto.setRole(roles);
    return accountDto;
  }

  @Override
  @Transactional
  public void deleteUser(Long idx) {
    userRepository.deleteById(idx);
  }

  @Override
  @Secured("ROLE_MANAGER")
  public void order() {
    System.out.println("order");
  }

}
