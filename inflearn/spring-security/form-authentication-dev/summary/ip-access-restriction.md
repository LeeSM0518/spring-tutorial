# 아이피 접속 제한하기

![image](https://user-images.githubusercontent.com/43431081/90336846-73cf6f80-e019-11ea-9aaa-81877f0090d0.png)

* **심의 기준**
  * 특정한 IP 만 접근이 가능하도록 심의하는 Voter 추가
  * Voter 중에서 가장 먼저 심사하도록 하여 허용된 IP 일 경우에만 최종 승인 및 거부 결정을 하도록 한다.
  * 허용된 IP 이면 ACCESS_GRANTED 가 아닌 ACCESS_ABSTAIN 을 리턴해서 추가 심의를 계속 진행하도록 한다
  * 허용된 IP 가 아니면 ACCESS_DENIED 를 리턴하지 않고 즉시 예외 발생하여 최종 자원 접근 거부

<br>

## 실제 코드

* **AccessIp**

  ```java
  @Entity
  @Table(name = "ACCESS_IP")
  @Data
  @EqualsAndHashCode(of = "id")
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public class AccessIp implements Serializable {
  
    @Id
    @GeneratedValue
    @Column(name = "IP_ID", unique = true, nullable = false)
    private Long id;
  
    @Column(name = "IP_ADDRESS", nullable = false)
    private String ipAddress;
  
  }
  ```

* **AccessIpRepository**

  ```java
  public interface AccessIpRepository extends JpaRepository<AccessIp, Long> {
  
    AccessIp findByIpAddress(String ipAddress);
  
  }
  ```

* **SetupDataLoader**

  ```java
  @Component
  public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
  
    private boolean alreadySetup = false;
  
    ...
  
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
  
    ...
  
  }
  ```

* **IpAddressVoter**

  ```java
  public class IpAddressVoter implements AccessDecisionVoter<Object> {
  
  
    private SecurityResourceService securityResourceService;
  
    public IpAddressVoter(SecurityResourceService securityResourceService) {
      this.securityResourceService = securityResourceService;
    }
  
    @Override
    public boolean supports(ConfigAttribute attribute) {
      return true;
    }
  
    @Override
    public boolean supports(Class<?> clazz) {
      return true;
    }
  
    /**
     *
     * @param authentication 인증 객체 정보
     * @param object         요청 정보
     * @param attributes     자원 권한 정보
     * @return
     */
    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
      WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
      String remoteAddress = details.getRemoteAddress();
      System.out.println(remoteAddress);
  
      List<String> accessIpList = securityResourceService.getAccessIpList();
  
      int result = ACCESS_DENIED;
  
      boolean abstain = accessIpList.stream().anyMatch(remoteAddress::equals);
      if (abstain) return ACCESS_ABSTAIN;
  
      if (result == ACCESS_DENIED) {
        throw new AccessDeniedException("Invalid IpAddress");
      }
  
      return result;
    }
  
  }
  ```

* **SecurityResourceService**

  ```java
  @Service
  public class SecurityResourceService  {
  
    private ResourcesRespository resourcesRepository;
    private AccessIpRepository accessIpRepository;
  
    public SecurityResourceService(ResourcesRespository resourcesRespository, AccessIpRepository accessIpRepository) {
      this.resourcesRepository = resourcesRespository;
      this.accessIpRepository = accessIpRepository;
    }
  
    ...
  
    public List<String> getAccessIpList() {
      return accessIpRepository.findAll().stream().map(AccessIp::getIpAddress).collect(Collectors.toList());
    }
  }
  ```

* **AppConfig**

  ```java
  @Configuration
  public class AppConfig {
  
    @Bean
    public SecurityResourceService securityResourceService(ResourcesRespository resourcesRespository,
                                                           AccessIpRepository accessIpRepository) {
      return new SecurityResourceService(resourcesRespository, accessIpRepository);
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
    public AccessDecisionManager affirmativeBased() {
      return new AffirmativeBased(getAccessDecisionVoters());
    }
  
    private List<AccessDecisionVoter<?>> getAccessDecisionVoters() {
  
      List<AccessDecisionVoter<?>> accessDecisionVoters = new ArrayList<>();
      // IP를 가장 먼저 검사해야 한다.
      accessDecisionVoters.add(new IpAddressVoter(securityResourceService));
      accessDecisionVoters.add(roleVoter());
  
      return accessDecisionVoters;
    }
  
   ...
  
  }
  ```