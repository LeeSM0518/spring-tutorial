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