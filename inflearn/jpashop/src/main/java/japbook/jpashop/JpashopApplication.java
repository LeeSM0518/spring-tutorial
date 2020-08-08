package japbook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 이 에노테이션으로 인해
//  현재 패키지와 하위 패키지에 있는 Bean 들을
//  모두 컨테이너에 스프링 빈으로 자동 등록
@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

}
