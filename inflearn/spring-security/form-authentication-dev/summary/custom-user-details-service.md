# CustomUserDetailsService

 ![image](https://user-images.githubusercontent.com/43431081/90108671-9fd2c280-dd85-11ea-9217-f6c2b90ccded.png)

<br>

## 예제

* **CustomDetailService**

  ```java
  @Service("userDetailsService")
  public class CustomDetailService implements UserDetailsService {
  
    @Autowired
    private UserRepository userRepository;
  
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      // 해당 아이디의 회원 조회
      Account account = userRepository.findByUsername(username);
  
      // 일치하는 회원이 없어서 조회가 되지 않는다면,
      if (account == null) {
        // 유저가 없다는 예외 던짐
        throw new UsernameNotFoundException("UsernameNotFoundException");
      }
  
      // 권한 리스트
      List<GrantedAuthority> roles = new ArrayList<>();
      roles.add(new SimpleGrantedAuthority(account.getRole())); // 권한 추가
      // 인증 객체 생성
  
      return new AccountContext(account, roles);
    }
  }
  ```

* **AccountContext** : `UserDetails` 를 반환하기 위해 `User` 를 상속한 `AccountContext` 를 구현

  ```java
  public class AccountContext extends User {
  
    private final Account account;
  
    public AccountContext(Account account, Collection<? extends GrantedAuthority> authorities) {
      super(account.getUsername(), account.getPassword(), authorities);
      this.account = account;
    }
  
    public Account getAccount() {
      return account;
    }
  
  }
  ```

* **SecurityConfig**

  ```java
  @Configuration
  @EnableWebSecurity
  @Slf4j
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    // 메모리 사용자 방식 제거
    //  @Override
    //  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    //
    //    String password = passwordEncoder().encode("1111");
    //
    //    System.out.println(password);
    //
    //    auth.inMemoryAuthentication().withUser("user").password(password).roles("USER");
    //    auth.inMemoryAuthentication().withUser("manager").password(password).roles("MANAGER, USER");
    //    auth.inMemoryAuthentication().withUser("admin").password(password).roles("ADMIN", "USER", "MANAGER");
    //  }
  
    @Autowired
    private UserDetailsService userDetailsService;
  
    @Override
    // 커스텀 인증 처리 서비스 등록
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.userDetailsService(userDetailsService);
    }
  
    ...
  
  }
  ```

  