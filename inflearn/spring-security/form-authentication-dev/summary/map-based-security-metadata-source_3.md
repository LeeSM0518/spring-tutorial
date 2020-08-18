# MapBasedSecurityMetadataSource (3)

![image](https://user-images.githubusercontent.com/43431081/90382941-6ae2aa80-e0ba-11ea-9f71-ad8f6793b761.png)

* **MethodResourcesMapFactoryBean**

  * MapBasedSecurityMetadataSource의 생성자로 ResorceMap을 전달하면 된다.

  * DB로 부터 얻은 권한/자원 정보를 ResourceMap 을 빈으로 생성해서 MapBasedMethodSecurityMetadataSource 에 전달.

<br>

## 처리 과정

![image](https://user-images.githubusercontent.com/43431081/90383074-9cf40c80-e0ba-11ea-8a5d-4683ef2cbcc1.png)

<br>

## 실제 코드

* **MethodSecurityConfig**

  ```java
  @Configuration
  @EnableGlobalMethodSecurity
  public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
  
    @Autowired
    private SecurityResourceService securityResourceService;
  
    @Override
    protected MethodSecurityMetadataSource customMethodSecurityMetadataSource() {
      return mapBasedMethodSecurityMetadataSource();
    }
  
    @Bean
    public MapBasedMethodSecurityMetadataSource mapBasedMethodSecurityMetadataSource() {
      return new MapBasedMethodSecurityMetadataSource(
        methodResourcesMapFactoryBean().getObject());
    }
  
    @Bean
    public MethodResourcesFactoryBean methodResourcesMapFactoryBean() {
      return new MethodResourcesFactoryBean(securityResourceService);
    }
  
  }
  ```
  
* **MethodResourcesFactoryBean**

  ```java
  public class MethodResourcesFactoryBean implements FactoryBean<LinkedHashMap<String, List<ConfigAttribute>>> {
  
    private SecurityResourceService securityResourceService;
    private LinkedHashMap<String, List<ConfigAttribute>> resourceMap;
  
    public MethodResourcesFactoryBean(SecurityResourceService securityResourceService) {
      this.securityResourceService = securityResourceService;
    }
  
    public void setSecurityResourceService(SecurityResourceService securityResourceService) {
      this.securityResourceService = securityResourceService;
    }
  
    @Override
    public LinkedHashMap<String, List<ConfigAttribute>> getObject() 
      throws Exception {
      if (resourceMap == null) {
        init();
      }
      return resourceMap;
    }
  
    private void init() {
      resourceMap = securityResourceService.getMethodResourceList();
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
  
    ...
  
    public LinkedHashMap<String, List<ConfigAttribute>> getMethodResourceList() {
      LinkedHashMap<String, List<ConfigAttribute>> result = 
        new LinkedHashMap<>();
      List<Resources> resourcesList = 
        resourcesRepository.findAllMethodResources();
      resourcesList.forEach(re -> {
        List<ConfigAttribute> configAttributeList = new ArrayList<>();
        re.getRoleSet().forEach(role -> {
          configAttributeList.add(new SecurityConfig(role.getRoleName()));
        });
          result.put(re.getResourceName(), configAttributeList);
      });
      return result;
    }
    
    ...
  }
  ```