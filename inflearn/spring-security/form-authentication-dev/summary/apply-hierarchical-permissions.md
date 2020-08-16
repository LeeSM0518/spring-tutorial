# 계층 권한 적용하기

![image-20200816222555447](../../../../../Library/Application Support/typora-user-images/image-20200816222555447.png)

* **RoleHierarchy**
  * 상위 계층 Role은 하위 계층 Role의 자원에 접근 가능함
  * ROLE_ADMIN > ROLE_MANAGER > ROLE_USER 일 경우 ROLE_ADMIN 만 있으면 하위 ROLE 의 권한을 모두 포함한다.
* **RoleHierarchyVoter**
  * RoleHierarchy 를 생성자로 받으며 이 클래스에서 설정한 규칙이 적용되어 심사함

<br>

## 실제 코드

* **RoleHierarchy**

  ```java
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Entity
  @Table(name = "ROLE_HIERARCHY")
  // 권한 계층을 위한 도메인
  public class RoleHierarchy implements Serializable {
  
    @Id
    @GeneratedValue
    private Long id;
  
    @Column(name = "child_name")
    private String childName;
  
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_name", referencedColumnName = "child_name")
    private RoleHierarchy parentName;
  
    @OneToMany(mappedBy = "parentName", cascade = CascadeType.ALL)
    private Set<RoleHierarchy> roleHierarchy = new HashSet<>();
  
  }
  ```

* **RoleHierarchyRepository**

  ```java
  public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, Long> {
  
    RoleHierarchy findByChildName(String roleName);
  
  }
  ```

* **RoleHierarchyService**

  ```java
  public interface RoleHierarchyService {
  
    String findAllHierarchy();
  
  }
  ```

* **RoleHierarchyServiceImpl**

  ```java
  @Service
  public class RoleHierarchyServiceImpl implements RoleHierarchyService {
  
    @Autowired
    private RoleHierarchyRepository roleHierarchyRepository;
  
    @Override
    @Transactional
    public String findAllHierarchy() {
      List<RoleHierarchy> roleHierarchies = roleHierarchyRepository.findAll();
  
      StringBuilder roles = new StringBuilder();
  
      roleHierarchies
          .forEach(roleHierarchy -> {
            if (roleHierarchy.getParentName() != null) {
              roles.append(roleHierarchy.getParentName().getChildName());
              roles.append(" > ");
              roles.append(roleHierarchy.getChildName());
              roles.append("\n");
            }
          });
  
      return roles.toString();
    }
  
  }
  ```

* **SetupDataLoader**

  ```java
  @Component
  public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
  
    private boolean alreadySetup = false;
  
    @Autowired
    private UserRepository userRepository;
  
    @Autowired
    private RoleRepository roleRepository;
  
    @Autowired
    private RoleHierarchyRepository roleHierarchyRepository;
  
    @Autowired
    private ResourcesRespository resourcesRespository;
  
    @Autowired
    private PasswordEncoder passwordEncoder;
  
    @Autowired
    private AccessIpRepository accessIpRepository;
  
    private static AtomicInteger count = new AtomicInteger(0);
  
    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
      if (alreadySetup) {
        return;
      }
  
      setupSecurityResources();
      setupAccessIpData();
  
      alreadySetup = true;
    }
  
    private void setupAccessIpData() {
      AccessIp byIpAddress = accessIpRepository.findByIpAddress("0:0:0:0:0:0:0:1");
      if (byIpAddress == null) {
        AccessIp accessIp = AccessIp.builder()
            .ipAddress("0:0:0:0:0:0:0:1")
            .build();
        accessIpRepository.save(accessIp);
      }
    }
  
    private void setupSecurityResources() {
      Set<Role> roles = new HashSet<>();
      Role adminRole = createRoleIfNotFound("ROLE_ADMIN", "관리자");
      roles.add(adminRole);
      createResourceIfNotFound("/admin/**", "", roles, "url");
  //    createResourceIfNotFound("execution(public * securitytutorial.tutorial.aopsecurity.*Service.pointcut*(..))", "", roles, "pointcut");
      createUserIfNotFound("admin", "admin@admin.com", "pass", roles);
      Role managerRole = createRoleIfNotFound("ROLE_MANAGER", "매니저권한");
      Role userRole = createRoleIfNotFound("ROLE_USER", "사용자권한");
      createRoleHierarchyIfNotFound(managerRole, adminRole);
      createRoleHierarchyIfNotFound(userRole, managerRole);
    }
  
    private void createRoleHierarchyIfNotFound(Role childRole, Role parentRole) {
      RoleHierarchy roleHierarchy = roleHierarchyRepository.findByChildName(parentRole.getRoleName());
      if (roleHierarchy == null) {
        roleHierarchy = RoleHierarchy.builder()
            .childName(parentRole.getRoleName())
            .build();
      }
      RoleHierarchy parentRoleHierarchy = roleHierarchyRepository.save(roleHierarchy);
  
      roleHierarchy = roleHierarchyRepository.findByChildName(childRole.getRoleName());
      if(roleHierarchy == null) {
        roleHierarchy = RoleHierarchy.builder()
            .childName(childRole.getRoleName())
            .build();
      }
      RoleHierarchy childRoleHierarchy = roleHierarchyRepository.save(roleHierarchy);
      childRoleHierarchy.setParentName(parentRoleHierarchy);
    }
  
    @Transactional
    public Resources createResourceIfNotFound(String resourceName, String httpMethod, Set<Role> roleSet, String resourceType) {
      Resources resources = resourcesRespository.findByResourceNameAndHttpMethod(resourceName, httpMethod);
  
      if (resources == null) {
        resources = Resources.builder()
            .resourceName(resourceName)
            .httpMethod(httpMethod)
            .resourceType(resourceType)
            .orderNum(count.incrementAndGet())
            .build();
      }
  
      return resourcesRespository.save(resources);
    }
  
    @Transactional
    public Account createUserIfNotFound(final String userName, final String email, final String password, Set<Role> roleSet) {
      Account account = userRepository.findByUsername(userName);
  
      if (account == null) {
        account = Account.builder()
            .username(userName)
            .email(email)
            .password(passwordEncoder.encode(password))
            .userRoles(roleSet)
            .build();
      }
  
      return userRepository.save(account);
    }
  
    @Transactional
    public Role createRoleIfNotFound(String roleName, String roleDesc) {
      Role role = roleRepository.findByRoleName(roleName);
  
      if (role == null) {
        role = Role
            .builder()
            .roleName(roleName)
            .roleDesc(roleDesc)
            .build();
      }
  
      return roleRepository.save(role);
    }
  
  }
  ```

* **SecurityInitializer**

  ```java
  @Component
  // 어플리케이션이 시작될 때 초기화 작업
  public class SecurityInitializer implements ApplicationRunner {
  
    @Autowired
    private RoleHierarchyService roleHierarchyService;
  
    @Autowired
    private RoleHierarchyImpl roleHierarchy;
  
    @Override
    public void run(ApplicationArguments args) throws Exception {
      String allHierarchy = roleHierarchyService.findAllHierarchy();
      roleHierarchy.setHierarchy(allHierarchy);
    }
  
  }
  ```