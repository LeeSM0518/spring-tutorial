import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main2 {

  public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx =
        new AnnotationConfigApplicationContext(AppContext.class);
    Greeter g1 = ctx.getBean("greeter", Greeter.class);
    Greeter g2 = ctx.getBean("greeter", Greeter.class);
    System.out.println("(g1 == g2) = " + (g1 == g2));

    Greeter g3 = ctx.getBean("greeter", Greeter.class);
    Greeter g4 = ctx.getBean("greeter", Greeter.class);
    System.out.println("(g3 == g4) = " + (g3 == g4));

    ctx.close();
  }

}
