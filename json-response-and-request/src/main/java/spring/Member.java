package spring;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

public class Member {

  private Long id;
  private String email;
  @JsonIgnore
  private String password;
  private String name;
  // ISO-8601 형식으로 변환
//  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @JsonFormat(pattern = "yyyyMMddHHmmss")
  private LocalDateTime registerDateTime;

  public Member(String email, String password, String name, LocalDateTime registerDateTime) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.registerDateTime = registerDateTime;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDateTime getRegisterDateTime() {
    return registerDateTime;
  }

  public void setRegisterDateTime(LocalDateTime registerDateTime) {
    this.registerDateTime = registerDateTime;
  }

  // 암호 변경 기능을 구현
  public void changePassword(String oldPassword, String newPassword) {
    if (!password.equals(oldPassword))
      throw new WrongPasswordException();
    this.password = newPassword;
  }

  public boolean matcherPassword(String password) {
    return this.password.equals(password);
  }

}
