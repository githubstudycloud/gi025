package com.githubstudycloud.gi025.catalog;

import com.githubstudycloud.gi025.common.exception.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Transactional(readOnly = true)
	public List<ProductApiModels.ProductView> listActiveProducts() {
		return productRepository.findAllByActiveTrueOrderByCategoryAscNameAsc().stream().map(this::toView).toList();
	}

	@Transactional(readOnly = true)
	public Product getProductEntity(String sku) {
		return productRepository.findBySku(sku)
			.orElseThrow(() -> new NotFoundException("Product %s was not found".formatted(sku)));
	}

	private ProductApiModels.ProductView toView(Product product) {
		return new ProductApiModels.ProductView(
			product.getSku(),
			product.getName(),
			product.getCategory(),
			product.getDescription(),
			product.getPrice(),
			product.isActive());
	}
}
