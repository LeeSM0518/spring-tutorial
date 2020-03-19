package controller;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import spring.RegisterRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterRequestValidator implements Validator {

  private static final String emailRegExp =
      "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
          "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
  private Pattern pattern;

  public RegisterRequestValidator() {
    pattern = Pattern.compile(emailRegExp);
  }

  @Override
  public boolean supports(Class<?> clazz) {
    // clazz 객체가 RegisterRequest 클래스로 타입 변환이 가능한지 확인한다.
    // 스프링 MVC가 자동으로 검증 기능을 수행하도록 설정하려면 올바르게 구현해야 한다.
    return RegisterRequest.class.isAssignableFrom(clazz);
  }

  @Override
  // target : 검사 대상 객체
  // errors : 검사 결과 에러 코드를 설정하기 위한 객체
  public void validate(Object target, Errors errors) {
    // 전달받은 target을 실제 타입으로 변환
    RegisterRequest regReq = (RegisterRequest) target;
    // 검사 대상 객체의 특정 프로퍼티나 상태가 올바른지 검사
    if (regReq.getEmail() == null || regReq.getEmail().trim().isEmpty()) {
      // 올바르지 않다면 Errors의 rejectValue() 메서드를 이용해서 에러 코드 저장
      errors.rejectValue("email", "required");
    } else {
      // 정규 표현식을 이용해서 이메일이 올바른지 확인
      Matcher matcher = pattern.matcher(regReq.getEmail());
      if (!matcher.matches()) {
        // 정규 표현식이 일치하지 rejectValue 를 통해 에러 코드 추가
        errors.rejectValue("email", "bad");
      }
    }
    // ValidationUtils : 객체의 값 검증 코드를 간결하게 작성할 수 있도록 도와준다.

    // 검사 대상 객체의 "name" 프로퍼티가 null 이거나 공백문자로만 되어 있는 경우
    // "name" 프로퍼티의 에러 코드로 "required"를 추가한다.
    // Errors 객체는 커맨드 객체의 특정 프로퍼티 값을 구할 수 있는
    //  getFieldValue() 메서드를 제공한다.
    ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "required");
    ValidationUtils.rejectIfEmpty(errors, "password", "required");
    ValidationUtils.rejectIfEmpty(errors, "confirmPassword", "required");
    if (!regReq.getPassword().isEmpty()) {
      if (!regReq.isPasswordEqualToConfirmPassword()) {
        errors.rejectValue("confirmPassword", "nomatch");
      }
    }
  }

}
