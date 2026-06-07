package com.caelum.chronos.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = BackendApplication.class)
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = OAuth2ClientAutoConfiguration.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
 }
}
