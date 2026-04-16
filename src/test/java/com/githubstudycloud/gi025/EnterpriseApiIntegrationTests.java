package com.githubstudycloud.gi025;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.githubstudycloud.gi025.config.EnterpriseProperties;
import com.githubstudycloud.gi025.config.ApiKeyAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EnterpriseApiIntegrationTests {

	private static final String API_KEY = EnterpriseProperties.Security.DEFAULT_LOCAL_API_KEY;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listCustomersRequiresApiKey() throws Exception {
		mockMvc.perform(get("/api/v1/customers"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
			.andExpect(jsonPath("$.message").value("Missing X-API-Key header"))
			.andExpect(jsonPath("$.path").value("/api/v1/customers"));
	}

	@Test
	void listCustomersReturnsSeededDataAndRequestId() throws Exception {
		mockMvc.perform(get("/api/v1/customers")
				.header(ApiKeyAuthenticationFilter.API_KEY_HEADER, API_KEY))
			.andExpect(status().isOk())
			.andExpect(header().exists("X-Request-Id"))
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].customerNo").exists())
			.andExpect(jsonPath("$.data[0].name").isNotEmpty());
	}

	@Test
	void quoteOrderReturnsExpectedEnterpriseDiscount() throws Exception {
		String payload = """
			{
			  "customerNo": "CUST-1001",
			  "items": [
			    { "sku": "CLOUD-OBS-01", "quantity": 5 },
			    { "sku": "AI-OPS-02", "quantity": 3 }
			  ]
			}
			""";

		mockMvc.perform(post("/api/v1/orders/quote")
				.header(ApiKeyAuthenticationFilter.API_KEY_HEADER, API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.customerNo").value("CUST-1001"))
			.andExpect(jsonPath("$.data.discountRate").value(0.12))
			.andExpect(jsonPath("$.data.quotedTotal").value(8088.96));
	}

	@Test
	void createOrderAssignsSequentialOrderNumbers() throws Exception {
		String payload = """
			{
			  "customerNo": "CUST-1001",
			  "items": [
			    { "sku": "CLOUD-OBS-01", "quantity": 1 }
			  ],
			  "notes": "Follow-up order"
			}
			""";

		mockMvc.perform(post("/api/v1/orders")
				.header(ApiKeyAuthenticationFilter.API_KEY_HEADER, API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.orderNo").value("SO-00002"));

		mockMvc.perform(post("/api/v1/orders")
				.header(ApiKeyAuthenticationFilter.API_KEY_HEADER, API_KEY)
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.orderNo").value("SO-00003"));
	}

	@Test
	void healthEndpointIsPublic() throws Exception {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
	}
}
