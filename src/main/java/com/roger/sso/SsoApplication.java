package com.roger.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// @EnableJpaRepositories("com.roger.sso.repository")
// @EntityScan("com.roger.sso.model")
public class SsoApplication {
	public static void main(String[] args) {
		SpringApplication.run(SsoApplication.class, args);
	}
}
