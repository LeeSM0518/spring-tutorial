package securitytutorial.tutorial.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ROLE_HIERARCHY")
// 권한 계층을 위한 도메인
public class RoleHierarchy implements Serializable {

  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "child_name")
  private String childName;

  @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_name", referencedColumnName = "child_name")
  private RoleHierarchy parentName;

  @OneToMany(mappedBy = "parentName", cascade = CascadeType.ALL)
  private Set<RoleHierarchy> roleHierarchy = new HashSet<>();

}
