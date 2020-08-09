package japbook.jpashop.repository;

import japbook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

// 스프링 빈으로 등록시키기 위한 어노테이션
@Repository
@RequiredArgsConstructor// 엔티티 매니저 생성자 및 자동 주입 구현 가능
public class MemberRepository {

  // JPA 엔티티 매니저를 자동 주입하기 위한 어노테이션
  //  @PersistenceContext
  private final EntityManager em;

  public void save(Member member) {
    em.persist(member); // 영속성 컨텍스트에 올린다.
  }

  public Member findOne(Long id) {
    return em.find(Member.class, id);
  }

  public List<Member> findAll() {
    // JPQL 을 작성
    //  파라미터: (JPQL, 반환타입)
    return em.createQuery("select m from Member m", Member.class)
        .getResultList();

    // JPQL : 객체를 대상으로 쿼리
  }

  public List<Member> findByName(String name) {
    return em.createQuery("select m from Member m where m.name = :name", Member.class)
        .setParameter("name", name)
        .getResultList();
  }

}
