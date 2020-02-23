package main;

import config.AppCtx;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import spring.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
  
  public static void main(String[] args) throws IOException {
    AbstractApplicationContext ctx =
        new AnnotationConfigApplicationContext(AppCtx.class);

    Client client = ctx.getBean(Client.class);
    client.send();
    Client2 client2 = ctx.getBean(Client2.class);
    client2.send();

    ctx.close();
  }

}
