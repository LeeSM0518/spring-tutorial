package japbook.jpashop.domain.item;

import lombok.Getter;

import javax.persistence.Embeddable;

// 값 타입
// JPA 내장 타입이기 때문에 아래와 같은 에노테이션 사용
@Embeddable
@Getter
public class Address {

  private String city;
  private String street;
  private String zipcode;

  // JPA 스펙상 엔티티나 임베디드 타입(@Embeddable)은
  //  자바 기본 생성자를 public 또는 protected 로 설정해야 한다. (JPA가 리플랙션을 사용할 수 있도록)
  //  protected 로 설정하는 것이 안전.
  protected Address() {
  }

  // 값 타입은 변경 불가능하게 설계해야 한다.
  //  그래서 @Setter 를 제거하고 아래와 같이
  //  생성자만 구현되어 있다.
  public Address(String city, String street, String zipcode) {
    this.city = city;
    this.street = street;
    this.zipcode = zipcode;
  }

}
