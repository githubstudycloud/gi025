package com.githubstudycloud.gi025.sales;

import com.githubstudycloud.gi025.common.model.BaseEntity;
import com.githubstudycloud.gi025.customer.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sales_orders")
public class SalesOrder extends BaseEntity {

	@Column(name = "order_no", nullable = false, unique = true, length = 32)
	private String orderNo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private OrderStatus status;

	@Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
	private BigDecimal totalAmount;

	@Column(nullable = false, length = 16)
	private String currency;

	@Column(length = 1000)
	private String notes;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SalesOrderItem> items = new ArrayList<>();

	public void addItem(SalesOrderItem item) {
		item.setOrder(this);
		this.items.add(item);
	}
}
