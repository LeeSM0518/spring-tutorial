//package io.security.basicsecurity;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//
//@Configuration
//@EnableWebSecurity
//@Order(0)
//class SecurityTest1 extends WebSecurityConfigurerAdapter {
//
//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http
//        .antMatcher("/admin/**")
//        .authorizeRequests()
//        .anyRequest().authenticated()
//        .and()
//        .httpBasic();
//  }
//
//}
//
//
//@Configuration
//@EnableWebSecurity
//@Order(1)
//class SecurityTest2 extends WebSecurityConfigurerAdapter {
//
//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http
//        .authorizeRequests()
//        .anyRequest().permitAll()
//        .and()
//        .formLogin();
//  }
//
//}
//
//
