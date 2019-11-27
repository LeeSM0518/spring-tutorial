# Spring 세미나

**MVC 패턴**

* **M** : Model
* **V** : View
* **C** : Controller

<br>

**Spring**

* **Model**
  * Service : 비즈니스 로직
  * Repository : database 관련 처리
* **Controller**
  * 유저의 요청 처리
* **View는 없다** : RESTful API를 사용할 것이기 때문에 UI는 관리하지 않는다.

<br>

# 실습

* **/account/Account.java**

  ```java
  package io.wisoft.springboottutorial.account;
  
  import org.hibernate.validator.constraints.Length;
  import org.hibernate.validator.constraints.UniqueElements;
  
  import javax.persistence.*;
  
  @Entity // JPA Object Relation Mapping 객체, 데이터베이스 엔티티와 매핑
  public class Account {
  
    @Id // 식별자 표시, 테이블이 존재하지 않을 시 primary key 를 만들어준다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 값 자동 증가
    private Long id;  // 사용자가 많을 수 있기 때문에 Long
  
    @Column // 컬럼 표시
    @Length(min=0, max=30) // 문자열 길이 제한
    private String email;
  
    @Column
    @Length(min=0, max=10)
    private String name;
  
    @Column
    @Length(min=0, max = 70)
    private String password;
  
    public Account(Long id, String email, String name, String password) {
      this.id  = id;
      this.email = email;
      this.name = name;
      this.password = password;
    }
  
    public Account(String email, String name, String password) {
      this.email = email;
      this.name = name;
      this.password = password;
    }
  
    public Account(){}
  
    // GET 요청시 데이터가 출력되지 않아서 getter 추가
    public String getName() {
      return name;
    }
  
    public Long getId() {
      return id;
    }
  
    public String getEmail() {
      return email;
    }
  
    public String getPassword() {
      return password;
    }
  
    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("Account{");
      sb.append("id=").append(id);
      sb.append(", email='").append(email).append('\'');
      sb.append(", name='").append(name).append('\'');
      sb.append(", password='").append(password).append('\'');
      sb.append('}');
      return sb.toString();
    }
  }
  ```

* **/account/AccountController.java**

  ```java
  package io.wisoft.springboottutorial.account;
  
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.http.HttpStatus;
  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.*;
  
  import javax.validation.Valid;
  import java.util.List;
  
  // 컨트롤러 명시
  @RestController
  // 요청 매핑
  @RequestMapping("accounts")
  public class AccountController {
  
    private AccountService accountService;
  
    @Autowired  // 스프링 컨테이너에서 자동으로 의존성 주입할 수 있도록 하기 위해
    public AccountController(AccountService accountService) {
      this.accountService = accountService;
    }
  
    @GetMapping
    public ResponseEntity<List<Account>> getAccounts() {
      List<Account> accounts = accountService.getAccounts();
      return new ResponseEntity<>(accounts, HttpStatus.OK);
    }
  
    @GetMapping("{account-id}")
    public ResponseEntity<Account> getAccountById(@PathVariable("account-id") Long id) {
      System.out.println(id);
      Account account = accountService.getAccountById(id);
      System.out.println(account);
      return new ResponseEntity<>(account, HttpStatus.OK);
    }
  
    @PostMapping
  //   Valid 검사 : 값의 형식이나 길이에 맞는지 유효성 검사
    public ResponseEntity<Account> insertAccount(@RequestBody @Valid AccountDto accountDto) {
      Account result = accountService.insertAccount(accountDto);
      return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
  
    @PutMapping("{account-id}")
    public ResponseEntity<Account> updateAccountById(@PathVariable("account-id") Long id,
                                                     @RequestBody @Valid AccountDto accountDto) {
      Account result = accountService.updateAccountById(id, accountDto);
      return new ResponseEntity<>(result, HttpStatus.OK);
    }
  
    @DeleteMapping("{account-id}")
    public ResponseEntity<String> deleteAccountById(@PathVariable("account-id") Long id) {
      accountService.deleteAccountById(id);
      return new ResponseEntity<>("삭제가 완료되었습니다.", HttpStatus.OK);
    }
  
  }
  ```

* **/account/AccountDto.java**

  ```java
  package io.wisoft.springboottutorial.account;
  
  import javax.validation.constraints.Email;
  import javax.validation.constraints.Max;
  import javax.validation.constraints.NotNull;
  import javax.validation.constraints.Size;
  
  public class AccountDto {
  
    @NotNull
    private String name;
  
    @NotNull
    @Email
    private String email;
  
    @NotNull
    @Size(min = 0, max = 70)
    private String password;
  
    public AccountDto(@NotNull String name, @NotNull @Email
        String email, @NotNull @Size(min = 0, max = 70) String password) {
      this.name = name;
      this.email = email;
      this.password = password;
    }
  
    public String getName() {
      return name;
    }
  
    public String getEmail() {
      return email;
    }
  
    public String getPassword() {
      return password;
    }
  
  }
  ```

* **/account/AccountRepository.java**

  ```java
  package io.wisoft.springboottutorial.account;
  
  import org.springframework.data.jpa.repository.JpaRepository;
  import org.springframework.stereotype.Repository;
  
  @Repository
  public interface AccountRepository extends JpaRepository<Account, Long> { // <연결객체, primary key 타입>
    
  }
  ```

* **/account/AccountService.java**

  ```java
  package io.wisoft.springboottutorial.account;
  
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
  import org.springframework.stereotype.Service;
  
  import java.util.List;
  import java.util.Optional;
  
  @Service
  public class AccountService {
  
    private AccountRepository accountRepository;
    private BCryptPasswordEncoder passwordEncoder;
  
    @Autowired
    public AccountService(AccountRepository accountRepository, BCryptPasswordEncoder passwordEncoder) {
      this.accountRepository = accountRepository;
      this.passwordEncoder = passwordEncoder;
    }
  
    public List<Account> getAccounts() {
      List<Account> accounts = accountRepository.findAll();
      return accounts;
    }
  
    public Account getAccountById(Long id) {
      Optional<Account> account = accountRepository.findById(id);
      System.out.println(account);
  //    System.out.println(account.get().getName());;
      return account.orElseGet(Account::new);
    }
  
    public Account insertAccount(AccountDto accountDto) {
      String password = passwordEncoder.encode(accountDto.getPassword());
      System.out.println(password);
      Account account = new Account(accountDto.getEmail(), accountDto.getName(), password);
      return accountRepository.save(account);
    }
  
    public Account updateAccountById(Long id, AccountDto accountDto) {
      if (accountRepository.existsById(id)) {
        Account result = new Account(id, accountDto.getEmail(), accountDto.getName(), accountDto.getPassword());
        return accountRepository.save(result);
      }
      return new Account();
    }
  
    public void deleteAccountById(Long id) {
      accountRepository.deleteById(id);
    }
  }
  ```

* **/SpringBootTutorialApplication.java**

  ```java
  package io.wisoft.springboottutorial;
  
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.context.annotation.Bean;
  import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
  
  @SpringBootApplication
  public class SpringBootTutorialApplication {
  
    public static void main(String[] args) {
      SpringApplication.run(SpringBootTutorialApplication.class, args);
    }
  
    @Bean
    public BCryptPasswordEncoder encoder() {
      return new BCryptPasswordEncoder();
    }
  
  }
  ```

* **/WebSecurityConfig.java**

  ```java
  package io.wisoft.springboottutorial;
  
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
  import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
  
  @EnableWebSecurity
  @Configuration
  // 암호화 lib 추가로 에러가 발생하여 추가한 클래스
  public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http.csrf().disable();
    }
  
  }
  ```

* **/resources/application.yml**

  ```yml
  server:
    port: 8088
  
  spring:
    profiles:
  #    개발자 모드
      active: development
  
    datasource:
  #    JDBC 드라이버 이름
      driver-class-name: org.postgresql.Driver
      url: jdbc:postgresql://arjuna.db.elephantsql.com:5432/fsmfppcj
      username: ******
      password: ******
  
  #    JPA 설정
    jpa:
      hibernate:
        ddl-auto: none
      show-sql: true
      database: postgresql
      database-platform: org.hibernate.dialect.PostgreSQLDialect
      generate-ddl: false
      properties:
        temp:
          user_jdbc_metadata_defaults: false
        jdbc:
          lob:
            non_contextual_creation: trueㅌ
  ```