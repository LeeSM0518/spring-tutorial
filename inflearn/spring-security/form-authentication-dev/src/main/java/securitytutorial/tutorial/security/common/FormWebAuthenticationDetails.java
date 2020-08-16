package securitytutorial.tutorial.security.common;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

// ID, PW 이외의 추가적인 인증을 처리하기 위한 클래스
public class FormWebAuthenticationDetails extends WebAuthenticationDetails {

  private String secretKey;

  public FormWebAuthenticationDetails(HttpServletRequest request) {
    super(request);
    secretKey = request.getParameter("secret_key");
  }

  public String getSecretKey() {
    return secretKey;
  }

}
