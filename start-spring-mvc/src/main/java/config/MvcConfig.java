package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// @EnableWebMvc : 스프링 MVC 설정을 활성화한다.
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

  @Override
  // DispatcherServlet의 매핑 경로를 '/'로 주었을 때,
  //  JSP/HTML/CSS 등을 올바르게 처리하기 위한 설정을 추가한다.
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  @Override
  // JSP를 이용해서 컨트롤러의 실행
  public void configureViewResolvers(ViewResolverRegistry registry) {
    registry.jsp("/WEB-INF/view/", ".jsp");
  }

}
