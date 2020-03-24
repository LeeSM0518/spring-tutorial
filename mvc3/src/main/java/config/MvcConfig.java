package config;

import controller.RegisterRequestValidator;
import interceptor.AuthCheckInterceptor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

//  @Override
//  public Validator getValidator() {
//    return new RegisterRequestValidator();
//  }

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  @Override
  public void configureViewResolvers(ViewResolverRegistry registry) {
    registry.jsp("/WEB-INF/view/", ".jsp");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/main").setViewName("main");
  }

  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource ms =
        new ResourceBundleMessageSource();
    ms.setBasenames("message.label");
    ms.setDefaultEncoding("UTF-8");
    return ms;
  }

  @Bean
  public AuthCheckInterceptor authCheckInterceptor() {
    return new AuthCheckInterceptor();
  }

  @Override
  // 인터셉터를 설정하는 메서드
  public void addInterceptors(InterceptorRegistry registry) {
    // HandleInterceptor 객체를 설정한다.
    registry.addInterceptor(authCheckInterceptor())
        // 인터셉터를 적용할 경로 패턴을 지정한다.
        .addPathPatterns("/edit/**");
  }

}