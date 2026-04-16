package com.githubstudycloud.gi025.sales;

import com.githubstudycloud.gi025.catalog.Product;
import com.githubstudycloud.gi025.catalog.ProductRepository;
import com.githubstudycloud.gi025.common.exception.BusinessException;
import com.githubstudycloud.gi025.common.exception.NotFoundException;
import com.githubstudycloud.gi025.customer.Customer;
import com.githubstudycloud.gi025.customer.CustomerTier;
import com.githubstudycloud.gi025.customer.CustomerService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesOrderService {

	private static final String DEFAULT_CURRENCY = "USD";

	private final SalesOrderRepository salesOrderRepository;

	private final ProductRepository productRepository;

	private final CustomerService customerService;

	private final OrderNumberSequenceRepository orderNumberSequenceRepository;

	public SalesOrderService(
			SalesOrderRepository salesOrderRepository,
			ProductRepository productRepository,
			CustomerService customerService,
			OrderNumberSequenceRepository orderNumberSequenceRepository) {
		this.salesOrderRepository = salesOrderRepository;
		this.productRepository = productRepository;
		this.customerService = customerService;
		this.orderNumberSequenceRepository = orderNumberSequenceRepository;
	}

	@Transactional(readOnly = true)
	public List<OrderApiModels.OrderView> listOrders() {
		return salesOrderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toView).toList();
	}

	@Transactional(readOnly = true)
	public OrderApiModels.OrderView getOrder(String orderNo) {
		return toView(getOrderEntity(orderNo));
	}

	@Transactional
	public OrderApiModels.OrderView createOrder(OrderApiModels.CreateOrderRequest request) {
		Customer customer = customerService.getCustomerEntity(request.customerNo());
		OrderApiModels.OrderQuote quote = quoteOrder(request.customerNo(), request.items());

		SalesOrder order = new SalesOrder();
		order.setOrderNo(nextOrderNo());
		order.setCustomer(customer);
		order.setStatus(OrderStatus.DRAFT);
		order.setCurrency(DEFAULT_CURRENCY);
		order.setNotes(request.notes());
		order.setTotalAmount(quote.quotedTotal());

		Map<String, Product> productMap = loadProducts(request.items());
		for (OrderApiModels.OrderLineInput line : request.items()) {
			Product product = productMap.get(line.sku());
			SalesOrderItem item = new SalesOrderItem();
			item.setProduct(product);
			item.setQuantity(line.quantity());
			item.setUnitPrice(product.getPrice());
			item.setLineTotal(product.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
			order.addItem(item);
		}

		return toView(salesOrderRepository.save(order));
	}

	@Transactional
	public OrderApiModels.OrderView approveOrder(String orderNo, String approver) {
		SalesOrder order = getOrderEntity(orderNo);
		if (order.getStatus() != OrderStatus.DRAFT) {
			throw new BusinessException("Only DRAFT orders can be approved");
		}
		order.setStatus(OrderStatus.APPROVED);
		order.setNotes(appendAuditNote(order.getNotes(), "Approved by %s".formatted(approver)));
		return toView(order);
	}

	@Transactional(readOnly = true)
	public OrderApiModels.OrderQuote quoteOrder(String customerNo, List<OrderApiModels.OrderLineInput> lineInputs) {
		Customer customer = customerService.getCustomerEntity(customerNo);
		Map<String, Product> products = loadProducts(lineInputs);

		List<OrderApiModels.OrderLineView> lines = lineInputs.stream().map(line -> {
			Product product = products.get(line.sku());
			BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(line.quantity()));
			return new OrderApiModels.OrderLineView(
				product.getSku(),
				product.getName(),
				line.quantity(),
				product.getPrice(),
				lineTotal);
		}).toList();

		BigDecimal subtotal = lines.stream()
			.map(OrderApiModels.OrderLineView::lineTotal)
			.reduce(BigDecimal.ZERO, BigDecimal::add)
			.setScale(2, RoundingMode.HALF_UP);
		BigDecimal discountRate = discountRate(customer.getTier());
		BigDecimal discountAmount = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
		BigDecimal quotedTotal = subtotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

		return new OrderApiModels.OrderQuote(
			customerNo,
			subtotal,
			discountRate,
			discountAmount,
			quotedTotal,
			DEFAULT_CURRENCY,
			lines);
	}

	@Transactional(readOnly = true)
	public SalesOrder getOrderEntity(String orderNo) {
		return salesOrderRepository.findByOrderNo(orderNo)
			.orElseThrow(() -> new NotFoundException("Order %s was not found".formatted(orderNo)));
	}

	private Map<String, Product> loadProducts(List<OrderApiModels.OrderLineInput> lineInputs) {
		Map<String, Product> productMap = new LinkedHashMap<>();
		List<String> skus = lineInputs.stream().map(OrderApiModels.OrderLineInput::sku).distinct().toList();
		productRepository.findAllBySkuIn(skus).forEach(product -> productMap.put(product.getSku(), product));
		for (String sku : skus) {
			if (!productMap.containsKey(sku)) {
				throw new NotFoundException("Product %s was not found".formatted(sku));
			}
		}
		return productMap;
	}

	private BigDecimal discountRate(CustomerTier tier) {
		return switch (tier) {
			case ENTERPRISE -> BigDecimal.valueOf(0.12);
			case GROWTH -> BigDecimal.valueOf(0.05);
			case STARTUP -> BigDecimal.ZERO;
		};
	}

	private String nextOrderNo() {
		OrderNumberSequence sequence = orderNumberSequenceRepository.findBySequenceName(OrderNumberSequence.SALES_ORDER)
			.orElseThrow(() -> new IllegalStateException("Missing sales_order sequence allocation row"));
		long sequenceValue = sequence.claimNextValue();
		orderNumberSequenceRepository.save(sequence);
		return "SO-%05d".formatted(sequenceValue);
	}

	private String appendAuditNote(String current, String addition) {
		if (current == null || current.isBlank()) {
			return addition;
		}
		return current + " | " + addition;
	}

	private OrderApiModels.OrderView toView(SalesOrder order) {
		List<OrderApiModels.OrderLineView> items = order.getItems().stream().map(item -> new OrderApiModels.OrderLineView(
			item.getProduct().getSku(),
			item.getProduct().getName(),
			item.getQuantity(),
			item.getUnitPrice(),
			item.getLineTotal())).toList();

		return new OrderApiModels.OrderView(
			order.getOrderNo(),
			order.getCustomer().getCustomerNo(),
			order.getCustomer().getName(),
			order.getStatus(),
			order.getTotalAmount(),
			order.getCurrency(),
			order.getNotes(),
			order.getCreatedAt(),
			items);
	}
}
