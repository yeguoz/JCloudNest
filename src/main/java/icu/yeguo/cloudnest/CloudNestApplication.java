package icu.yeguo.cloudnest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@ServletComponentScan
@MapperScan("icu.yeguo.cloudnest.mapper")
public class CloudNestApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudNestApplication.class, args);
	}

}
