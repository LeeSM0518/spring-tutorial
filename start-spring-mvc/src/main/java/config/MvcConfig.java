package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.HashMap;
import java.util.Map;

@Configuration
// @EnableWebMvc : 스프링 MVC 설정을 활성화한다.
//@EnableWebMvc
public class MvcConfig /*implements WebMvcConfigurer*/ {

//  @Override
//  // DispatcherServlet의 매핑 경로를 '/'로 주었을 때,
//  //  JSP/HTML/CSS 등을 올바르게 처리하기 위한 설정을 추가한다.
//  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
//    configurer.enable();
//  }
//
//  @Override
//  // JSP를 이용해서 컨트롤러의 실행
//  public void configureViewResolvers(ViewResolverRegistry registry) {
//    registry.jsp("/WEB-INF/view/", ".jsp");
//  }

  @Bean
  public HandlerMapping handlerMapping() {
    RequestMappingHandlerMapping hm =
        new RequestMappingHandlerMapping();
    hm.setOrder(0);
    return hm;
  }

  @Bean
  public HandlerAdapter handlerAdapter() {
    return new RequestMappingHandlerAdapter();
  }

  @Bean
  public HandlerMapping simpleHandlerMapping() {
    SimpleUrlHandlerMapping hm = new SimpleUrlHandlerMapping();
    Map<String, Object> pathMap = new HashMap<>();
    pathMap.put("/**", defaultServletHandler());
    hm.setUrlMap(pathMap);
    return hm;
  }

  @Bean
  public HttpRequestHandler defaultServletHandler() {
    return new DefaultServletHttpRequestHandler();
  }

  @Bean
  public HandlerAdapter requestHandlerAdapter() {
    return new HttpRequestHandlerAdapter();
  }

  @Bean
  public ViewResolver viewResolver() {
    InternalResourceViewResolver vr = new InternalResourceViewResolver();
    vr.setPrefix("/WEB-INF/view/");
    vr.setSuffix(".jsp");
    return vr;
  }


}
