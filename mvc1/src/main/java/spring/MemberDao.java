package spring;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import spring.rowmapper.MemberRowMapper;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public class MemberDao {

  private JdbcTemplate jdbcTemplate;

  public MemberDao(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  //  public Member selectByEmail(String email) {
//    List<Member> results = jdbcTemplate.query(
//        // JdbcTemplate의 query() 메서드를 이용해서 쿼리 실행
//        "select * from MEMBER where EMAIL = ?",
//        // 람다식을 이용해서 RowMapper의 객체를 전달한다.
//        (rs, rowNum) -> new Member(rs.getString("EMAIL"),
//            rs.getString("PASSWORD"),
//            rs.getString("NAME"),
//            rs.getTimestamp("REGDATE").toLocalDateTime())
//        // 인덱스 파라미터(물음표)를 email 로 결정한다.
//        , email);
//    // results가 비어 있는 경우와 그렇지 않은 경우를 구분해서 리턴 값을 처리
//    return results.isEmpty() ? null : results.get(0);
//  }
  public Member selectByEmail(String email) {
    List<Member> results = jdbcTemplate.query(
        "select * from MEMBER where EMAIL = ?",
        new MemberRowMapper()
        , email);
    return results.isEmpty() ? null : results.get(0);
  }

  //  public void insert(Member member) {
//    // GeneratedKeyHolder : 자동 생성된 키값을 구해주는 KeyHolder 구현 클래스이다.
////    KeyHolder keyHolder = new GeneratedKeyHolder();
//    // PreparedStatement 객체와 KeyHolder 객체를 파라미터로 갖는다.
//    jdbcTemplate.update(conn -> {
//      // 람다식을 통해서 PreparedStatement 객체를 구현해서 넘기고,
//      //  prepareStatement() 메서드의 두 번째 파라미터로 String 배열을 넘기는데
//      //  이 두 번째 파라미터는 자동 생성되는 키 칼럼 목록을 지정할 때 사용한다.
//      PreparedStatement pstmt = conn.prepareStatement(
//          "insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) " +
//              "values (?, ?, ?, ?)",
//          new String[] {"ID"});
//      pstmt.setString(1, member.getEmail());
//      pstmt.setString(2, member.getPassword());
//      pstmt.setString(3, member.getName());
//      pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
//      return pstmt;
////    }, keyHolder);
//    });
//    // KeyHolder에 보관된 키값을 getKey() 메서드를 이용해서 구한다.
////    Number keyValue = keyHolder.getKey();
////    member.setId(keyValue.longValue());
//  }
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
//    jdbcTemplate.update(
//        (con) -> {
//          PreparedStatement pstmt = con.prepareStatement(
//              "insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) values (?,?,?,?)"
//          );
//          pstmt.setString(1, member.getEmail());
//          pstmt.setString(2, member.getPassword());
//          pstmt.setString(3, member.getName());
//          pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
//          pstmt.addBatch();
//          // 생성한 PreparedStatement 객체 리턴
//          return pstmt;
//        });
  }

  //  public List<Member> selectAll() {
//    return jdbcTemplate.query("select * from MEMBER",
//        (rs, rowNum) -> {
//      Member member = new Member(
//          rs.getString("EMAIL"),
//          rs.getString("PASSWORD"),
//          rs.getString("NAME"),
//          rs.getTimestamp("REGDATE").toLocalDateTime());
//      member.setId(rs.getLong("ID"));
//      return member; });
//  }
  public List<Member> selectAll() {
    return jdbcTemplate.query("select * from MEMBER",
        new MemberRowMapper());
  }

  public int count() {
    return jdbcTemplate.queryForObject(
        "select count(*) from MEMBER", Integer.class);
  }

}
