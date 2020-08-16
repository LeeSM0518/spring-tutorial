package securitytutorial.tutorial.security.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import securitytutorial.tutorial.security.common.AjaxLoginAuthenticationEntryPoint;
import securitytutorial.tutorial.security.filter.AjaxLoginProcessFilter;
import securitytutorial.tutorial.security.handler.AjaxAccessDeniedHandler;
import securitytutorial.tutorial.security.handler.AjaxAuthenticationFailureHandler;
import securitytutorial.tutorial.security.handler.AjaxAuthenticationSuccessHandler;
import securitytutorial.tutorial.security.provider.AjaxAuthenticationProvider;

@Configuration
@Order(0) // 설정 순서 결정, 0: 가장 먼저 설정을 한다.
public class AjaxSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(ajaxAuthenticationProvider());
  }

  @Bean
  public AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler() {
    return new AjaxAuthenticationSuccessHandler();
  }

  @Bean
  public AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler() {
    return new AjaxAuthenticationFailureHandler();
  }

  @Bean
  public AuthenticationProvider ajaxAuthenticationProvider() {
    return new AjaxAuthenticationProvider();
  }

  @Bean
  public AjaxLoginAuthenticationEntryPoint ajaxLoginAuthenticationEntryPoint() {
    return new AjaxLoginAuthenticationEntryPoint();
  }

  @Bean
  public AjaxAccessDeniedHandler ajaxAccessDeniedHandler() {
    return new AjaxAccessDeniedHandler();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .antMatcher("/api/**")
        .authorizeRequests()
        .antMatchers("/api/login", "/api/login*").permitAll()
        .antMatchers("/api/messages").hasRole("MANAGER")
        .anyRequest().authenticated();

    /**
     * addFilter       : 가장 마지막에 필터가 존재해야 할 때
     * addFilterBefore : 추가하고자 하는 필터가 기존의 필터 앞에 존재해야 할 때
     * addFilterAfter  : 추가하고자 하는 필터가 기존의 필터 뒤에 존재해야 할 때
     * addFilterAt     : 기존의 필터 위치를 대체하고자 할 때 사용한다.
     */
    http
//        .addFilterBefore(ajaxLoginProcessFilter(), UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling()
        .authenticationEntryPoint(ajaxLoginAuthenticationEntryPoint())
        .accessDeniedHandler(ajaxAccessDeniedHandler());
//    http.csrf().disable(); // CSRF 기능 X
    http.csrf(); // CSRF 기능 X

    customConfigurerAjax(http);
  }

  private void customConfigurerAjax(HttpSecurity http) throws Exception {
    http
        .apply(new AjaxLoginConfigurer<>())
        .setSuccessHandler(ajaxAuthenticationSuccessHandler())
        .setFailureHandler(ajaxAuthenticationFailureHandler())
        .setAuthenticationManager(authenticationManager())
        .loginProcessingUrl("/api/login");
  }

//  @Bean
//  public AjaxLoginProcessFilter ajaxLoginProcessFilter() throws Exception {
//    AjaxLoginProcessFilter ajaxLoginProcessFilter = new  AjaxLoginProcessFilter();
//    ajaxLoginProcessFilter.setAuthenticationManager(authenticationManagerBean());
//    ajaxLoginProcessFilter.setAuthenticationSuccessHandler(ajaxAuthenticationSuccessHandler());
//    ajaxLoginProcessFilter.setAuthenticationFailureHandler(ajaxAuthenticationFailureHandler());
//    return ajaxLoginProcessFilter;
//  }

}
