# Map 기반 DB 연동

![image](https://user-images.githubusercontent.com/43431081/90331008-6f409200-dfec-11ea-9c33-7d0b608c7a1d.png)

<br>

## 실제 코드

* **UrlResourcesMapFactoryBean**

  ```java
  // 리소스 빈을 생성한다.
  public class UrlResourcesMapFactoryBean implements FactoryBean<LinkedHashMap<RequestMatcher, List<ConfigAttribute>>> {
  
    private SecurityResourceService securityResourceService;
    // 빈으로 만들것이다.
    // 순서를 보장해서 저장한다.
    private LinkedHashMap<RequestMatcher, List<ConfigAttribute>> resourceMap;
  
    public void setSecurityResourceService(SecurityResourceService securityResourceService) {
      this.securityResourceService = securityResourceService;
    }
  
    @Override
    public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getObject() throws Exception {
      if (resourceMap == null) {
        init();
      }
      return resourceMap;
    }
  
    private void init() {
      resourceMap = securityResourceService.getResourceList();
    }
  
    @Override
    public Class<?> getObjectType() {
      return LinkedHashMap.class;
    }
  
    @Override
    public boolean isSingleton() {
      return true;
    }
  
  }
  ```

* **SecurityResourceService**

  ```java
  @Service
  public class SecurityResourceService  {
  
    private ResourcesRespository resourcesRepository;
  
    public SecurityResourceService(ResourcesRespository resourcesRespository) {
      this.resourcesRepository = resourcesRespository;
    }
  
    // DB로부터 데이터를 가져와서 맵 객체를 만들어서 반환해준다.
    public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getResourceList() {
      LinkedHashMap<RequestMatcher, List<ConfigAttribute>> result = new LinkedHashMap<>();
      // DB에서 모든 리소스를 가져온다.
      List<Resources> resourcesList = resourcesRepository.findAllResources();
      resourcesList.forEach(re -> {
        List<ConfigAttribute> configAttributeList = new ArrayList<>();
        re.getRoleSet().forEach(role -> {
          configAttributeList.add(new SecurityConfig(role.getRoleName()));
          result.put(new AntPathRequestMatcher(re.getResourceName()), configAttributeList);
        });
      });
      return result;
    }
  
  }
  ```

* **ResourcesRespository**

  ```java
  public interface ResourcesRespository extends JpaRepository<Resources, Long> {
  
    Resources findByResourceNameAndHttpMethod(String resourceName, String httpMethod);
  
    @Query("select r from Resources r join fetch r.roleSet where r.resourceType = 'url' order by r.orderNum desc")
    List<Resources> findAllResources();
  
  }
  ```

* **AppConfig**

  ```java
  @Configuration
  public class AppConfig {
  
    @Bean
    public SecurityResourceService securityResourceService(ResourcesRespository resourcesRespository) {
      return new SecurityResourceService(resourcesRespository);
    }
  
  }
  ```

* **SecurityConfig**

  ```java
  @Configuration
  @EnableWebSecurity
  @Slf4j
  @Order(1)
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    ...
  
      @Autowired
      private SecurityResourceService securityResourceService;
  
    ...
  
      @Bean
      public FilterSecurityInterceptor customFilterSecurityInterceptor() throws Exception {
      FilterSecurityInterceptor filterSecurityInterceptor = 
        new FilterSecurityInterceptor();
      filterSecurityInterceptor.setSecurityMetadataSource(
        urlFilterInvocationSecurityMetadataSource());
      /**
       * affirmativeBased : 하나라도 승인이 있으면 승인 처리
       * ConsensusBased   : 승인과 거부의 개수를 따져서 다수결 쪽으로 처리
       * UnanimousBased   : 하나라도 거부가 있으면 거부 처리
       */
      filterSecurityInterceptor.setAccessDecisionManager(affirmativeBased());
      filterSecurityInterceptor.setAuthenticationManager(authenticationManagerBean());
      return filterSecurityInterceptor;
    }
  
    @Bean
    public FilterInvocationSecurityMetadataSource urlFilterInvocationSecurityMetadataSource() throws Exception {
      return new UrlFilterInvocationSecurityMetadataSource(
        urlResourcesMapFactoryBean().getObject());
    }
  
    @Bean
    public UrlResourcesMapFactoryBean urlResourcesMapFactoryBean() {
      UrlResourcesMapFactoryBean urlResourcesMapFactoryBean = 
        new UrlResourcesMapFactoryBean();
      urlResourcesMapFactoryBean.setSecurityResourceService(
        securityResourceService);
      return urlResourcesMapFactoryBean;
    }
  
  }
  ```

  