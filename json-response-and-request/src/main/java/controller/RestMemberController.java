package controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
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
      // 이 익셉션이 발생하면
      //  @ExceptionHandler 애노테이션을 사용한
      //  handleNoData() 메서드가 에러를 처리한다.
      throw new MemberNotFoundException();
    }
    // member 객체를 JSON 으로 변환한다.
    return member;
  }

//  @PostMapping("/api/members")
//  public void newMember(
//      // @RequestBody 애노테이션을 커맨드 객체에 붙이면
//      //  JSON 형식의 문자열을 해당 자바 객체로 변환한다.
//      @RequestBody @Valid RegisterRequest regReq,
//      HttpServletResponse response) throws IOException {
//    try {
//      Long newMemberId = registerService.regist(regReq);
//      response.setHeader("Location", "/api/members/" + newMemberId);
//      response.setStatus(HttpServletResponse.SC_CREATED);
//    } catch (DuplicateMemberDaoException dupEx) {
//      response.sendError(HttpServletResponse.SC_CONFLICT);
//    }
//  }

  @PostMapping("/api/members")
  public ResponseEntity<Object> newMember(
      @RequestBody @Valid RegisterRequest regReq) throws IOException {
    try {
      Long newMemberId = registerService.regist(regReq);
      URI uri = URI.create("/api/members/" + newMemberId);
      return ResponseEntity.created(uri).build();
    } catch (DuplicateMemberDaoException dupEx) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  @ExceptionHandler(MemberNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoData() {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("no member"));
  }

  public void setMemberDao(MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  public void setRegisterService(MemberRegisterService registerService) {
    this.registerService = registerService;
  }

}