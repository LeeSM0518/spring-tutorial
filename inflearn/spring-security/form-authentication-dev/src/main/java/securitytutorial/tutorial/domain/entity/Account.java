package securitytutorial.tutorial.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
@ToString(exclude = {"userRoles"})
@Builder
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {

  @Id
  @GeneratedValue
  private Long id;

  @Column
  private String username;

  @Column
  private String password;

  @Column
  private String email;

  @Column
  private int age;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(name = "account_roles", joinColumns = @JoinColumn(name = "account_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> userRoles = new HashSet<>();

}
