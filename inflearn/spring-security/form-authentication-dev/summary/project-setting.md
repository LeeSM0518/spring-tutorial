# 실전 프로젝트 구성

* 프로그램 설치 필요
  * DB - PostgreSQL

<br>

* **build.gradle**

  ```java
  plugins {
      id 'org.springframework.boot' version '2.3.2.RELEASE'
      id 'io.spring.dependency-management' version '1.0.9.RELEASE'
      id 'java'
  }
  
  ext["h2.version"] = "1.4.199"
  
  group = 'security-tutorial'
  version = '0.0.1-SNAPSHOT'
  sourceCompatibility = '11'
  
  configurations {
      compileOnly {
          extendsFrom annotationProcessor
      }
  }
  
  repositories {
      mavenCentral()
  }
  
  dependencies {
      implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
      implementation 'org.springframework.boot:spring-boot-starter-security'
      implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
      implementation 'org.springframework.boot:spring-boot-starter-web'
      compileOnly 'org.projectlombok:lombok'
      developmentOnly 'org.springframework.boot:spring-boot-devtools'
      runtimeOnly 'com.h2database:h2'
      runtimeOnly 'org.postgresql:postgresql'
      annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor'
      annotationProcessor 'org.projectlombok:lombok'
      testImplementation('org.springframework.boot:spring-boot-starter-test') {
          exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
      }
      testImplementation 'org.springframework.security:spring-security-test'
      implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE'
      implementation 'org.modelmapper:modelmapper:2.3.8'
  }
  
  test {
      useJUnitPlatform()
  }
  
  ```

<br>

## 기본 보안 세팅

* **security.configs.SecurityConfig**

  ```java
  package securitytutorial.tutorial.security.configs;
  
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
  import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
  import org.springframework.security.crypto.factory.PasswordEncoderFactories;
  import org.springframework.security.crypto.password.PasswordEncoder;
  
  @Configuration
  @EnableWebSecurity
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
  
      String password = passwordEncoder().encode("1111");
  
      System.out.println(password);
  
      auth.inMemoryAuthentication().withUser("user").password(password).roles("USER");
      auth.inMemoryAuthentication().withUser("manager").password(password).roles("MANAGER");
      auth.inMemoryAuthentication().withUser("admin").password(password).roles("ADMIN");
    }
  
    @Bean
    public PasswordEncoder passwordEncoder() {
      // 비밀번호 암호화
      return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
  
    @Override
    protected void configure(HttpSecurity http) throws Exception {
      http
          .authorizeRequests()
          .antMatchers("/").permitAll()
          .antMatchers("/mypage").hasRole("USER")
          .antMatchers("/manages").hasRole("MANAGER")
          .antMatchers("/config").hasRole("ADMIN")
          .anyRequest().authenticated()
  
          .and()
          .formLogin();
    }
  
  }
  ```

  