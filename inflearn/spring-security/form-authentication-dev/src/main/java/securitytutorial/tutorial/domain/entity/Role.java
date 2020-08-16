package securitytutorial.tutorial.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ROLE")
@ToString(exclude = {"users", "resourcesSet"})
@EqualsAndHashCode(of = "id")
public class Role implements Serializable {

  @Id
  @GeneratedValue
  @Column(name = "role_id")
  private Long id;

  @Column(name = "role_name")
  private String roleName;

  @Column(name = "role_desc")
  private String roleDesc;

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "roleSet")
  @OrderBy("ordernum desc")
  private Set<Resources> resourcesSet = new LinkedHashSet<>();

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "userRoles")
  private Set<Account> accounts = new HashSet<>();

}
