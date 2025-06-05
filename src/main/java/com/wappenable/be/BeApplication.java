package com.wappenable.be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = "com.wappenable")
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
// (exclude = { SecurityAutoConfiguration.class }) // 주석처리 해야 테스트 가능
public class BeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeApplication.class, args);
	}

}
