package com.githubstudycloud.gi025.config;

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

		private String apiKey = "change-me-enterprise-key";
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
}
