#  Chapter 08. DB 연동

* **이 장에서 다룰 내용**
  * DataSource 설정
  * Jdbc Template을 이용한 쿼리 실행
  * DB 관련 익셉션 변환 처리
  * 트랜잭션 처리

<br>

# 1. JDBC 프로그래밍의 단점을 보완하는 스프링

JDBC API를 이용하면 DB 연동에 필요한 Connection을 구한 다음 쿼리를 실행하기 위한 PreparedStatement를 생성한다. 그리고 쿼리를 실행한 뒤에느 finally 블록에서 ResultSet, PreparedStatement, Connection을 닫는다.

여기서 문제는 Connection, PreparedStatement, ResultSet을 생성하고 닫는 코드가 구조적으로 반복되는 것이다.

구조적인 반복을 줄이기 위한 방법은 **템플릿 메서드 패턴과 전략 패턴을** 함께 사용하는 것이다. 스프링은 이 두 패턴을 엮은 **JdbcTemplate 클래스를** 제공한다.

<br>

* **JdbcTemplate 클래스를 사용한 코드**

  ```java
  List<Member> results = jdbcTemplate.query(
    "select * from MEMBER where EMAIL = ?",
    new RowMapper<Member>() {
     @Override
      public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        Member member = new Member(rs.getString("EMAIL"),
                                  rs.getString("PASSWORD"),
                                  rs.getString("NAME"),
                                  rs.getTimestamp("REGDATE"));
        member.setId(rs.getLong("ID"));
        return member;
      }
    },
    email);
  return results.isEmpty() ? null : results.get(0);
  ```

* **위의 코드에서 람다를 이용한 코드**

  ```java
  List<Member> results = jdbcTemplate.query(
    "select * from MEMBER where EMAIL = ?",
    (ResultSet rs, int rowNum) -> {
      Member member = new Member(rs.getString("EMAIL"),
                                rs.getString("PASSWORD"),
                                rs.getString("NAME"),
                                rs.getTimestamp("REGDATE"));
      member.setId(rs.getLong("ID"));
      return member;
    },
    email);
  return results.isEmpty() ? null : results.get(0);
  ```

<br>

스프링이 제공하는 또 다른 장점은 트랜잭션 관리가 쉽다는 것이다.

JDBC API로 트랜잭션을 처리하려면 Connection의 setAutoCommit(false)을 이용해서 자동 커밋을 비활성화하고 commit() 과 rollback() 메서드를 이용해서 트랜잭션을 커밋하거나 롤백해야 한다.

스프링을 사용하면 트랜잭션을 적용하고 싶은 메서드에 **@Transactional 애노테이션을** 붙이기만 하면 된다.

```java
@Transactional
public void insert(Member member) {
  ...
}
```

> 커밋과 롤백 처리는 스프링이 알아서 처리한다.

<br>

# 2. 프로젝트 준비

## 2.1. 프로젝트 생성

* **dependency 추가**

  ```xml
  plugins {
      id 'java'
  }
  
  group 'org.example'
  version '1.0-SNAPSHOT'
  
  sourceCompatibility = 1.8
  
  repositories {
      mavenCentral()
  }
  
  dependencies {
      testCompile group: 'junit', name: 'junit', version: '4.12'
      implementation 'org.springframework:spring-context:5.2.4.RELEASE'
      implementation 'org.springframework:spring-jdbc:5.2.4.RELEASE'
      implementation 'org.apache.tomcat:tomcat-jdbc:10.0.0-M1'
      implementation 'org.lucee:postgresql:8.3-606.jdbc4'
  }
  ```

<br>

### 커넥션 풀이란?

자파 프로그램에서 DBMS로 커넥션을 생성하는 시간은 매우 길기 때문에 DB 커넥션을 생성하는 시간은 전체 성능에 영향을 줄 수 있다.

최초 연결에 따른 응답 속도 저하와 동시 접속자가 많을 때 발생하는 부하를 줄이기 위해 사용하는 것이 **커넥션 풀이다.** 커넥션 풀은 **일정 개수의 DB 커넥션을 미리 만들어두는 기법이다.**

DB 커넥션이 필요한 프로그램은 커넥션 풀에서 커넥션을 가져와 사용한 뒤 커넥션을 다시 풀에 반납한다.

DB 커넥션 풀 기능을 제공하는 모듈로는 Tomcat JDBC, HikariCP, DBCP, c3p0 등이 존재한다.

<br>

DB를 사용해서 MemberDao 클래스를 구현할 것이므로 이전 코드를 삭제하고 새로 작성해보자.

* **/spring/MembeDao.java**

  ```java
  public class MemberDao {
  
    public Member selectByEmail(String email) {
      return null;
    }
    
    public void insert(Member member) {
      
    }
    
    public void update(Member member) {
      
    }
    
    public Collection<Member> selectAll() {
      return null;
    }
    
  }
  ```

<br>

## 2.2. DB 테이블 생성

* **Member Table**

  ```sql
  create table MEMBER (
    ID serial primary key,
    EMAIL varchar(255) unique,
    PASSWORD varchar(100),
    NAME varchar(100),
    REGDATE timestamp
  );
  
  insert into MEMBER values
  (DEFAULT, 'nalsm98@test.com', '1234', 'min', now());
  ```

<br>

# 3. DataSource 설정

스프링이 제공하는 DB 연동 기능은 DataSource를 사용해서 DB Connection을 구한다. DB 연동에 사용할 **DataSource를 스프링 빈으로 등록하고** DB 연동 기능을 구현한 빈 객체는 **DataSource를 주입받아 사용한다.**

* **/config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
  
    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
      // DataSource 객체 생성
      DataSource ds = new DataSource();
      // JDBC 드라이버 클래스 지정.
      ds.setDriverClassName("org.postgresql.Driver");
      // DB 연결할 때 사용할 URL, 계정, 암호 설정
      ds.setUrl("jdbc:postgresql://arjuna.db.elephantsql.com:5432/");
      ds.setUsername("****");
      ds.setPassword("****");
      
      ds.setInitialSize(2);
      ds.setMaxActive(10);
      return ds;
    }
  
  }
  ```

<br>

## 3.1. Tomcat JDBC의 주요 프로퍼티

Tomcat JDBC 모듈의 Datasource 클래스는 커넥션 풀 기능을 제공하는 클래스이다.

DataSource 클래스는 커넥션을 몇 개 만들지 지정할 수 있는 메서드를 제공한다.

<br>

* **Tomcat JDBC DataSource 클래스의 주요 프로퍼티**

| 설정 메서드                           | 설명                                                         |
| ------------------------------------- | ------------------------------------------------------------ |
| setInitialSize(int)                   | 커넥션 풀을 초기화할 때 생성할 초기 커넥션 개수를 지정한다(기본 10). |
| setMaxActive(int)                     | 커넥션 풀에서 가져올 수 있는 최대 커넥션 개수(기본값 100).   |
| setMaxIdle(int)                       | 커넥션 풀에 유지할 수 있는 최대 커넥션 개수(기본값 100).     |
| setMinIdle(int)                       | 커넥션 풀에 유지할 최소 커넥션 개수(기본값 initialSize).     |
| setMaxWait(int)                       | 커넥션 풀에서 커넥션을 가져올 때 대기할 최대 시간(밀리초 단위, 기본 값 30000(30초)) |
| setMaxAge(long)                       | 최초 커넥션 연결 후 커넥션의 최대 유효 시간을 밀리초 단위로 지정(기본 값 0, 유효 시간 없음을 의미) |
| setValidationQuery(String)            | 커넥션이 유효한지 검사할 때 사용할 쿼리를 지정(기본값 null)  |
| setValidationQueryTimeout(int)        | 검사 쿼리의 최대 실행 시간을 초 단위로 지정.<br />이 시간을 초과하면 검사에 실패(기본값 -1, 비활성화 상태) |
| setTestOneBorrow(boolean)             | 풀에서 커넥션을 가져올 때 검사 여부를 지정(기본값 false)     |
| setTestOnReturn(boolean)              | 풀에 커넥션을 반환할 때 검사 여부를 지정(기본값 false)       |
| setTestWhileIdle(boolean)             | 커넥션이 풀에 유휴 상태로 있는 동안에 검사할지 여부를 지정(기본값 false) |
| setMinEvictableIdleTimeMillis(int)    | 커넥션 풀에 유휴 상태로 유지할 최소 시간을 밀리초 단위로 지정.<br />testWhileIdle이 true 이면 유휴 시간이 이값을 초과한 커넥션을<br />풀에서 제거한다. 기본값은 60000밀리초(60초) |
| setTimeBetweenEvictionRunsMillis(int) | 커넥션 풀의 유휴 커넥션을 검사할 주기를 밀리초 단위로 지정(기본값 5000밀리초). 1초 이하로 설정하면 안된다. |

커넥션 풀은 커넥션을 생성하고 유지한다. 커넥션 풀에 커넥션을 요청하면 해당 커넥션은 **활성(active) 상태가** 되고, 커넥션을 다시 커넥션 풀에 반환하면 **유휴(idle) 상태가** 된다. 

**maxActive는** 활성 상태가 가능한 최대 커넥션 개수를 지정한다. 정해진 maxActive 만큼 커넥션이 사용되고 있을 때 커넥션을 요청하면 다른 커넥션이 반환될 때 까지 대기한다. 이 대기 시간이 **maxWait 이다.** 대기 시간 내에 반환된 커넥션이 없으면 **익셉션이 발생한다.**

커넥션 풀을 사용하면 미리 커넥션을 생성했다가 필요할 때에 커넥션을 꺼내 쓰므로 전체 응답 시간이 짧아진다. 그래서 커넥션 풀을 초기화할 때 최소 수준의 커넥션을 미리 생성하는 것이 좋다. 이때 **생성할 커넥션 개수를 initialSize로 지정한다.**

커넥션 풀에 생성된 커넥션은 지속적으로 재사용된다. 그런데 한 커넥션이 **영원히 유지되는 것은 아니다.** 일정 시간 내에 쿼리를 실행하지 않으면 연결을 끊기도 한다. 커넥션 풀에 특정 커넥션이 5분 넘게 유휴 상태로 존재했다고 하자. 이 경우 DBMS는 해당 커넥션의 연결을 끊지만 커넥션은 여전히 풀 속에 남아 있다. 이 **연결이 끊어진 커넥션을 가져와 재사용을 하면 익셉션이 발생한다.**

이런 문제를 방지하려면 **커넥션 풀의 커넥션이 유효한지 주기적으로 검사해야 한다.** 이와 관련된 속성이 **minEvictableIdleTimeMillis, timeBetweenEvictionRunsMillis, testWhileIdle 이다.**

* **예시**

  ```java
  @Bean(destroyMethod = "close")
  public DataSource dataSource() {
    DataSource ds = new DataSource();
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setUrl("jdbc:postgresql://arjuna.db.elephantsql.com:5432/");
    ds.setUsername("kberhhnn");
    ds.setPassword("HYXtqTXqY_vYfqysat4KIyMeNTfFj7cJ");
    ds.setInitialSize(2);
    ds.setMaxActive(10);
    
    // 유휴 커넥션 검사
    ds.setTestWhileIdle(true);
    // 최소 유휴 시간 3분
    ds.setMinEvictableIdleTimeMillis(1000 * 60 * 3);
    // 10초 주기
    ds.setTimeBetweenEvictionRunsMillis(1000 * 10);
    
    return ds;
  }
  ```

<br>

# 4. JdbcTemplate을 이용한 쿼리 실행

## 4.1. JdbcTemplate 생성하기

* **/spring/MemberDao.java**

  ```java
  package spring;
  
  import org.springframework.jdbc.core.JdbcTemplate;
  
  import javax.sql.DataSource;
  
  public class MemberDao {
  
    private JdbcTemplate jdbcTemplate;
  
    // DataSource를 생성자에 전달해서 DataSource를 주입받도록 한다.
    public MemberDao(DataSource dataSource) {
      this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
  }
  ```

<br>

* **/config/AppCtx.java**

  ```java
  @Configuration
  public class AppCtx {
  
    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
      // DataSource 객체 생성
      DataSource ds = new DataSource();
      // JDBC 드라이버 클래스 지정.
      ds.setDriverClassName("org.postgresql.Driver");
      // DB 연결할 때 사용할 URL, 계정, 암호 설정
      ds.setUrl("jdbc:postgresql://arjuna.db.elephantsql.com:5432/");
      ds.setUsername("kberhhnn");
      ds.setPassword("HYXtqTXqY_vYfqysat4KIyMeNTfFj7cJ");
  
      ds.setInitialSize(2);
      ds.setMaxActive(10);
      return ds;
    }
    
    @Bean
    // MemberDao 빈 설정
    public MemberDao memberDao() {
      return new MemberDao(dataSource());
    }
  
  }
  ```

<br>

## 4.2. JdbcTemplate을 이용한 조회 쿼리 실행

JdbcTemplate 클래스는 SELECT 쿼리 실행을 위한 query() 메서드를 제공한다.

* **자주 사용되는 쿼리 메서드**
  * List\<T> query(String sql, RowMapper\<T> rowMapper)
  * List\<T> query(String sql, Object[ ] args, RowMapper\<T> rowMapper)
  * List\<T> query(String sql, RowMapper\<T> rowMapper, Object... args)

<br>

query() 메서드는 sql 파라미터로 전달받은 쿼리를 실행하고 RowMapper를 이용해서 ResultSet의 결과를 자바 객체로 변환한다.

RowMapper의 mapRow() 메서드는 SQL 실행 결과로 구한 ResultSet에서 한 행의 데이터를 읽어와 자바 객체로 변환하는 매퍼 기능을 구현한다.

<br>

* **/spring/MemberDao.java**

  ```java
  package spring;
  
  import org.springframework.jdbc.core.JdbcTemplate;
  
  import javax.sql.DataSource;
  import java.util.Collection;
  import java.util.List;
  
  public class MemberDao {
  
    private JdbcTemplate jdbcTemplate;
  
    public MemberDao(DataSource dataSource) {
      this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
  
    public Member selectByEmail(String email) {
      List<Member> results = jdbcTemplate.query(
          // JdbcTemplate의 query() 메서드를 이용해서 쿼리 실행
          "select * from MEMBER where EMAIL = ?",
          // 람다식을 이용해서 RowMapper의 객체를 전달한다.
          (rs, rowNum) -> new Member(rs.getString("EMAIL"),
              rs.getString("PASSWORD"),
              rs.getString("NAME"),
              rs.getTimestamp("REGDATE").toLocalDateTime())
          // 인덱스 파라미터(물음표)를 email 로 결정한다.
      , email);
      // results가 비어 있는 경우와 그렇지 않은 경우를 구분해서 리턴 값을 처리
      return results.isEmpty() ? null : results.get(0);
    }
  
    ...
  
  }
  ```

  

<br>

동일한 RowMapper 구현을 여러 곳에서 사용한다면 RowMapper 인터페이스를 구현한 클래스를 만들어서 코드 중복을 막을 수 있다.

```java
// RowMapper를 구현한 클래스를 작성
public class MemberRowMapper implements RowMapper<Member> {
  @Override
  public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
    Member member = new Member(
      rs.getString("EMAIL"),
      rs.getString("PASSWORD"),
      rs.getString("NAME"),
      rs.getTimestamp("REGDATE").toLocalDateTime());
    member.setId(rs.getLong("ID"));
    return member;
  }
}

// MemberRowMapper 객체 생성
List<Member> results = jdbcTemplate.query(
  "select * from MEMBER where EMAIL = ? and NAME = ?",
  new MemberRowMapper(),
  email, name);
```

<br>

MemberDao의 selectAll() 메서드를 구현해보자.

* **/spring/MemberDao.java**

  ```java
  public List<Member> selectAll() {
      return jdbcTemplate.query("select * from MEMBER",
          (rs, rowNum) -> {
        Member member = new Member(
            rs.getString("EMAIL"),
            rs.getString("PASSWORD"),
            rs.getString("NAME"),
            rs.getTimestamp("REGDATE").toLocalDateTime());
        member.setId(rs.getLong("ID"));
        return member; });
    }
  ```

위 코드는 selectByEmail() 메서드와 동일한 RowMapper 임의 클래스를 사용했다. 

다음과 같이 Member를 위한 RowMapper 구현 클래스를 이용해서 두 메서드를 수정하면 RowMapper 임의 클래스나 람다식 중복을 제거할 수 있다.

```java
public Member selectByEmail(String email) {
  List<Member> results = jdbcTemplate.query(
    "select * from MEMBER where EMAIL = ?",
    new MemberRowMapper()
    , email);
  return results.isEmpty() ? null : results.get(0);
}

public List<Member> selectAll() {
  return jdbcTemplate.query("select * from MEMBER",
                            new MemberRowMapper());
}
```

<br>

## 4.3. 결과가 1행인 경우 사용할 수 있는 queryForObject() 메서드

MEMBER 테이블의 전체 행 개수를 구하는 코드를 작성해보자.

* **/spring/MemberDao.java**

  ```java
  public int count() {
    return jdbcTemplate.queryForObject(
      "select count(*) from MEMBER", Integer.class);
  }
  ```

  * queryForObject() 메서드는 쿼리 실행 결과 행이 한 개인 경우에 사용할 수 있는 메서드이다.

  * 두 번째 파라미터는 칼럼을 읽어올 때 사용할 타입을 지정한다.

  * **인덱스 파라미터를 이용한 예시**

    ```java
    double avg = queryForObject(
      "select avg(height) from FURNITURE where TYPE=? and STATUS=?",
      Double.class,
      100, "S");
    ```

<br> 특정 ID를 갖는 회원 데이터를 queryForObject()로 읽어오고 싶을 때

```java
Member memebr = jdbcTemplate.queryForObject(
  "select * from MEMBER where ID = ?",
  (rs, rowNum) -> {
    Member member = new Member(rs.getString("EMAIL"),
                              rs.getString("PASSWORD"),
                              rs.getString("NAME"),
                              rs.getTimestamp("REGDATE").toLocalDateTime());
    memget.setId(rs.getLong("ID"));
    return member;
  }, 100);
```

<br>

* **주요 queryForObject() 메서드**
  * T queryForObject(String sql, Class\<T> requiredType)
  * T queryForObject(String sql, Class\<T> requiredType, Object... args)
  * T queryForObject(String sql, RowMapper\<T> rowMapper)
  * T queryForObject(String sql, RowMapper\<T> rowMapper, Object... args)

> 주의할 점은 결과 행이 정확히 한 개가 아니면 익셉션이 발생하기 때문에 결과 행이 한 개가 아니면 query() 메서드를 사용해야 한다.

<br>

## 4.4. JdbcTemplate을 이용한 변경 쿼리 실행

INSERT, UPDATE, DELETE 쿼리는 update() 메서드를 사용한다.

* **int update(String sql)**
* **int update(String sql, Object... args)**

<br>

* **/spring/MemberDao.java**

  ```java
  public class MemberDao {
  
    private JdbcTemplate jdbcTemplate;
  
    public MemberDao(DataSource dataSource) {
      this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    ...
      
    public void update(Member member) {
      jdbcTemplate.update(
          "update MEMBER set NAME = ?, PASSWORD = ?, where EMAIL = ?",
          member.getName(), member.getPassword(), member.getEmail());
    }
    
  }
  ```

<br>

## 4.5. PreparedStatementCreator를 이용한 쿼리 실행

PreparedStatment의 set 메서드를 사용해서 **직접 인덱스 파라미터의 값을 설정해야 할 때도 있다.**

이 경우 PreparedStatementCreator를 인자로 받는 메서드를 이용해서 **직접 PreparedStatement를 생성하고 설정해야 한다.**

* **insert 예시**

  ```java
  jdbcTemplate.update(new PreparedStatementCreator() {
    @Override
    public PreparedStatement createPreparedStatement(Connection con) 
      throws SQLException {
      // 파라미터로 전달받은 Connection을 이용해서 PreparedStatement 생성
      PreparedStatement pstmt = con.prepareStatement(
        "insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) values (?,?,?,?)
      );
      pstmt.setString(1, member.getEmail());
      pstmt.setString(2, member.getPassword());
      pstmt.setString(3, member.getName());
      pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
      // 생성한 PreparedStatement 객체 리턴
      return pstmt;
    }
  })
  ```

<br>

* **PreparedStatementCreator 인터페이스를 파라미터로 갖는 메서드**
  * List\<T> query(PreparedStatementCreator psc, RowMapper\<T> rowMapper)
  * int update(PreparedStatementCreator psc)
  * int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder)

<br>

## 4.6. INSERT 쿼리 실행 시 KeyHolder를 이용해서 자동 생성 키값 구하기

AUTO_INCREMENT와 같은 자동 증가 칼럼을 가진 테이블에 값을 삽입하면 해당 칼럼의 값이 자동으로 생성된다.

그런데 쿼리 실행 후에 생성된 키 값을 알고 싶다면 **KeyHolder를** 사용해야 한다.

* **/spring/MemberDao.java**

  ```java
  package spring;
  
  ...
  
  public class MemberDao {
  
    private JdbcTemplate jdbcTemplate;
  
    public MemberDao(DataSource dataSource) {
      this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    ...
  
    public void insert(Member member) {
      // GeneratedKeyHolder : 자동 생성된 키값을 구해주는 KeyHolder 구현 클래스이다.
      KeyHolder keyHolder = new GeneratedKeyHolder();
      // PreparedStatement 객체와 KeyHolder 객체를 파라미터로 갖는다.
      jdbcTemplate.update(conn -> {
        // 람다식을 통해서 PreparedStatement 객체를 구현해서 넘기고,
        //  prepareStatement() 메서드의 두 번째 파라미터로 String 배열을 넘기는데
        //  이 두 번째 파라미터는 자동 생성되는 키 칼럼 목록을 지정할 때 사용한다.
        PreparedStatement pstmt = conn.prepareStatement(
            "insert into MEMBER (EMAIL, PASSWORD, NAME, REGDATE) " +
                "values (?, ?, ?, ?)",
            new String[] {"ID"});
        pstmt.setString(1, member.getEmail());
        pstmt.setString(2, member.getPassword());
        pstmt.setString(3, member.getName());
        pstmt.setTimestamp(4, Timestamp.valueOf(member.getRegisterDateTime()));
        return pstmt;
      }, keyHolder);
      // KeyHolder에 보관된 키값을 getKey() 메서드를 이용해서 구한다.
      Number keyValue = keyHolder.getKey();
      member.setId(keyValue.longValue());
    }
  
  }
  ```

<br>

# 5. MemberDao 테스트하기

간단한 메인 클래스를 작성해서 MemberDao가 정상적으로 동작하는지 확인해보자.

* **/main/MainForMemberDao.java**

  ```java
  package main;
  
  import config.AppCtx;
  import org.springframework.context.annotation.AnnotationConfigApplicationContext;
  import spring.Member;
  import spring.MemberDao;
  
  import java.time.LocalDateTime;
  import java.time.format.DateTimeFormatter;
  import java.util.List;
  
  public class MainForMemberDao {
  
    private static MemberDao memberDao;
  
    public static void main(String[] args) {
      AnnotationConfigApplicationContext ctx =
          new AnnotationConfigApplicationContext(AppCtx.class);
  
      memberDao = ctx.getBean(MemberDao.class);
  
      selectAll();
      updateMember();
      insertMember();
  
      ctx.close();
    }
  
    private static void selectAll() {
      System.out.println("----- selectAll");
      int total = memberDao.count();
      System.out.println("전체 데이터: " + total);
      List<Member> members = memberDao.selectAll();
      members.forEach(m ->
          System.out.println(m.getId() + ":" + m.getEmail() + ":" + m.getName()));
    }
  
    private static void updateMember() {
      System.out.println("----- updateMember");
      Member member = memberDao.selectByEmail("nalsm98@test.com");
      String oldPw = member.getPassword();
      String newPw = Double.toHexString(Math.random());
      member.changePassword(oldPw, newPw);
  
      memberDao.update(member);
      System.out.println("암호 변경: " + oldPw + " > " + newPw);
    }
  
    private static DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("MMddHHmmss");
  
    private static void insertMember() {
      System.out.println("----- insertMember");
  
      String prefix = formatter.format(LocalDateTime.now());
      Member member = new Member(prefix + "@test.com",
          prefix, prefix, LocalDateTime.now());
      memberDao.insert(member);
      System.out.println(member.getName() + " 데이터 추가");
    }
  
  }
  ```

* **실행 결과**

  ```
  ----- selectAll
  전체 데이터: 1
  1:nalsm98@test.com:min
  ----- updateMember
  암호 변경: 0x1.0563e72c47816p-1 > 0x1.55f52cd14d4a6p-2
  ----- insertMember
  0301215005 데이터 추가
  ```

<br>

## 5.1. DB 연동 과정에서 발생 가능한 익셉션

* **DB 연결 정보가 올바르지 않으면 다음과 같은 익셉션이 발생할 수 있다.**

```
...
Exception in thread "main" org.springframework.jdbc.CannotGetJdbcConnectionException: Failed to obtain JDBC Connection: ...
Access denied for user 'kberhhnn'@'arjuna.db.elephantsql.com:5432'
```

DB 연결 정보는 DataSource에 있으므로 DataSource를 잘못 설정하면 연결을 구할 수 없다는 익셉션(CannotGetJdbcConnectionException)이 발생한다.

<br>

* **DB를 실행하지 않았거나 방화벽에 막혀 있어서 DB에 연결할 수 없다면 연결 자체를 할 수 없다는 에러 메시지가 출력된다. **

```
... CannotGetJdbcConnectionException: ...
Communications link failure ...
```

<br>

* **잘못된 쿼리를 사용하는 것도 주요 에러 원인이다.**

  * 에러 예시

    ```java
    jdbcTemplate.update("update MEMBER set NAME = ?, PASSWORD = ? where" + "EMAIL = ?", member.getName(), member.getPassword(), member.getEmail()):
    ```

  * 쿼리

    ```
    update MEMBER set NAME = ?, PASSWORD = ? whereEMAIL = ?
    ```

    > where 문과 EMAIL이 붙으므로 잘못된 쿼리문이다.

  * 익셉션

    ```
    ... BadSqlGarmmarException: ... : bad SQL grammer ...
    MySQLSyntaxErrorException: ...
    ```

<br>

# 6. 스프링의 익셉션 변환 처리

SQL 문법이 잘못됐을 때 발생한 메시지를 보면 익셉션 클래스가 org.spring.framework.jdbc 패키지에 속한 **BadSqlGrammarException 클래스임을** 알 수 있다. 에러 메시지를 보면 BadSqlGrammarException이 발생한 이유는 MySQLSyntaxErrorException이 발생했기 때문이다.

```
org.springframework.jdbc.BadSqlGrammarException: ... 생략
... 생략
Caused by: com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: ... 생략
```

<br>

JDBC API 를 사용하는 과정에서 SQLException이 발생하면 이 익셉션을 알맞은 **DataAccessException으로 변환해서 발생한다.** 즉 다음과 유사한 방식으로 익셉션을 변환해서 재발생한다.

```java
try {
  ... // JDBC 사용 코드
} catch(SQLException ex) {
  throw convertSqlToDataException(ex);
}
```

이를 통해 JdbcTemplate은 이 익셉션을 DataAccessException을 상속받은 **BadSqlGrammarException으로 변환한다.**

<br>

스프링은 왜 SQLException을 그대로 전파하지 않고 SQLException을 DataAccessException으로 변환할까?

주된 이유는 **연동 기술에 상관없이 동일하게 익셉션을 처리할 수 있도록 하기 위함이다.**

![image](https://user-images.githubusercontent.com/43431081/75668064-0e323580-5cbc-11ea-9351-1d1c122b15aa.png)

<br>

스프링은 이 외에도 DuplicateKeyException, QueryTimeoutException 등 DataAccessException을 상속한 다양한 익셉션 클래스를 제공한다. 각 익셉션 클래스의 이름은 문제가 발생한 원인을 의미한다.

DataAccessException은 RuntimeException이다. DataAccessException은 RuntimeException이므로 필요한 경우에만 익셉션을 처리하면 된다.

```java
// JDBC를 직접 사용하면 SQLException을 반드시 알맞게 처리해주어야 함
try {
  pstmt = conn.prepareStatement(someQuery);
  ...
} catch(SQLException ex) {
  ... // SQLException을 알맞게 처리해 주어야 함
}

// 스프링을 사용하면 DataAccessException을 필요한 경우에만
//	try-catch 로 처리해주면 된다.
jdbcTemplate.update(someQuery, param1);
```

<br>

# 7. 트랜잭션 처리

두 개 이상의 쿼리를 한 작업으로 실행해야 할 때 사용하는 것이 **트랜잭션(transaction)이다.** 

트랜잭션은 여러 쿼리를 논리적으로 하나의 작업으로 묶어준다. 

쿼리 실행 결과를 취소하고 DB를 기존 상태로 되돌리는 것을 **롤백(rollback)이라고** 부른다. 반면에 트랜잭션으로 묶인 모든 쿼리가 성공해서 쿼리 결과를 DB에 실제로 반영하는 것을 **커밋(commit)이라고** 한다.

트랜잭션을 시작하면 트랜잭션을 커밋하거나 롤백할 때 까지 실행한 쿼리들이 하나의 작업 단위가 된다.

JDBC는 Connection의 **setAutoCommit(false)를 이용해서 트랜잭션을 시작하고 commit()과 rollback()을** 이용해서 트랜잭션을 반영(커밋)하거나 취소(롤백) 한다.

<br>

## 7.1. @Transactional을 이용한 트랜잭션 처리

스프링이 제공하는 **@Transactional 애노테이션을** 사용하면 트랜잭션 범위를 매우 쉽게 지정할 수 있다.

트랜잭션 범위에서 실행하고 싶은 **메서드에 @Transactional 애노테이션만** 붙이면 된다.

```java
@Transactional
public void changePassword(String email, String oldPwd, String newPwd) {
  Member member = memberDao.selectByEmail(email);
  if (member == null)
    throw new MemberNotFoundException();

  member.changePassword(oldPwd, newPwd);

  memberDao.update(member);
}
```

* memberDao.selectByEmail() 쿼리와 member.changePassword() 쿼리가 한 트랜잭션에 묶인다.

<br>

@Transactional 애노테이션이 제대로 동작하려면 다음의 두 가지 내용을 스프링 설정에 추가해야 한다.

* **플랫폼 트랜잭션 매니저(PlatformTransactionManager) 빈 설정**
* **@Transactional 애노테이션 활성화 설정**

<br>

**설정 예시**

```java
@Configuration
@EnableTransactionManagement
public class AppCtx {

  @Bean(destroyMethod = "close")
  public DataSource dataSource() {
    DataSource ds = new DataSource();
    ... 생략
    return ds;
  }
  
  ... 생략
  
  @Bean
  public PlatformTransactionManager transactionManager() {
    DataSourceTransactionManager tm = new DataSourceTransactionManager();
    tm.setDataSource(dataSource());
    return tm;
  }

}
```

* **PlatformTransactionManager** : 스프링이 제공하는 트랜잭션 매니저 인터페이스
  * 구현기술에 상관없이 동일한 방식으로 트랜잭션을 처리하기 위해 이 인터페이스를 사용
  * dataSource 프로퍼티를 이용해서 트랜잭션 연동에 사용할 DataSource를 지정한다.
* **@EnableTransactionManagement** : @Transactional 애노테이션이 붙은 메서드를 트랜잭션 범위에서 실행하는 기능을 활성화한다.
  * 등록된 PlatformTransactionManager 빈을 사용해서 트랜잭션을 적용한다.

<br>

* **/spring/ChangePasswordService.java**

  ```java
  public class ChangePasswordService {
  
    private MemberDao memberDao;
  
    @Transactional
    public void changePassword(String email, String oldPwd, String newPwd) {
      Member member = memberDao.selectByEmail(email);
      if (member == null)
        throw new MemberNotFoundException();
  
      member.changePassword(oldPwd, newPwd);
  
      memberDao.update(member);
    }
  
    public void setMemberDao(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
  
  }
  ```

* **/config/AppCtx.java**

  ```java
  @Configuration
  @EnableTransactionManagement
  public class AppCtx {
  
    @Bean(destroyMethod = "close")
    public DataSource dataSource() {
      DataSource ds = new DataSource();
      ... 생략
      return ds;
    }
  
    @Bean
    public MemberDao memberDao() {
      return new MemberDao(dataSource());
    }
  
    @Bean
    public PlatformTransactionManager transactionManager() {
      DataSourceTransactionManager tm = new DataSourceTransactionManager();
      tm.setDataSource(dataSource());
      return tm;
    }
    
    @Bean
    public ChangePasswordService changePwdSvc() {
      ChangePasswordService pwdSvc = new ChangePasswordService();
      pwdSvc.setMemberDao(memberDao());
      return pwdSvc;
    }
  
  }
  ```

* **/spring/MainForCPS.java**

  ```java
  public class MainForCPS {
  
    public static void main(String[] args) {
      AnnotationConfigApplicationContext ctx =
          new AnnotationConfigApplicationContext(AppCtx.class);
  
      ChangePasswordService cps =
          ctx.getBean("changePwdSvc", ChangePasswordService.class);
  
      try {
        cps.changePassword("nalsm98@test.com", "1234", "1111");
      } catch (MemberNotFoundException e) {
        System.out.println("회원 데이터가 존재하지 않습니다.");
      } catch (WrongPasswordException e) {
        System.out.println("암호가 올바르지 않습니다.");
      }
  
      ctx.close();
    }
  
  }
  ```

트랜잭션을 확인하기 위해서 스프링이 출력하는 로그 메시지를 보자. 트랜잭션과 관련 **로그 메시지를 추가로 출력하기 위해 Logback를** 사용해보자.

<br>

* **build.gradle** : dependency 추가

  ```
  plugins {
      id 'java'
  }
  
  group 'org.example'
  version '1.0-SNAPSHOT'
  
  sourceCompatibility = 1.8
  
  repositories {
      mavenCentral()
  }
  
  dependencies {
      testCompile group: 'junit', name: 'junit', version: '4.12'
      compile("org.slf4j:slf4j-api:1.7.7")
      compile('ch.qos.logback:logback-classic:1.1.2')
      implementation 'org.springframework:spring-context:5.2.4.RELEASE'
      implementation 'org.springframework:spring-jdbc:5.2.4.RELEASE'
      implementation 'org.apache.tomcat:tomcat-jdbc:10.0.0-M1'
      implementation 'org.lucee:postgresql:8.3-606.jdbc4'
  }
  ```

* **실행 결과**

  ```
  2020-03-02 21:25:51,657 DEBUG o.s.j.d.DataSourceTransactionManager - Creating new transaction with name [spring.ChangePasswordService.changePassword]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
  2020-03-02 21:25:52,062 DEBUG o.s.j.d.DataSourceTransactionManager - Acquired Connection [ProxyConnection[PooledConnection[org.postgresql.jdbc4.Jdbc4Connection@1a760689]]] for JDBC transaction
  2020-03-02 21:25:52,064 DEBUG o.s.j.d.DataSourceTransactionManager - Switching JDBC Connection [ProxyConnection[PooledConnection[org.postgresql.jdbc4.Jdbc4Connection@1a760689]]] to manual commit
  2020-03-02 21:25:52,072 DEBUG o.s.j.c.JdbcTemplate - Executing prepared SQL query
  2020-03-02 21:25:52,072 DEBUG o.s.j.c.JdbcTemplate - Executing prepared SQL statement [select * from MEMBER where EMAIL = ?]
  2020-03-02 21:25:52,139 DEBUG o.s.j.c.JdbcTemplate - Executing prepared SQL update
  2020-03-02 21:25:52,139 DEBUG o.s.j.c.JdbcTemplate - Executing prepared SQL statement [update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?]
  2020-03-02 21:25:52,179 DEBUG o.s.j.d.DataSourceTransactionManager - Initiating transaction commit
  2020-03-02 21:25:52,179 DEBUG o.s.j.d.DataSourceTransactionManager - Committing JDBC transaction on Connection [ProxyConnection[PooledConnection[org.postgresql.jdbc4.Jdbc4Connection@1a760689]]]
  2020-03-02 21:25:52,219 DEBUG o.s.j.d.DataSourceTransactionManager - Releasing JDBC Connection [ProxyConnection[PooledConnection[org.postgresql.jdbc4.Jdbc4Connection@1a760689]]] after transaction
  ```

  * DataSourceTransactionManager - Switching JDBC Connection

    : 트랜잭션 시작

  * DataSourceTransactionManager - Initiating transaction commit

    : 트랜잭션 커밋

  * DataSourceTransactionManager - Committing JDBC transaction on Connection

    : 트랜잭션 커밋 중

<br>

어떤 것이 트랜잭션을 시작하고, 커밋하고, 롤백하는지를 살펴보도록 하자.

<br>

## 7.2. @Transactional과 프록시

스프링은 @Transactional 애노테이션을 이용해서 트랜잭션을 처리하기 위해 **내부적으로 AOP를 사용한다.**

실제로 @Transactional 애노테이션을 적용하기 위해 **@EnableTransactionManagement 태그를 사용하면 스프링은 @Transactional 애노테이션이 적용된 빈 객체를 찾아서 알맞은 프록시 객체를 생성한다.**

![image](https://user-images.githubusercontent.com/43431081/75677707-6e7ea280-5ccf-11ea-8cba-e58cbbf74d7d.png)

* ChangePasswordService 클래스의 메서드에 @Transactional 애노테이션이 적용되어 있으므로 스프링은 트랜잭션 기능을 적용한 프록시 객체를 생성한다.
  * MainForCPS 클래스에서 getBean() 을 실행해서 changePwdSvc 객체를 가져오면 ChangePasswordService 객체 대신에 **트랜잭션 처리를 위해 생성한 프록시 객체를 리턴한다.**
* 이 프록시 객체를 @Transactional 애노테이션이 붙은 메서드를 호출하면 **PlatformTransactionManager를** 사용해서 트랜잭션을 시작한다.

<br>

## 7.3. @Transactional 적용 메서드의 롤백 처리

롤백을 처리하는 주체는 프록시 객체이다.

* **예시**

  ```java
  try {
    cps.changePassword("nalsm98@test.com", "1234", "1111");
  } catch (MemberNotFoundException e) {
    System.out.println("회원 데이터가 존재하지 않습니다.");
  } catch (WrongPasswordException e) {
    System.out.println("암호가 올바르지 않습니다.");
  }
  ```

  * **WrongIdPasswordException이** 발생했을 때 트랜잭션이 롤백된 것을 알 수 있다.

  * 실제로 @Transactional을 처리하기 위한 프록시 객체는 원본 객체의 메서드를 실행하는 과정에서 **RuntimeException이 발생하면 아래와 같이 트랜잭션을 롤백한다.**

    ![image](https://user-images.githubusercontent.com/43431081/75678214-79860280-5cd0-11ea-8f1a-b7fe2d24afda.png)

<br>

별로 설정을 추가하지 않으면 발생한 익셉션이 **RuntimeException일 때 트랜잭션을 롤 백한다.**

JdbcTemplate은 DB 연동 과정에 문제가 있으면 익셉션이 발생해 프록시는 트랜잭션을 롤백한다.

SQLException은 RuntimeException을 상속하고 있지 않으므로 **SQLException이 발생하면 트랜잭션을 롤백하지 않는다.** SQLException이 발생하는 경우에도 트랜잭션을 롤백하고 싶다면 **@Transactional의 rollbackFor 속성을 사용해야 한다.**

```java
@Transactional(rollbackFor = SQLException.class)
public void someMethod() {
  ...
}
```

<br>

rollbackFor와 반대 설정을 제공하는 것이 **noRollbackFor 속성이다.** 이 속성은 지정한 익셉션이 발생해도 **롤백시키지 않고 커밋할 익셉션 타입을 지정할 때 사용한다.**

<br>

## 7.4. @Transactional의 주요 속성

| 속성        | 타입        | 설명                                                         |
| ----------- | ----------- | ------------------------------------------------------------ |
| value       | String      | 트랜잭션을 관리할 때 사용할 PlatformTransactionManager<br />빈의 이름을 지정한다. 기본값은 " " 이다. |
| propagation | Propagation | 트랜잭션 전파 타입을 지정한다.<br />기본값은 Propagation.REQUIRED 이다. |
| isolation   | Isolation   | 트랜잭션 격리 레벨을 지정한다.<br />기본값은 Isolation.DEFAULT 이다. |
| timeout     | int         | 트랜잭션 제한 시간을 지정한다. 기본값은 -1로 이 경우<br />데이터베이스의 타임아웃 시간을 사용한다. 초 단위로 지정한다. |

<br>

@Transactional 애노테이션의 value 속성값이 없으면 등록된 빈 중에서 타입이 PlatformTransactionManager인 빈을 사용한다.

```java
// AppCtx 설정 클래스의 플랫폼 트랜잭션 매니저 빈 설정
@Bean
public PlatformTransactionManager transactionManager() {
  DataSourceTransactionManager tm = new DataSourceTransactionManager();
  tm.setDataSource(dataSource());
  return tm;
}
```

<br>

* **Propagation 열거 타입의 주요 값**

  | 값            | 설명                                                         |
  | ------------- | ------------------------------------------------------------ |
  | REQUIRED      | 메서드를 수행하는 데 트랜잭션이 필요하다는 것을 의미한다.    |
  | MANDATORY     | 메서드를 수행하는 데 트랜잭션이 필요하다는 것을 의미한다.<br />하지만 REQUIRED와 달리 진행 중인 트랜잭션이 존재하지 않을 경우 익셉션 발생 |
  | REQUIRES_NEW  | 항상 새로운 트랜잭션을 시작한다.                             |
  | SUPPORTS      | 메서드가 트랜잭션을 필요로 하지는 않지만,<br />진행 중인 트랜잭션이 존재하면 트랜잭션을 사용한다는 것을 의미한다. |
  | NOT_SUPPORTED | 메서드가 트랜잭션을 필요로 하지 않음을 의미한다.<br />진행 중인 트랜잭션이 존재할 경우 메서드가 실행되는 동안<br />트랜잭션은 일시 중지되고 메서드 실행이 종료된 후에 트랜잭션을 계속 진행한다. |
  | NEVER         | 메서드가 트랜잭션을 필요로 하지 않는다.<br />만약 진행 중인 트랜잭션이 존재하면 익셉션이 발생한다. |
  | NESTED        | 진행 중인 트랜잭션이 존재하면 기존 트랜잭션에 중첩된 트랜잭션에서<br />메서드를 실행한다. |

<br>

* **Isolation 열거 타입에 정의된 값**

  | 값               | 설명                                                         |
  | ---------------- | ------------------------------------------------------------ |
  | DEFAULT          | 기본 설정을 사용한다.                                        |
  | READ_UNCOMMITTED | 다른 트랜잭션이 커밋하지 않은 데이터를 읽을 수 있다.         |
  | READ_COMMITED    | 다른 트랜잭션이 커밋한 데이터를 읽을 수 있다.                |
  | REPEATABLE_READ  | 처음에 읽어 온 데이터와 두 번째 읽어 온 데이터가 동일한 값을 갖는다. |
  | SERIALIZABLE     | 동일한 데이터에 대해서 동시에 두 개 이상의 트랜잭션을<br />수행할 수 없다. |

> **트랜잭션의 격리 레벨(Isolation)은** 동시에 DB에 접근할 때 그 접근을 어떻게 제어할지에 대한 설정을 다룬다.

<br>

## 7.5. @EnableTransactionManagement 애노테이션의 주요 속성

| 속성             | 설명                                                         |
| ---------------- | ------------------------------------------------------------ |
| proxyTargetClass | 클래스를 이용해서 프록시를 생성할지 여부를 지정한다.<br />기본값은 false로서 인터페이스를 이용해서 프록시를 생성한다. |
| order            | AOP 적용 순서를 지정한다.<br />기본값은 가장 낮은 우선순위에 해당하는 int의 최댓값이다. |

<br>

## 7.6. 트랜잭션 전파

* **예시**

  ```java
  public class ChangePasswordService {
  
    private MemberDao memberDao;
  
    @Transactional
    public void changePassword(String email, String oldPwd, String newPwd) {
      Member member = memberDao.selectByEmail(email);
      if (member == null)
        throw new MemberNotFoundException();
  
      member.changePassword(oldPwd, newPwd);
  
      memberDao.update(member);
    }
  
    public void setMemberDao(MemberDao memberDao) {
      this.memberDao = memberDao;
    }
  
  }
  
  public class MemberDao {
  
    private JdbcTemplate jdbcTemplate;
  
    ... 생략
  
    public void update(Member member) {
      jdbcTemplate.update(
          "update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
          member.getName(), member.getPassword(), member.getEmail());
    }
  
  }
  ```

  * MemberDao의 update() 메서드에 @Transactional 이 붙어 있지 않지만 JdbcTemplate 클래스에 트랜잭션 범위에서 쿼리를 실행할 수 있다.

* **실행 흐름**

  ![image](https://user-images.githubusercontent.com/43431081/75680989-041d3080-5cd6-11ea-995f-912ead1a867a.png)

