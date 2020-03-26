package interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthCheckInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler) throws Exception {
    HttpSession session = request.getSession(false);
    if (session != null) {
      Object authInfo = session.getAttribute("authInfo");
      if (authInfo != null) {
        // 로그인 정보가 존재할 때만 true 반환
        return true;
      }
    }
    // request.getContextPath() : 현재 컨텍스트 경로를 리턴한다.
    //  ex) http://localhos:8080/test 가 웹 애플리케이션 경로일때, /test 가 컨텍스트 경로
    response.sendRedirect(request.getContextPath() + "/login");
    return false;
  }
}
