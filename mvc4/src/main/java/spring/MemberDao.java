package spring;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import spring.rowmapper.MemberRowMapper;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public class MemberDao {

  private JdbcTemplate jdbcTemplate;

  public MemberDao(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public Member selectByEmail(String email) {
    List<Member> results = jdbcTemplate.query(
        "select * from MEMBER where EMAIL = ?",
        new MemberRowMapper()
        , email);
    return results.isEmpty() ? null : results.get(0);
  }

  public void insert(Member member) {
    jdbcTemplate.update("insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) " +
            "values (?, ?, ?, ?)",
        member.getEmail(), member.getPassword(), member.getName(),
        Timestamp.valueOf(member.getRegisterDateTime()));
  }

  public void update(Member member) {
    jdbcTemplate.update(
        "update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
        member.getName(), member.getPassword(), member.getEmail());
  }

  public List<Member> selectAll() {
    return jdbcTemplate.query("select * from MEMBER",
        new MemberRowMapper());
  }

  public int count() {
    return jdbcTemplate.queryForObject(
        "select count(*) from MEMBER", Integer.class);
  }

  public List<Member> selectByRegdate(
      LocalDateTime from, LocalDateTime to) {
    String fromDateTime = from.toLocalDate().toString() + " " + from.toLocalTime().toString();
    System.out.println();
    System.out.println("test");
    System.out.println(fromDateTime);
    String toDateTime = to.toLocalDate().toString() + " " + to.toLocalTime().toString();
    System.out.println(toDateTime);
    System.out.println();
    return jdbcTemplate.query(
        "select from MEMBER where REGDATE between ? and ? " +
            "order by REGDATE desc",
        (rs, rowNum) -> {
          Member member = new Member(
              rs.getString("EMAIL"),
              rs.getString("PASSWORD"),
              rs.getString("NAME"),
              rs.getTimestamp("REGDATE").toLocalDateTime());
          member.setId(rs.getLong("ID"));
          return member;
        },
        from.toLocalDate(), toDateTime);
  }

}
