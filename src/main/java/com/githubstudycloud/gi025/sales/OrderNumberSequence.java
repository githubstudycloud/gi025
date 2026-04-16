package com.githubstudycloud.gi025.sales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_number_sequences")
public class OrderNumberSequence {

	public static final String SALES_ORDER = "sales_order";

	@Id
	@Column(name = "sequence_name", nullable = false, length = 64)
	private String sequenceName;

	@Column(name = "next_value", nullable = false)
	private long nextValue;

	public long claimNextValue() {
		long currentValue = nextValue;
		nextValue = currentValue + 1;
		return currentValue;
	}
}
