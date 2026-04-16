package com.githubstudycloud.gi025;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.githubstudycloud.gi025.config.ApiKeyAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "enterprise.security.api-keys=prod-test-key")
@ActiveProfiles("prod")
@AutoConfigureMockMvc
class ProductionProfileIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void prodProfileRejectsDefaultLocalApiKey() throws Exception {
		mockMvc.perform(get("/api/v1/customers")
				.header(ApiKeyAuthenticationFilter.API_KEY_HEADER, "dev-local-api-key"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void prodProfileUsesExplicitApiKeyAndDisablesH2Console() throws Exception {
		mockMvc.perform(get("/api/v1/customers")
				.header(ApiKeyAuthenticationFilter.API_KEY_HEADER, "prod-test-key"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").isArray());

		mockMvc.perform(get("/h2-console/")
				.header(ApiKeyAuthenticationFilter.API_KEY_HEADER, "prod-test-key"))
			.andExpect(status().isNotFound());
	}
}
