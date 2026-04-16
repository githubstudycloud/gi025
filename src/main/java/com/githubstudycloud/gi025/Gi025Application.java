package com.githubstudycloud.gi025;

import com.githubstudycloud.gi025.config.EnterpriseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableConfigurationProperties(EnterpriseProperties.class)
public class Gi025Application {

	public static void main(String[] args) {
		SpringApplication.run(Gi025Application.class, args);
	}

}
