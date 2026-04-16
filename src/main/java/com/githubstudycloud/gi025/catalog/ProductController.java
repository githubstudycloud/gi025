package com.githubstudycloud.gi025.catalog;

import com.githubstudycloud.gi025.common.api.ApiResponse;
import com.githubstudycloud.gi025.common.web.RequestIdFilter;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public ApiResponse<List<ProductApiModels.ProductView>> listProducts(
			@RequestAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE) String requestId) {
		return ApiResponse.success(requestId, productService.listActiveProducts());
	}
}
