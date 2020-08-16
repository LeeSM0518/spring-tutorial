package securitytutorial.tutorial.security.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import securitytutorial.tutorial.security.handler.CustomAccessDeniedHandler;
import securitytutorial.tutorial.security.metadatasource.UrlFilterInvocationSecurityMetadataSource;
import securitytutorial.tutorial.security.provider.CustomAuthenticationProvider;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@Slf4j
@Order(1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  /**
   * 커스텀 UserDetailService 를 사용하는
   * 커스텀 AuthenticationProvider 을 새롭게 만들었기 때문에,
   * 지워도 된다.
   */
//  @Autowired
//  private UserDetailsService userDetailsService;

  @Autowired
  private AuthenticationSuccessHandler customAuthenticationSuccessHandler;

  @Autowired
  private AuthenticationFailureHandler customAuthenticationFailureHandler;

  @Autowired
  private AuthenticationDetailsSource authenticationDetailsSource;

  @Override
  // 커스텀 인증 처리 서비스 등록
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//    auth.userDetailsService(userDetailsService);
    // 커스텀 AuthenticationProvider 적용
    auth.authenticationProvider(authenticationProvider());
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    return new CustomAuthenticationProvider();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    // 비밀번호 암호화
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    // 정적 파일들 요청하는 것은 보안 필터를 적용하지 않는다.
    web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        /**
         * "/login*" : 로그인 URL에 query param 을 넘기기 위함
         */
        .antMatchers("/", "/users", "user/login/**", "/login*").permitAll()
        .antMatchers("/mypage").hasRole("USER")
        .antMatchers("/messages").hasRole("MANAGER")
        .antMatchers("/config").hasRole("ADMIN")
        .anyRequest().authenticated()
    .and()
        .formLogin()
        .loginPage("/login")                 // 로그인 페이지
        .loginProcessingUrl("/login_proc")   // 로그인 처리 URL
        .authenticationDetailsSource(authenticationDetailsSource) // ID, PW 이외의 추가 인증
        .defaultSuccessUrl("/")              // 기본 URL
        .successHandler(customAuthenticationSuccessHandler) // 인증 성공 후처리 핸들러
        .failureHandler(customAuthenticationFailureHandler) // 인증 실패 후처리 핸들러
        .permitAll();                        // 누구든지 인증

    http
        .exceptionHandling()
        .accessDeniedHandler(accessDeniedHandler()); // 권한 인증 실패시 처리할 핸들러

    http
        .addFilterBefore(customFilterSecurityInterceptor(), FilterSecurityInterceptor.class);
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    CustomAccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler();
    accessDeniedHandler.setErrorPage("/denied");
    return accessDeniedHandler;
  }

  @Bean
  public FilterInvocationSecurityMetadataSource urlFilterInvocationSecurityMetadataSource() {
    return new UrlFilterInvocationSecurityMetadataSource();
  }

  @Bean
  public AccessDecisionManager affirmativeBased() {
    AffirmativeBased affirmativeBased = new AffirmativeBased(getAccessDecisionVoters());
    return affirmativeBased;
  }

  private List<AccessDecisionVoter<?>> getAccessDecisionVoters() {
    return Arrays.asList(new RoleVoter());
  }

  @Bean
  public FilterSecurityInterceptor customFilterSecurityInterceptor() throws Exception {
    FilterSecurityInterceptor filterSecurityInterceptor = new FilterSecurityInterceptor();
    filterSecurityInterceptor.setSecurityMetadataSource(urlFilterInvocationSecurityMetadataSource());
    /**
     * affirmativeBased : 하나라도 승인이 있으면 승인 처리
     * ConsensusBased   : 승인과 거부의 개수를 따져서 다수결 쪽으로 처리
     * UnanimousBased   : 하나라도 거부가 있으면 거부 처리
     */
    filterSecurityInterceptor.setAccessDecisionManager(affirmativeBased());
    filterSecurityInterceptor.setAuthenticationManager(authenticationManagerBean());
    return filterSecurityInterceptor;
  }

}
