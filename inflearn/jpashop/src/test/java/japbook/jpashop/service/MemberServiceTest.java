package japbook.jpashop.service;

import japbook.jpashop.domain.Member;
import japbook.jpashop.repository.MemberRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)  // 스프링과 테스트 통합
@SpringBootTest               // 스프링 부트 띄우고 테스트(이게 없으면 @Autowired 다 실패)
@Transactional                // 반복 가능한 테스트 지원, 각각의 테스트를 실행할 때마다 트랜잭션을 시작하고
                              //   테스트가 끝나면 트랜잭션을 강제로 롤백
public class MemberServiceTest {

  @Autowired
  MemberService memberService;
  @Autowired
  MemberRepository memberRepository;
  @Autowired
  EntityManager em;

  @Test
  @Rollback(false) // 롤백을 하지 않도록 한다.
  public void 회원가입() throws Exception {
    // given
    Member member = new Member();
    member.setName("kim20");

    // when
    //  @Transactional 때문에 Commit이 호출이 되지 않아서 insert를 날리지 않는다.
    //  영속성 컨텍스트가 flush를 하지 않는다.
    Long savedId = memberService.join(member);

    // then
//    em.flush(); // 영속성 컨텍스트 적용(DB 적용)
    assertEquals(member, memberRepository.findOne(savedId));
  }

  @Test(expected = IllegalStateException.class)
  public void 중복_회원_예외() throws Exception {
    // given
    Member member1 = new Member();
    member1.setName("kim");

    Member member2 = new Member();
    member2.setName("kim");

    // when
    memberService.join(member1);
    memberService.join(member2); // 예외가 발생해야 한다!!!

    // then
    fail("예외가 발생해야 한다.");
  }

}