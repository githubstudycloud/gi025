package com.githubstudycloud.gi025.catalog;

import com.githubstudycloud.gi025.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

	@Column(nullable = false, unique = true, length = 64)
	private String sku;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, length = 64)
	private String category;

	@Column(nullable = false, precision = 18, scale = 2)
	private BigDecimal price;

	@Column(nullable = false, length = 500)
	private String description;

	@Column(nullable = false)
	private boolean active = true;
}
