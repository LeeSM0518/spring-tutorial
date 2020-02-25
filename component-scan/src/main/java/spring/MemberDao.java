package spring;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberDao {

  public MemberDao() {
    System.out.println("Component 생성");
    this.setName("component 생성");
  }

  public String name;

  public void setName(String name) {
    this.name = name;
  }

  private static long nextId = 0;

  private Map<String, Member> map = new HashMap<>();

  public Member selectByEmail(String email) {
    return map.get(email);
  }

  public void insert(Member member) {
    member.setId(++nextId);
    map.put(member.getEmail(), member);
  }

  public void update(Member member) {
    map.put(member.getEmail(), member);
  }

  public Collection<Member> selectAll() {
    return map.values();
  }

}
