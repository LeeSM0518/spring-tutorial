package securitytutorial.tutorial.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import securitytutorial.tutorial.domain.dto.AccountDto;
import securitytutorial.tutorial.security.token.AjaxAuthenticationToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AjaxLoginProcessFilter extends AbstractAuthenticationProcessingFilter {

  private ObjectMapper objectMapper = new ObjectMapper();

  public AjaxLoginProcessFilter() {
    // 로그인 요청 URL 설정
    super(new AntPathRequestMatcher("/api/login"));
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
                                              HttpServletResponse response)
      throws AuthenticationException, IOException, ServletException {
    // 요청이 Ajax 요청인지 점검
    if (!isAjax(request)) {
      throw new IllegalStateException("Authentication is not supported");
    }

    AccountDto accountDto = objectMapper.readValue(request.getReader(), AccountDto.class);

    if (StringUtils.isEmpty(accountDto.getUsername()) || StringUtils.isEmpty(accountDto.getPassword())) {
      throw new IllegalArgumentException("Username or Password is empty");
    }

    AjaxAuthenticationToken ajaxAuthenticationToken =
        new AjaxAuthenticationToken(accountDto.getUsername(), accountDto.getPassword());

    return getAuthenticationManager().authenticate(ajaxAuthenticationToken);
  }

  private boolean isAjax(HttpServletRequest request) {
    return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
  }

}
