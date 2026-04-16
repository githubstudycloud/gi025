package com.githubstudycloud.gi025;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.githubstudycloud.gi025.common.exception.BusinessException;
import com.githubstudycloud.gi025.mcp.EnterpriseMcpServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EnterpriseMcpServerTests {

	@Autowired
	private EnterpriseMcpServer enterpriseMcpServer;

	@Test
	void quoteOrderRejectsMalformedLineItemQuantity() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> enterpriseMcpServer.quoteOrder("CUST-1001", "CLOUD-OBS-01:abc"));

		assertEquals("Quantity must be a positive integer for token CLOUD-OBS-01:abc", exception.getMessage());
	}

	@Test
	void quoteOrderRejectsInvalidTokenShape() {
		BusinessException exception = assertThrows(
			BusinessException.class,
			() -> enterpriseMcpServer.quoteOrder("CUST-1001", "CLOUD-OBS-01"));

		assertEquals("Invalid token CLOUD-OBS-01, expected SKU:QTY", exception.getMessage());
	}
}
