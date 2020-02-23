package config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import spring.*;

@Configuration
public class AppCtx {

  @Bean
  public Client client() {
    Client client = new Client();
    client.setHost("host");
    return client;
  }

//  @Bean(initMethod ="connect", destroyMethod = "close")
//  public Client2 client2() {
//    Client2 client2 = new Client2();
//    client2.setHost("host");
//    return client2;
//  }

  @Bean(destroyMethod = "close")
  public Client2 client2() {
    Client2 client2 = new Client2();
    client2.setHost("host");
    client2.connect();
    return client2;
  }

}
