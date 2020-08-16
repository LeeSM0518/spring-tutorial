package securitytutorial.tutorial.security.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
// 사용자가 인증에 성공했을 때 처리해줄 핸들러
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private RequestCache requestCache = new HttpSessionRequestCache();

  private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

    setDefaultTargetUrl("/");

    // 사용자가 인증하기 전에 요청했던 정보
    SavedRequest savedRequest = requestCache.getRequest(request, response);
    if (savedRequest != null) {
      String targetUrl = savedRequest.getRedirectUrl();
      redirectStrategy.sendRedirect(request, response, targetUrl);
    } else {
      redirectStrategy.sendRedirect(request, response, getDefaultTargetUrl());
    }
  }
}
