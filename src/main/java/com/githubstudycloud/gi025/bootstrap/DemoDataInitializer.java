package com.githubstudycloud.gi025.bootstrap;

import com.githubstudycloud.gi025.catalog.Product;
import com.githubstudycloud.gi025.catalog.ProductRepository;
import com.githubstudycloud.gi025.config.EnterpriseProperties;
import com.githubstudycloud.gi025.customer.CustomerApiModels;
import com.githubstudycloud.gi025.customer.CustomerService;
import com.githubstudycloud.gi025.customer.CustomerTier;
import com.githubstudycloud.gi025.knowledge.KnowledgeArticle;
import com.githubstudycloud.gi025.knowledge.KnowledgeArticleRepository;
import com.githubstudycloud.gi025.sales.OrderApiModels;
import com.githubstudycloud.gi025.sales.SalesOrderService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoDataInitializer {

	@Bean
	ApplicationRunner seedDemoData(
			EnterpriseProperties properties,
			CustomerService customerService,
			ProductRepository productRepository,
			KnowledgeArticleRepository knowledgeArticleRepository,
			SalesOrderService salesOrderService) {
		return args -> {
			if (!properties.getDemo().isSeedEnabled() || customerService.count() > 0L) {
				return;
			}

			customerService.createCustomer(new CustomerApiModels.CreateCustomerRequest(
				"Acme Global Holdings",
				"procurement@acme.example",
				"United States",
				CustomerTier.ENTERPRISE));
			customerService.createCustomer(new CustomerApiModels.CreateCustomerRequest(
				"Blue Orbit Labs",
				"cto@blueorbit.example",
				"Singapore",
				CustomerTier.GROWTH));
			customerService.createCustomer(new CustomerApiModels.CreateCustomerRequest(
				"Northwind AI Studio",
				"founder@northwind.example",
				"China",
				CustomerTier.STARTUP));

			productRepository.saveAll(List.of(
				product("CLOUD-OBS-01", "Cloud Observability Suite", "Observability", "Enterprise telemetry and AIOps platform", "1299.00"),
				product("AI-OPS-02", "AI Operations Copilot", "AI Platform", "Operational copilots with approval workflows", "899.00"),
				product("DATA-MESH-03", "Data Mesh Gateway", "Data Platform", "Policy-driven data product gateway", "599.00"),
				product("MCP-EDGE-04", "MCP Edge Connector", "Integration", "Secure remote MCP connectivity for enterprise estates", "399.00")));

			knowledgeArticleRepository.saveAll(List.of(
				article("sales-playbook", "Enterprise Sales Playbook", "sales", "Focus first on pains, stakeholders, and measurable ROI before demo sequencing."),
				article("incident-template", "Incident Triage Template", "operations", "Start with impact, scope, customer tier, and active mitigations. Avoid speculative root cause."),
				article("mcp-adoption", "MCP Adoption Notes", "ai-platform", "Prefer Streamable-HTTP for new deployments and keep tool contracts stable for agent reuse.")));

			salesOrderService.createOrder(new OrderApiModels.CreateOrderRequest(
				"CUST-1001",
				List.of(
					new OrderApiModels.OrderLineInput("CLOUD-OBS-01", 5),
					new OrderApiModels.OrderLineInput("AI-OPS-02", 3)),
				"Seed order for enterprise pipeline demo"));
		};
	}

	private Product product(String sku, String name, String category, String description, String price) {
		Product product = new Product();
		product.setSku(sku);
		product.setName(name);
		product.setCategory(category);
		product.setDescription(description);
		product.setPrice(new BigDecimal(price));
		product.setActive(true);
		return product;
	}

	private KnowledgeArticle article(String slug, String title, String topic, String content) {
		KnowledgeArticle article = new KnowledgeArticle();
		article.setSlug(slug);
		article.setTitle(title);
		article.setTopic(topic);
		article.setContent(content);
		article.setPublished(true);
		return article;
	}
}
