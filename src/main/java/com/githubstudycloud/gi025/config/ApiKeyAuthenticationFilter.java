package com.githubstudycloud.gi025.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.githubstudycloud.gi025.common.web.RequestIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

	public static final String API_KEY_HEADER = "X-API-Key";

	private final EnterpriseProperties properties;

	private final ObjectMapper objectMapper = JsonMapper.builder().build();

	private final boolean h2ConsoleEnabled;

	public ApiKeyAuthenticationFilter(
			EnterpriseProperties properties,
			@Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled) {
		this.properties = properties;
		this.h2ConsoleEnabled = h2ConsoleEnabled;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return "OPTIONS".equalsIgnoreCase(request.getMethod())
			|| path.startsWith("/actuator/health")
			|| path.equals("/actuator/info")
			|| path.equals("/error")
			|| (h2ConsoleEnabled && path.startsWith("/h2-console"));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String apiKey = request.getHeader(API_KEY_HEADER);
		if (apiKey == null || apiKey.isBlank()) {
			unauthorized(request, response, "Missing " + API_KEY_HEADER + " header");
			return;
		}

		if (!properties.getSecurity().matches(apiKey)) {
			unauthorized(request, response, "Invalid API key");
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

	private void unauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
		SecurityContextHolder.clearContext();
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		String requestId = (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("success", false);
		body.put("timestamp", Instant.now().toString());
		body.put("requestId", requestId);
		body.put("code", "UNAUTHORIZED");
		body.put("message", message);
		body.put("path", request.getRequestURI());
		body.put("details", Map.of());
		objectMapper.writeValue(response.getWriter(), body);
	}
}
