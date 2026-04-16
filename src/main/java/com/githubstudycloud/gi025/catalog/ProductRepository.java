package com.githubstudycloud.gi025.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

	List<Product> findAllByActiveTrueOrderByCategoryAscNameAsc();

	List<Product> findAllBySkuIn(Collection<String> skus);

	Optional<Product> findBySku(String sku);
}
