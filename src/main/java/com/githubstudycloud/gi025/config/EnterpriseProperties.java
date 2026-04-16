package com.githubstudycloud.gi025.config;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "enterprise")
public class EnterpriseProperties {

	private final Security security = new Security();

	private final Demo demo = new Demo();

	private final Ai ai = new Ai();

	@Getter
	@Setter
	public static class Security {

		public static final String DEFAULT_LOCAL_API_KEY = "dev-local-api-key";

		private List<String> apiKeys = new ArrayList<>(List.of(DEFAULT_LOCAL_API_KEY));

		private boolean allowDefaultApiKey = true;

		public List<String> configuredApiKeys() {
			return apiKeys == null ? List.of() : apiKeys.stream()
				.map(value -> value == null ? null : value.trim())
				.filter(value -> value != null && !value.isBlank())
				.distinct()
				.toList();
		}

		public boolean matches(String candidate) {
			if (candidate == null || candidate.isBlank()) {
				return false;
			}
			byte[] actual = candidate.trim().getBytes(StandardCharsets.UTF_8);
			return configuredApiKeys().stream()
				.map(value -> value.getBytes(StandardCharsets.UTF_8))
				.anyMatch(expected -> MessageDigest.isEqual(expected, actual));
		}

		void validate() {
			List<String> configuredKeys = configuredApiKeys();
			if (configuredKeys.isEmpty()) {
				throw new IllegalStateException("At least one enterprise.security.api-keys value must be configured");
			}
			if (!allowDefaultApiKey && configuredKeys.contains(DEFAULT_LOCAL_API_KEY)) {
				throw new IllegalStateException(
					"The default local API key is not allowed for this profile. Set ENTERPRISE_API_KEYS explicitly.");
			}
		}
	}

	@Getter
	@Setter
	public static class Demo {

		private boolean seedEnabled = true;
	}

	@Getter
	@Setter
	public static class Ai {

		private String systemPrompt = """
			You are an enterprise operations copilot.
			Prioritize accuracy, summarize clearly, and use available MCP tools when they improve the answer.
			When business data is insufficient, say what is missing instead of guessing.
			""";
	}

	@PostConstruct
	void validate() {
		security.validate();
	}
}
