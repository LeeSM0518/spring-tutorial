package securitytutorial.tutorial.security.configs;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import securitytutorial.tutorial.security.filter.AjaxLoginProcessFilter;

public final class AjaxLoginConfigurer<H extends HttpSecurityBuilder<H>> extends
    AbstractAuthenticationFilterConfigurer<H, AjaxLoginConfigurer<H>, AjaxLoginProcessFilter> {

  private AuthenticationSuccessHandler successHandler;
  private AuthenticationFailureHandler failureHandler;
  private AuthenticationManager authenticationManager;

  public AjaxLoginConfigurer() {
    super(new AjaxLoginProcessFilter(), null);
  }

  @Override
  public void init(H http) throws Exception {
    super.init(http);
  }

  @Override
  public void configure(H http) throws Exception {
    if (authenticationManager == null) {
      // AuthenticationManger 공유 객체를 가져온다.
      authenticationManager = http.getSharedObject(AuthenticationManager.class);
    }

    // 필터 설정
    getAuthenticationFilter().setAuthenticationManager(authenticationManager);
    getAuthenticationFilter().setAuthenticationSuccessHandler(successHandler);
    getAuthenticationFilter().setAuthenticationFailureHandler(failureHandler);

    SessionAuthenticationStrategy sessionAuthenticationStrategy =
        http.getSharedObject(SessionAuthenticationStrategy.class);
    if (sessionAuthenticationStrategy != null) {
      getAuthenticationFilter().setSessionAuthenticationStrategy(sessionAuthenticationStrategy);
    }

    RememberMeServices rememberMeServices = http.getSharedObject(RememberMeServices.class);

    if (rememberMeServices != null) {
      getAuthenticationFilter().setRememberMeServices(rememberMeServices);
    }

    http.setSharedObject(AjaxLoginProcessFilter.class, getAuthenticationFilter());
    http.addFilterBefore(getAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
  }

  public AjaxLoginConfigurer<H> setSuccessHandler(AuthenticationSuccessHandler successHandler) {
    this.successHandler = successHandler;
    return this;
  }

  public AjaxLoginConfigurer<H> setFailureHandler(AuthenticationFailureHandler failureHandler) {
    this.failureHandler = failureHandler;
    return this;
  }

  public AjaxLoginConfigurer<H> setAuthenticationManager(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
    return this;
  }

  @Override
  protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
    return new AntPathRequestMatcher(loginProcessingUrl, "POST");
  }

}
