package icu.yeguo.cloudnest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("icu.yeguo.cloudnest.mapper")
public class CloudNestApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudNestApplication.class, args);
	}

}
