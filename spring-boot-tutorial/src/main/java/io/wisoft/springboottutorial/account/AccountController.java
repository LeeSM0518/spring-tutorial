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
