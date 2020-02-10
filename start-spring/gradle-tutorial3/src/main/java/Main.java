// AnnotationConfigApplicationContext : 자바 설정에서 정보를 읽어와 빈 객체를 생성하고 관리한다.
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

  public static void main(String[] args) {
    // AnnotationConfigApplicationContext 객체를 생성할 때
    //  앞서 작성한 AppContext 클래스를 파라미터로 넘긴다.
    //  AppContext 에 정의한 @Bean 설정 정보를 읽어와 Greeter 객체를 생성하고 초기화한다.
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(AppContext.class);
    // AnnotationConfigApplicationContext 가 자바 설정을 읽어와 생성한 빈 객체를 검색
    //  첫 번째 파라미터 : 빈 객체 이름
    //  두 번째 파라미터 : 빈 객체의 타입
    //    Greeter 객체를 가져온다.
    Greeter g = ctx.getBean("greeter", Greeter.class);
    String msg = g.greet("스프링");
    System.out.println(msg);
    ctx.close();
  }

}
