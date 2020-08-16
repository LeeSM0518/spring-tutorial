package securitytutorial.tutorial.security.filter;

import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PermitAllFilter extends FilterSecurityInterceptor {

  private static final String FILTER_APPLIED = "__spring_security_filterSecurityInterceptor_filterApplied";
  private boolean observeOncePerRequest = true;

  private List<RequestMatcher> permitAllRequestMatchers = new ArrayList<>();

  public PermitAllFilter(String ... permitAllResources) {
    permitAllRequestMatchers = Arrays.stream(permitAllResources)
        .map(AntPathRequestMatcher::new)
        .collect(Collectors.toList());
  }

  @Override
  protected InterceptorStatusToken beforeInvocation(Object object) {

    HttpServletRequest request = ((FilterInvocation) object).getRequest();
    boolean permitAll = permitAllRequestMatchers.stream()
        .anyMatch(requestMatcher -> requestMatcher.matches(request));

    if (permitAll) {
      return null;
    }

    return super.beforeInvocation(object);
  }

  @Override
  public void invoke(FilterInvocation fi) throws IOException, ServletException {
    if ((fi.getRequest() != null)
        && (fi.getRequest().getAttribute(FILTER_APPLIED) != null)
        && observeOncePerRequest) {
      fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
    } else {
      if (fi.getRequest() != null && observeOncePerRequest) {
        fi.getRequest().setAttribute(FILTER_APPLIED, Boolean.TRUE);
      }

      InterceptorStatusToken token = beforeInvocation(fi);

      try {
        fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
      } finally {
        super.finallyInvocation(token);
      }

      super.afterInvocation(token, null);
    }
  }
}
