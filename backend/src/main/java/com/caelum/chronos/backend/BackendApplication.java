package com.caelum.chronos.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.caelum.chronos")
@EnableAsync
@EnableRetry
@EnableScheduling
@EnableJpaRepositories(basePackages = {
		"com.caelum.chronos.modules",
		"com.caelum.chronos.shared.infra"
})
@EntityScan(basePackages = {
		"com.caelum.chronos.modules",
		"com.caelum.chronos.shared.domain",
		"com.caelum.chronos.shared.infra"
})
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
