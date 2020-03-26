package spring;

public class AuthService {

  private MemberDao memberDao;

  public void setMemberDao(MemberDao memberDao) {
    this.memberDao = memberDao;
  }

  public AuthInfo authenticate(String email, String password) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) {
      throw new WrongPasswordException();
    }
    if (!member.matcherPassword(password)) {
      throw new WrongPasswordException();
    }

    return new AuthInfo(member.getId(),
        member.getEmail(),
        member.getName());
  }

}
