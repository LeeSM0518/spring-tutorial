package securitytutorial.tutorial.security.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import securitytutorial.tutorial.security.service.AccountContext;
import securitytutorial.tutorial.security.token.AjaxAuthenticationToken;

public class AjaxAuthenticationProvider implements AuthenticationProvider {

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  // 인증에 관련된 모든 검증하는 메서드
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    String username = authentication.getName();
    String password = (String) authentication.getCredentials();

    // ID 검증 (해당 ID로 회원이 존재하는지 확인)
    AccountContext accountContext = (AccountContext) userDetailsService.loadUserByUsername(username);

    // PW 검증 (사용자로부터 입력된 PW, DB로부터 가져온 PW)
    if (!passwordEncoder.matches(password, accountContext.getAccount().getPassword())) {
      throw new BadCredentialsException("BadCredentialsException");
    }

    /**
     * public UsernamePasswordAuthenticationToken(Object principal, Object credentials,
     * 			Collection<? extends GrantedAuthority> authorities)
     * @param principle   : 인증에 사용한 사용자 객체(Account)
     * @param credentials : PW 정보
     * @param authorities : 권한 정보들
     */
    // 인증 객체 생성
    return new AjaxAuthenticationToken(accountContext.getAccount(), null, accountContext.getAuthorities());
  }

  @Override
  // authentication 파라미터의 클래스 타입과 인증 토큰의 타입이 일치하는지 확인
  public boolean supports(Class<?> authentication) {
    return authentication.equals(AjaxAuthenticationToken.class);
  }

}
