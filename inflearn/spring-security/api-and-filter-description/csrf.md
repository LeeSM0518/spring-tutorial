# CSRF & CsrfFilter

## CSRF 란?

![image](https://user-images.githubusercontent.com/43431081/89982432-ac391b80-dcb0-11ea-8719-36735d6e47b2.png)

<br>

## CsrfFilter 란?

![image](https://user-images.githubusercontent.com/43431081/89982460-c07d1880-dcb0-11ea-9d42-84497b078516.png)

<br>

## CSRF 설정

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
  http
    .authorizeRequests()
    .anyRequest().permitAll();
  http
    .formLogin();
  http
    .csrf(); // CSRF 설정 (default가 설정임)
}
```