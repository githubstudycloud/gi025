package com.githubstudycloud.gi025.catalog;

import java.math.BigDecimal;

public final class ProductApiModels {

	private ProductApiModels() {
	}

	public record ProductView(
			String sku,
			String name,
			String category,
			String description,
			BigDecimal price,
			boolean active) {
	}
}
