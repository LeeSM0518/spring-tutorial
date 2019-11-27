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
