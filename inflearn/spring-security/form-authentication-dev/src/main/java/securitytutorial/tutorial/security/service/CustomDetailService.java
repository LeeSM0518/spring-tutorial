package securitytutorial.tutorial.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import securitytutorial.tutorial.domain.entity.Account;
import securitytutorial.tutorial.domain.entity.Role;
import securitytutorial.tutorial.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("userDetailsService")
public class CustomDetailService implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 해당 아이디의 회원 조회
    Account account = userRepository.findByUsername(username);

    // 일치하는 회원이 없어서 조회가 되지 않는다면,
    if (account == null) {
      // 유저가 없다는 예외 던짐
      throw new UsernameNotFoundException("UsernameNotFoundException");
    }

    Set<String> userRoles = account.getUserRoles()
        .stream()
        .map(userRole -> userRole.getRoleName())
        .collect(Collectors.toSet());

    List<GrantedAuthority> collect = userRoles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

//    // 권한 리스트
//    List<GrantedAuthority> roles = account.getUserRoles()
//        .stream()
//        .map(Role::getRoleName)
//        .map(SimpleGrantedAuthority::new)
//        .collect(Collectors.toList());

    // 인증 객체 생성
    return new AccountContext(account, collect);
  }
}
