package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import controller.RegisterRequestValidator;
import interceptor.AuthCheckInterceptor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

@Configuration
// @EnableWebMvc를 통해 스프링 MVC는
//  여러 형식으로 변환할 수 있는 HttpMessageConverter를 미리 등록한다.
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

//  @Override
//  public Validator getValidator() {
//    return new RegisterRequestValidator();
//  }

  @Override
  // HttpMessageConverter를 추가로 설정할 때 사용하는 메서드이다.
  // 등록된 HttpMessageConverter 목록을 파라미터로 받는다.
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
        .json()
        // Jackson이 날짜 형식을 출력할 때 유닉스 타임 스탬프로 출력하는
        //  기능을 비활성화한다.
        // 이 기능을 비활성화하면 ObjectMapper는 날짜 타입의 값을
        //  ISO-8601 형식으로 출력한다.
        .featuresToDisable(
            SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS)
        .build();
    // 새로 생성한 HttpMessageConverter는 목록의 제일 앞에 위치시켜야 한다.
    converters.add(0,
        new MappingJackson2HttpMessageConverter(objectMapper));
  }

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