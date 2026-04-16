package com.githubstudycloud.gi025.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

	@Bean
	CacheManager cacheManager() {
		CaffeineCacheManager manager = new CaffeineCacheManager("customer-list", "knowledge-search");
		manager.setCaffeine(Caffeine.newBuilder()
			.recordStats()
			.maximumSize(1_000)
			.expireAfterWrite(Duration.ofMinutes(10)));
		return manager;
	}
}
