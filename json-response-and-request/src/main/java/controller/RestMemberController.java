package controller;

import org.springframework.web.bind.annotation.*;
import spring.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
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

  @PostMapping("/api/members")
  public void newMember(
      // @RequestBody 애노테이션을 커맨드 객체에 붙이면
      //  JSON 형식의 문자열을 해당 자바 객체로 변환한다.
      @RequestBody @Valid RegisterRequest regReq,
      HttpServletResponse response) throws IOException {
    try {
      Long newMemberId = registerService.regist(regReq);
      response.setHeader("Location", "/api/members/" + newMemberId);
      response.setStatus(HttpServletResponse.SC_CREATED);
    } catch (DuplicateMemberDaoException dupEx) {
      response.sendError(HttpServletResponse.SC_CONFLICT);
    }
  }

  public void setMemberDao(MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  public void setRegisterService(MemberRegisterService registerService) {
    this.registerService = registerService;
  }

}