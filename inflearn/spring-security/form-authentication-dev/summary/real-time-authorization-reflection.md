# 인가처리 실시간 반영하기

![image](https://user-images.githubusercontent.com/43431081/90334226-319d3280-e007-11ea-8cfa-8d92603fb181.png)

* 관리자가 자원 권한 정보를 바꾸면 DB에는 반영이 되어 있지만, 서버에는 반영이 되어있지 않기 때문에 관리자가 권한을 변경할 때 DB로부터 권한 정보를 다시 불러오는 작업을 해야한다.

<br>

## 실제 코드

* **UrlFilterInvocationSecurityMetadataSource**

  ```java
  public class UrlFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
  
    ...
  
      public void reload() {
      // DB로부터 자원 정보를 가져온다.
      LinkedHashMap<RequestMatcher, List<ConfigAttribute>> reloadMap =
        securityResourceService.getResourceList();
      Iterator<Map.Entry<RequestMatcher, List<ConfigAttribute>>> iterator = 
        reloadMap.entrySet().iterator();
  
      // 자원 정보 맵 객체를 초기화한다.
      requestMap.clear();
  
      // 현재 DB 자원정보로 맵 객체를 업데이트 한다.
      while (iterator.hasNext()) {
        Map.Entry<RequestMatcher, List<ConfigAttribute>> next = iterator.next();
        requestMap.put(next.getKey(), next.getValue());
      }
    }
  
  }
  ```

* **ResourceController**

  ```java
  @Controller
  public class ResourceController {
  
    ...
  
    @Autowired
    private UrlFilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource;
  
    ...
  
    @PostMapping(value = "/admin/resources")
    public String createResources(ResourcesDto resourcesDto) throws Exception {
  
      ModelMapper modelMapper = new ModelMapper();
      Role role = roleRepository.findByRoleName(resourcesDto.getRoleName());
      Set<Role> roles = new HashSet<>();
      roles.add(role);
      Resources resources = modelMapper.map(resourcesDto, Resources.class);
      resources.setRoleSet(roles);
  
      resourcesService.createResources(resources);
  
      if ("url".equals(resourcesDto.getResourceType())) {
        // 자원 정보 맵 객체 업데이트
        filterInvocationSecurityMetadataSource.reload();
      } else {
        methodSecurityService.addMethodSecured(resourcesDto.getResourceName(), resourcesDto.getRoleName());
      }
  
      return "redirect:/admin/resources";
    }
  
    ...
  
    @GetMapping(value = "/admin/resources/delete/{id}")
    public String removeResources(@PathVariable String id, Model model) throws Exception {
  
      Resources resources = resourcesService.getResources(Long.valueOf(id));
      resourcesService.deleteResources(Long.valueOf(id));
  
      if ("url".equals(resources.getResourceType())) {
        // 자원 정보 맵 객체 업데이트
        filterInvocationSecurityMetadataSource.reload();
      } else {
        methodSecurityService.removeMethodSecured(resources.getResourceName());
      }
  
      return "redirect:/admin/resources";
    }
  
  
  }
  ```