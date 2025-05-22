package com.wappenable.be;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootTest
@Disabled("통합 테스트 환경에서는 contextLoads는 필요 없음")
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
class BeApplicationTests {

	@Test
	void contextLoads() {
	}

}
