package com.githubstudycloud.gi025.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderApiModels {

	private OrderApiModels() {
	}

	public record OrderLineInput(
			@NotBlank(message = "sku is required") String sku,
			@Positive(message = "quantity must be greater than zero") int quantity) {
	}

	public record CreateOrderRequest(
			@NotBlank(message = "customerNo is required") String customerNo,
			@NotEmpty(message = "items must not be empty") @Valid List<OrderLineInput> items,
			String notes) {
	}

	public record ApproveOrderRequest(@NotBlank(message = "approver is required") String approver) {
	}

	public record OrderLineView(
			String sku,
			String productName,
			int quantity,
			BigDecimal unitPrice,
			BigDecimal lineTotal) {
	}

	public record OrderView(
			String orderNo,
			String customerNo,
			String customerName,
			OrderStatus status,
			BigDecimal totalAmount,
			String currency,
			String notes,
			Instant createdAt,
			List<OrderLineView> items) {
	}

	public record OrderQuote(
			String customerNo,
			BigDecimal subtotal,
			BigDecimal discountRate,
			BigDecimal discountAmount,
			BigDecimal quotedTotal,
			String currency,
			List<OrderLineView> items) {
	}
}
