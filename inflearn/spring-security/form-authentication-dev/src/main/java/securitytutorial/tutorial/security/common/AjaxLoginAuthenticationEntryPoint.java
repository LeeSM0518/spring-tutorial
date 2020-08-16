package securitytutorial.tutorial.security.common;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AjaxLoginAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  // 익명 사용자나 권한이 없는 사용자가 자원에 접근했을 때 호출되는 메소드
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException, ServletException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UnAuthorized");
  }

}
