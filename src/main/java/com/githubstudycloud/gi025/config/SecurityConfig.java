package com.githubstudycloud.gi025.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
			@Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled)
			throws Exception {
		http.csrf(AbstractHttpConfigurer::disable);
		http.httpBasic(AbstractHttpConfigurer::disable);
		http.formLogin(AbstractHttpConfigurer::disable);
		http.logout(AbstractHttpConfigurer::disable);
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.authorizeHttpRequests(authorize -> {
			authorize.requestMatchers("/actuator/health/**", "/actuator/info", "/error").permitAll();
			if (h2ConsoleEnabled) {
				authorize.requestMatchers("/h2-console/**").permitAll();
			}
			authorize.anyRequest().authenticated();
		});
		http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
		http.addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager();
	}
}
