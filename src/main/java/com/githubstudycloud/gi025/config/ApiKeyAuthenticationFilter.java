package com.githubstudycloud.gi025.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

	public static final String API_KEY_HEADER = "X-API-Key";

	private final EnterpriseProperties properties;

	public ApiKeyAuthenticationFilter(EnterpriseProperties properties) {
		this.properties = properties;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return "OPTIONS".equalsIgnoreCase(request.getMethod())
			|| path.startsWith("/actuator/health")
			|| path.equals("/actuator/info")
			|| path.equals("/error")
			|| path.startsWith("/h2-console");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String apiKey = request.getHeader(API_KEY_HEADER);
		if (apiKey == null || apiKey.isBlank()) {
			unauthorized(response, "Missing " + API_KEY_HEADER + " header");
			return;
		}

		if (!properties.getSecurity().getApiKey().equals(apiKey)) {
			unauthorized(response, "Invalid API key");
			return;
		}

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			"enterprise-api-client",
			"n/a",
			List.of(new SimpleGrantedAuthority("ROLE_API")));
		authentication.setDetails(request.getHeader(HttpHeaders.USER_AGENT));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}

	private void unauthorized(HttpServletResponse response, String message) throws IOException {
		SecurityContextHolder.clearContext();
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.getWriter().write("""
			{"code":"UNAUTHORIZED","message":"%s"}
			""".formatted(escapeJson(message)));
	}

	private String escapeJson(String input) {
		return input.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
