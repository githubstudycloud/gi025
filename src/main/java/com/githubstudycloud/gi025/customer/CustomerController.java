package com.githubstudycloud.gi025.customer;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@GetMapping
	public ApiResponse<List<CustomerApiModels.CustomerView>> listCustomers(
			@RequestParam(required = false) String tier,
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, customerService.listCustomers(CustomerTier.fromNullable(tier)));
	}

	@GetMapping("/{customerNo}")
	public ApiResponse<CustomerApiModels.CustomerView> getCustomer(
			@PathVariable String customerNo,
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, customerService.getCustomer(customerNo));
	}

	@PostMapping
	public ApiResponse<CustomerApiModels.CustomerView> createCustomer(
			@Valid @RequestBody CustomerApiModels.CreateCustomerRequest request,
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, customerService.createCustomer(request));
	}
}
