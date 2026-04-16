package com.githubstudycloud.gi025.sales;

import com.githubstudycloud.gi025.common.api.ApiResponse;
import com.githubstudycloud.gi025.common.web.RequestIdFilter;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class SalesOrderController {

	private final SalesOrderService salesOrderService;

	public SalesOrderController(SalesOrderService salesOrderService) {
		this.salesOrderService = salesOrderService;
	}

	@GetMapping
	public ApiResponse<List<OrderApiModels.OrderView>> listOrders(
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, salesOrderService.listOrders());
	}

	@GetMapping("/{orderNo}")
	public ApiResponse<OrderApiModels.OrderView> getOrder(
			@PathVariable String orderNo,
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, salesOrderService.getOrder(orderNo));
	}

	@PostMapping
	public ApiResponse<OrderApiModels.OrderView> createOrder(
			@Valid @RequestBody OrderApiModels.CreateOrderRequest request,
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, salesOrderService.createOrder(request));
	}

	@PostMapping("/quote")
	public ApiResponse<OrderApiModels.OrderQuote> quoteOrder(
			@Valid @RequestBody OrderApiModels.CreateOrderRequest request,
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, salesOrderService.quoteOrder(request.customerNo(), request.items()));
	}

	@PostMapping("/{orderNo}/approve")
	public ApiResponse<OrderApiModels.OrderView> approveOrder(
			@PathVariable String orderNo,
			@Valid @RequestBody OrderApiModels.ApproveOrderRequest request,
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, salesOrderService.approveOrder(orderNo, request.approver()));
	}
}
