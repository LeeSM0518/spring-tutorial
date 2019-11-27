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
