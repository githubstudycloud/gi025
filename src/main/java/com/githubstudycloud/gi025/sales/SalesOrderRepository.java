package com.githubstudycloud.gi025.sales;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {

	@EntityGraph(attributePaths = { "customer", "items", "items.product" })
	List<SalesOrder> findAllByOrderByCreatedAtDesc();

	@EntityGraph(attributePaths = { "customer", "items", "items.product" })
	Optional<SalesOrder> findByOrderNo(String orderNo);
}
