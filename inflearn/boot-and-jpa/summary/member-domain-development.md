# 회원 도메인 개발

**구현 기능**

* 회원 등록
* 회원 목록 조회

<br>

**순서**

* 회원 리포지토리 개발
* 회원 서비스 개발
* 회원 기능 테스트

<br>

## 회원 리포지토리 개발

```java
package japbook.jpashop.repository;

import japbook.jpashop.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

// 스프링 빈으로 등록시키기 위한 어노테이션
@Repository
// @RequiredArgsContructor 를 통해 엔티티 매니저 생성자 및 자동 주입 구현 가능
public class MemberRepository {

  // JPA 엔티티 매니저를 자동 주입하기 위한 어노테이션
  //  @PersistenceContext
  private final EntityManager em;

  @Autowired // 스프링 부트만 가능
  public MemberRepository(EntityManager em) {
    this.em = em;
  }

  public void save(Member member) {
    em.persist(member); // 영속성 컨텍스트에 올린다.
  }

  public Member findOne(Long id) {
    return em.find(Member.class, id);
  }

  public List<Member> findAll() {
    // JPQL 을 작성
    //  파라미터: (JPQL, 반환타입)
    return em.createQuery("select m from Member m", Member.class)
        .getResultList();

    // JPQL : 객체를 대상으로 쿼리
  }

  public List<Member> findByName(String name) {
    return em.createQuery("select m from Member m where m.name = :name", Member.class)
        .setParameter("name", name)
        .getResultList();
  }

}

```

<br>

## 회원 서비스 개발

```java
package japbook.jpashop.service;

import japbook.jpashop.domain.Member;
import japbook.jpashop.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
// @RequiredArgsConstructor 를 통해 리포지토리 생성자 주입 구현 가능
public class MemberService {

  private final MemberRepository memberRepository;

  @Autowired
  public MemberService(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }

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

```

* 회원가입 할 때 검증 로직이 있어도 멀티 쓰레드 상황을 고려해서 회원 테이블의 회원명 컬럼에 **유니크 제약 조건을 추가하는 것이 안전하다.**
* 생성자 주입 방식을 권장
* 생성자가 하나면 `@Autowired` 를 생략하여도 스프링 부트가 자동으로 주입해준다. 
* Lombok에서 지원해주는 `@RequiredArgsConstructor` 를 사용하면 현재 `final` 로 되어있는 필드를 자동으로 생성자를 만들어준다.

<br>

## 회원 기능 테스트

* 회원가입을 성공해야 한다.
* 회원가입 할 때 같은 이름이 있으면 예외가 발생해야 한다.

 ```java
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
@Transactional                //  반복 가능한 테스트 지원, 각각의 테스트를 실행할 때마다 
                              //  트랜잭션을 시작하고 테스트가 끝나면 트랜잭션을 강제로 롤백
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
 ```

<br>

## 테스트 케이스를 위한 설정

테스트 케이스는 격리된 환경에서 실행하고, 끝나면 데이터를 초기화하는 것이 좋다. 그런 면에서 메모리 DB를 사용하는 것이 가장 이상적이다.

추가로 테스트 케이스를 위한 스프링 환경과, 일반적으로 애플리케이션을 실행하는 환경은 보통 다르므로 설정 파일을 다르게 사용하자.

* **test/resources/application.yml** 파일 생성

  ```yaml
  server:
    port: 8099
  
  # 스프링 부트는 알아서 DB 설정이 없으면 메모리 환경에서 작업을 한다.
  spring:
  #  datasource:
  #    driver-class-name: org.h2.Driver
  #    url: jdbc:h2:mem:test
  #    username: test
  #    password: 1234
  #
  #  jpa:
  #    hibernate:
  #      ddl-auto: create // create-drop: WAS가 종료될 때, 테이블을 모두 drop 드랍한다.
  #    properties:
  #      hibernate:
  #        format_sql: true
  
  logging:
    level:
      org.hibernate.SQL: debug
      org.hibernate.type: trace
  ```

  * 이제 테스트에서 스프링을 실행하면 이 위치에 있는 설정 파일을 읽는다.
  * `ddl-auto: create-drop` 모드로 동작한다. 따라서 데이터소스나, JPA 관련된 별도의 추가 설정을 하지 않아도 된다.