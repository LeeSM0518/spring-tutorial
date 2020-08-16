package io.security.basicsecurity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity // 웹 보안 활성화
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private UserDetailsService userDetailsService;

//  @Override
  // 인증, 인가 API 설정 및 보안 옵션 설정 가능

//  protected void configure(HttpSecurity http) throws Exception {
//    http
//        .authorizeRequests()
//        .anyRequest().authenticated()   // 어떠한 요청에도 인증 설정
//
//        .and()
//
//        // 로그인 필터 설정
//        .formLogin()
////        .loginPage("/loginPage")
//        .defaultSuccessUrl("/")            // 로그인 성공 후 이동 페이지
//        .failureUrl("/login")              // 로그인 실패 후 이동 페이지
//        .usernameParameter("userId")       // 아이디 파라미터명 설정
//        .passwordParameter("passwd")       // 패스워드 파라미터명 설정
//        .loginProcessingUrl("/login_proc") // 로그인 Form Action Url
//        .successHandler((request, response, authentication) -> {
//          // 로그인 성공 후 핸들러
//          System.out.println("authentication: " + authentication.getName());
//          response.sendRedirect("/");
//        })
//        .failureHandler((request, response, exception) -> {
//          // 로그인 실패 후 핸들러
//          System.out.println("exception: " + exception.getMessage());
//          response.sendRedirect("/login");
//        }).permitAll()
//
//        .and()
//
//        // 로그아웃 필터 설정
//        .logout()
//        .logoutUrl("/logout")        // 로그아웃 처리 URL
//        .logoutSuccessUrl("/login")  // 로그아웃 성공 후 이동페이지
//        .addLogoutHandler(((request, response, authentication) -> {
//          // 로그아웃 핸들러 추가
//          HttpSession session = request.getSession();
//          session.invalidate();
//        }))
//        .logoutSuccessHandler(((request, response, authentication) -> {
//          // 로그아웃 성공 후 핸들러
//          response.sendRedirect("/login");
//        }))
//        .deleteCookies("remember-me")  // 로그아웃 후 쿠키 삭
//
//        .and()
//
//        // remember me 필터 설정
//        .rememberMe()
//        .rememberMeParameter("remember")            // 파라미터 명 (default: remember-me)
//        .tokenValiditySeconds(3600)                 // 토큰 유지 기간 (default: 14일)
//        .userDetailsService(userDetailsService)
//
//        .and()
//
//        // Session 제어 필터 설정
//        .sessionManagement()
//        .maximumSessions(1)               // 최대 허용 가능 세션 개수 (-1: 무제한 로그인 세션)
//        .maxSessionsPreventsLogin(false); // 동시 접속 차단 (true, false)
////        .expiredUrl("/expired")  세션이 만료된 경우 이동 할 페이지
//
//    // Session 고정 보호 필터 설정
//    http
//        .sessionManagement()
//        .sessionFixation()
//        .changeSessionId(); // 인증이되면 세션이 바뀐다.
//
//    // Session 정책 설정
//    http
//        .sessionManagement()
//        /**
//         * SessionCreationPolicy Enum
//         * Always      : 스프링 시큐리티가 항상 세션 생성
//         * If_Required : 스프링 시큐리티가 필요 시 생성(기본값)
//         * Never       : 스프링 시큐리티가 생성하지 않지만 이미 존재하면 사용
//         * Stateless   : 스프링 시큐리티가 생성하지 않고 존재해도 사용하지 않음
//         */
//        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
//  }

//  @Override
//  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//    // 계정을 메모리에 올린다.
//    auth.inMemoryAuthentication()
//        // {noop} : 패스워드 암호화 유형(noop: 평문 그대로)
//        .withUser("user").password("{noop}1111").roles("USER");
//    auth.inMemoryAuthentication()
//        .withUser("sys").password("{noop}1111").roles("SYS");
//    auth.inMemoryAuthentication()
//        .withUser("admin").password("{noop}1111").roles("ADMIN");
//  }
//
//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http
//        .authorizeRequests()
//        .antMatchers("/user").hasRole("USER")
//        .antMatchers("/admin/pay").hasRole("ADMIN")
//        .antMatchers("/admin/**").access("hasRole('ADMIN') or hasRole('SYS')")
//        .anyRequest().authenticated();
//    http
//        .formLogin();
//  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    // 계정을 메모리에 올린다.
    auth.inMemoryAuthentication()
        // {noop} : 패스워드 암호화 유형(noop: 평문 그대로)
        .withUser("user").password("{noop}1111").roles("USER");
    auth.inMemoryAuthentication()
        .withUser("sys").password("{noop}1111").roles("SYS");
    auth.inMemoryAuthentication()
        .withUser("admin").password("{noop}1111").roles("ADMIN");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        .antMatchers("/login").permitAll() // 로그인 페이지는 인증을 허락해야 인증을 할 수 있다.
        .antMatchers("/user").hasRole("USER")
        .antMatchers("/admin/pay").hasRole("ADMIN")
        .antMatchers("/admin/**").access("hasRole('ADMIN') or hasRole('SYS')")
        .antMatchers("/*").hasRole("USER")
        .anyRequest().authenticated();
    http
        .formLogin()
        .successHandler(((request, response, authentication) -> {
          HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
          SavedRequest savedRequest = requestCache.getRequest(request, response);
          String redirectUrl = savedRequest.getRedirectUrl();
          response.sendRedirect(redirectUrl);
        }));
    http
        .exceptionHandling()
        .authenticationEntryPoint(((request, response, authException) -> {
          response.sendRedirect("/login");
        }))
        .accessDeniedHandler(((request, response, accessDeniedException) -> {
          response.sendRedirect("/denied");
        }));
  }

}
