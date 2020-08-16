package securitytutorial.tutorial.security.common;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
// WebAuthenticationDetails 를 생성하는 클래스
public class FormAuthenticationDetailsSource implements
    AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {

  @Override
  public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
    return new FormWebAuthenticationDetails(context);
  }

}
