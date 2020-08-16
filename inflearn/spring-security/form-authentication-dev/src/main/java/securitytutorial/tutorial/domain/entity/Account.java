package securitytutorial.tutorial.domain.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

// JPA가 관리하는 엔티티
@Entity
/**
 * @Data
 *  Getter
 *  Setter
 *  RequiredArgsConstructor
 *  ToString
 *  EqualsAndHashCode
 *  lombok.Value
 */
@Data
public class Account {

  @Id
  @GeneratedValue
  private Long id;
  private String username;
  private String password;
  private String email;
  private String age;
  private String role;

}
