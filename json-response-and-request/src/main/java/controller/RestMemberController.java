package controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import spring.Member;
import spring.MemberDao;
import spring.MemberRegisterService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// @Controller 애노테이션 대신 @RestController 애노테이션 사
@RestController
public class RestMemberController {

  private MemberDao memberDao;
  private MemberRegisterService registerService;

  @GetMapping("/api/members")
  // 요청 매핑 애노테이션 적용 메서드의 리턴 타입으로
  //  일반 객체 사용
  public List<Member> members() {
    return memberDao.selectAll();
  }

  @GetMapping("/api/members/{id}")
  // 요청 매핑 애노테이션 적용 메서드의 리턴 타입으로
  //  일반 객체 사용
  public Member member(@PathVariable Long id,
                       HttpServletResponse response) throws IOException {
    Member member = memberDao.selectById(id);
    if (member == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return null;
    }
    return member;
  }

  public void setMemberDao(MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  public void setRegisterService(MemberRegisterService registerService) {
    this.registerService = registerService;
  }

}