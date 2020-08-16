package securitytutorial.tutorial.security.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import javax.security.auth.Subject;
import java.util.Collection;

public class AjaxAuthenticationToken extends AbstractAuthenticationToken {

  private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  private final Object principal;
  private Object credentials;

  // 인증을 받기 전에 사용자가 입력한 아이디, 패스워드를 담는다.
  public AjaxAuthenticationToken(Object principal, Object credentials) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(false);
  }

  // 인증된 이후의 결과를 담는다.
  public AjaxAuthenticationToken(Object principal, Object credentials,
                                             Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(true); // must use super, as we override
  }

  public Object getCredentials() {
    return this.credentials;
  }

  public Object getPrincipal() {
    return this.principal;
  }

  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    if (isAuthenticated) {
      throw new IllegalArgumentException(
          "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
    }

    super.setAuthenticated(false);
  }

  @Override
  public void eraseCredentials() {
    super.eraseCredentials();
    credentials = null;
  }

}
