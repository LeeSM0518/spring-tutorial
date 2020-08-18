package securitytutorial.tutorial.security.configs;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.method.MapBasedMethodSecurityMetadataSource;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import securitytutorial.tutorial.security.factory.MethodResourcesMapFactoryBean;
import securitytutorial.tutorial.security.service.SecurityResourceService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

@Configuration
// 맵 기반 인증을 사용할 것이기 때문에, 속성들은 default(false)로 둔다.
@EnableGlobalMethodSecurity
// 맵 기반 인증 처리를 위한 GlobalMethodSecurityConfiguration 상속
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

  @Autowired
  private SecurityResourceService securityResourceService;

  @Override
  protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
    // MapBasedMethodSecurityMetadataSource : 맵 기반 메소드 인증 처리를 위한 클래스
    return mapBasedMethodSecurityMetadataSource();
  }

  @SneakyThrows
  @Bean
  public MapBasedMethodSecurityMetadataSource mapBasedMethodSecurityMetadataSource() {
    return new MapBasedMethodSecurityMetadataSource(methodResourcesMapFactoryBean().getObject());
  }

  @Bean
  public MethodResourcesMapFactoryBean methodResourcesMapFactoryBean() {
    return new MethodResourcesMapFactoryBean(securityResourceService, "method");
  }

  @Bean
  public MethodResourcesMapFactoryBean pointcutResourcesMapFactoryBean() {
    return new MethodResourcesMapFactoryBean(securityResourceService, "pointcut");
  }

  @Bean
  BeanPostProcessor protectPointcutPostProcessor() throws Exception {
    Class<?> clazz = Class.forName("org.springframework.security.config.method.ProtectPointcutPostProcessor");
    Constructor<?> declaredConstructor = clazz.getDeclaredConstructor(MapBasedMethodSecurityMetadataSource.class);
    declaredConstructor.setAccessible(true);
    Object instance = declaredConstructor.newInstance(mapBasedMethodSecurityMetadataSource());
    Method setPointcutMap = instance.getClass().getMethod("setPointcutMap", Map.class);
    setPointcutMap.setAccessible(true);
    setPointcutMap.invoke(instance, pointcutResourcesMapFactoryBean().getObject());

    return (BeanPostProcessor) instance;
  }

}
