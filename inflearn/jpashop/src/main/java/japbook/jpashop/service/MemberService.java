package japbook.jpashop.service;

import japbook.jpashop.domain.Member;
import japbook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor //리포지토리 생성자 주입 구현 가능
public class MemberService {

  private final MemberRepository memberRepository;

  /**
   * 회원 가입
   */
  @Transactional
  public Long join(Member member) {
    validateDuplicateMember(member); // 중복 회원 검증
    memberRepository.save(member);
    return member.getId();
  }

  private void validateDuplicateMember(Member member) {
    List<Member> findMembers = memberRepository.findByName(member.getName());
    if (!findMembers.isEmpty()) {
      throw new IllegalStateException("이미 존재하는 회원입니다.");
    }
  }

  // 회원 전체 조회
  public List<Member> findMembers() {
    return memberRepository.findAll();
  }

  public Member findOne(Long memberId) {
    return memberRepository.findOne(memberId);
  }

}
