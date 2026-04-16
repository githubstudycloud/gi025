package com.githubstudycloud.gi025.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.githubstudycloud.gi025.common.exception.BusinessException;
import com.githubstudycloud.gi025.customer.CustomerApiModels;
import com.githubstudycloud.gi025.customer.CustomerService;
import com.githubstudycloud.gi025.customer.CustomerTier;
import com.githubstudycloud.gi025.knowledge.KnowledgeApiModels;
import com.githubstudycloud.gi025.knowledge.KnowledgeService;
import com.githubstudycloud.gi025.sales.OrderApiModels;
import com.githubstudycloud.gi025.sales.SalesOrderService;
import java.util.Arrays;
import java.util.List;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class EnterpriseMcpServer {

	private final CustomerService customerService;

	private final SalesOrderService salesOrderService;

	private final KnowledgeService knowledgeService;

	private final ObjectMapper objectMapper;

	public EnterpriseMcpServer(
			CustomerService customerService,
			SalesOrderService salesOrderService,
			KnowledgeService knowledgeService,
			ObjectMapper objectMapper) {
		this.customerService = customerService;
		this.salesOrderService = salesOrderService;
		this.knowledgeService = knowledgeService;
		this.objectMapper = objectMapper;
	}

	@McpTool(name = "list_customers", description = "List CRM customers with an optional tier filter")
	public List<CustomerApiModels.CustomerView> listCustomers(
			@McpToolParam(description = "Optional tier filter: ENTERPRISE, GROWTH, STARTUP", required = false)
			String tier) {
		return customerService.listCustomers(CustomerTier.fromNullable(tier));
	}

	@McpTool(name = "create_customer", description = "Create a new CRM customer account")
	public CustomerApiModels.CustomerView createCustomer(
			@McpToolParam(description = "Customer legal name", required = true) String name,
			@McpToolParam(description = "Primary email", required = true) String email,
			@McpToolParam(description = "Customer country or region", required = true) String country,
			@McpToolParam(description = "Tier: ENTERPRISE, GROWTH, STARTUP", required = false) String tier) {
		return customerService.createCustomer(new CustomerApiModels.CreateCustomerRequest(
			name,
			email,
			country,
			CustomerTier.fromNullable(tier)));
	}

	@McpTool(name = "quote_order", description = "Quote a sales order. Use lineItems format SKU:QTY,SKU:QTY")
	public OrderApiModels.OrderQuote quoteOrder(
			@McpToolParam(description = "Customer number, for example CUST-1001", required = true) String customerNo,
			@McpToolParam(description = "Comma-separated line items like CLOUD-OBS-01:10,AI-OPS-02:4", required = true)
			String lineItems) {
		return salesOrderService.quoteOrder(customerNo, parseLineItems(lineItems));
	}

	@McpTool(name = "approve_order", description = "Approve an existing draft order")
	public OrderApiModels.OrderView approveOrder(
			@McpToolParam(description = "Order number, for example SO-00001", required = true) String orderNo,
			@McpToolParam(description = "Approver display name", required = true) String approver) {
		return salesOrderService.approveOrder(orderNo, approver);
	}

	@McpTool(name = "search_knowledge", description = "Search enterprise knowledge articles")
	public List<KnowledgeApiModels.ArticleView> searchKnowledge(
			@McpToolParam(description = "Keyword to search in titles, topics, or content", required = true)
			String keyword) {
		return knowledgeService.search(keyword);
	}

	@McpResource(
		name = "pricing-policy",
		uri = "kb://policies/pricing",
		mimeType = "text/markdown",
		description = "Pricing and discount policy used by the demo sales platform")
	public String pricingPolicy() {
		return """
			# Pricing Policy

			- ENTERPRISE customers receive a default 12%% commercial discount.
			- GROWTH customers receive a default 5%% commercial discount.
			- STARTUP customers use list price by default.
			- Quotes returned by the MCP server are informational and must be approved before booking revenue.
			""";
	}

	@McpResource(
		name = "customer-portfolio",
		uri = "crm://customers/portfolio",
		mimeType = "application/json",
		description = "Current customer portfolio exported as JSON")
	public String customerPortfolio() {
		return toJson(customerService.listCustomers(null));
	}

	@McpResource(
		name = "customer-summary",
		uri = "crm://customers/{customerNo}/summary",
		mimeType = "application/json",
		description = "Lookup a single customer summary by customer number")
	public String customerSummary(String customerNo) {
		return toJson(customerService.getCustomer(customerNo));
	}

	@McpResource(
		name = "ops-runbook",
		uri = "ops://runbooks/mcp-enterprise-base",
		mimeType = "text/markdown",
		description = "Operational runbook for the enterprise MCP base")
	public String operationsRunbook() {
		return """
			# MCP Operations Runbook

			- Authenticate every HTTP request with the `X-API-Key` header.
			- The default MCP transport in this project is Streamable-HTTP on `/mcp`.
			- Health checks are exposed on `/actuator/health`.
			- Demo business APIs live under `/api/v1`.
			- Remote MCP tool callbacks are disabled by default and can be enabled with the `mcp-client` profile.
			""";
	}

	@McpPrompt(name = "sales_follow_up", description = "Create a follow-up prompt for an account executive")
	public String salesFollowUpPrompt(
			@McpArg(name = "customerNo", description = "Customer number", required = true) String customerNo) {
		CustomerApiModels.CustomerView customer = customerService.getCustomer(customerNo);
		return """
			Prepare a concise account executive follow-up message.
			Customer number: %s
			Name: %s
			Tier: %s
			Country: %s

			Include:
			1. Current relationship summary.
			2. Likely cross-sell opportunity.
			3. One practical next step.
			""".formatted(
			customer.customerNo(),
			customer.name(),
			customer.tier(),
			customer.country());
	}

	@McpPrompt(name = "incident_triage", description = "Create an incident triage prompt for order operations")
	public String incidentTriagePrompt(
			@McpArg(name = "orderNo", description = "Order number", required = true) String orderNo) {
		OrderApiModels.OrderView order = salesOrderService.getOrder(orderNo);
		return """
			Triage an order operations incident using the following context.
			Order: %s
			Customer: %s (%s)
			Status: %s
			Total: %s %s
			Notes: %s

			Provide:
			1. Potential blast radius.
			2. Immediate containment checks.
			3. Suggested remediation path.
			""".formatted(
			order.orderNo(),
			order.customerName(),
			order.customerNo(),
			order.status(),
			order.totalAmount(),
			order.currency(),
			order.notes());
	}

	private List<OrderApiModels.OrderLineInput> parseLineItems(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new BusinessException("lineItems must not be blank");
		}

		List<String> tokens = Arrays.stream(raw.split(","))
			.map(String::trim)
			.filter(token -> !token.isBlank())
			.toList();
		if (tokens.isEmpty()) {
			throw new BusinessException("lineItems must contain at least one SKU:QTY token");
		}

		return tokens.stream().map(this::parseLineItemToken).toList();
	}

	private OrderApiModels.OrderLineInput parseLineItemToken(String token) {
		String[] parts = token.split(":", -1);
		if (parts.length != 2) {
			throw new BusinessException("Invalid token %s, expected SKU:QTY".formatted(token));
		}

		String sku = parts[0].trim();
		String quantityText = parts[1].trim();
		if (sku.isBlank() || quantityText.isBlank()) {
			throw new BusinessException("Invalid token %s, expected SKU:QTY".formatted(token));
		}
		if (!quantityText.matches("\\d+")) {
			throw new BusinessException("Quantity must be a positive integer for token %s".formatted(token));
		}

		try {
			int quantity = Integer.parseInt(quantityText);
			if (quantity <= 0) {
				throw new BusinessException("Quantity must be greater than zero for token %s".formatted(token));
			}
			return new OrderApiModels.OrderLineInput(sku, quantity);
		}
		catch (NumberFormatException exception) {
			throw new BusinessException("Quantity is too large for token %s".formatted(token));
		}
	}

	private String toJson(Object value) {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
		}
		catch (JsonProcessingException exception) {
			throw new BusinessException("Failed to serialize MCP resource payload");
		}
	}
}
