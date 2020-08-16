# PasswordEncoder

* 비밀번호를 안전하게 암호화 하도록 제공
* Spring Security 5.0 이전에는 기본 PasswordEncoder 가 평문을 지원하는 NoOpPasswordEncoder(현재는 Deprecated)

<br>

### 생성 방법

```java
PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
```

* 여러개의 PasswordEncoder 유형을 선언한 뒤, 상황에 맞게 선택해서 사용할 수 있도록 지원하는 Encoder 이다.

<br>

### 암호화 포맷 : {id}encodedPassword

* 기본 포맷은 Bcrypt: {bcrypt}~~
* 알고리즘 종류: bcrypt, noop, pbkdf2, scrypt, sha256

<br>

### 인터페이스

* `encode(password)`
  * 패스워드 암호화
* `matchers(rawPassword, encodedPassword)`
  * 패스워드 비교

<br>

## 예시

```java
@PostMapping("/users")
public String createUser(AccountDto accountDto) {

  ModelMapper modelMapper = new ModelMapper();
  Account account = modelMapper.map(accountDto, Account.class);
  account.setPassword(passwordEncoder.encode(account.getPassword()));
  userService.createUser(account);

  return "redirect:/";
}
```

