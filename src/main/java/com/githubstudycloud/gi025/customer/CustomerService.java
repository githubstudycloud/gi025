package com.githubstudycloud.gi025.customer;

import com.githubstudycloud.gi025.common.exception.BusinessException;
import com.githubstudycloud.gi025.common.exception.NotFoundException;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "customer-list", key = "#tier != null ? #tier.name() : 'ALL'")
	public List<CustomerApiModels.CustomerView> listCustomers(CustomerTier tier) {
		List<Customer> customers = tier == null
			? customerRepository.findAllByOrderByCreatedAtDesc()
			: customerRepository.findByTierOrderByCreatedAtDesc(tier);
		return customers.stream().map(this::toView).toList();
	}

	@Transactional(readOnly = true)
	public CustomerApiModels.CustomerView getCustomer(String customerNo) {
		return toView(getCustomerEntity(customerNo));
	}

	@Transactional
	@CacheEvict(value = "customer-list", allEntries = true)
	public CustomerApiModels.CustomerView createCustomer(CustomerApiModels.CreateCustomerRequest request) {
		if (customerRepository.existsByEmailIgnoreCase(request.email())) {
			throw new BusinessException("A customer with email %s already exists".formatted(request.email()));
		}

		Customer customer = new Customer();
		customer.setCustomerNo(nextCustomerNo());
		customer.setName(request.name().trim());
		customer.setEmail(request.email().trim().toLowerCase());
		customer.setCountry(request.country().trim());
		customer.setTier(request.tier() == null ? CustomerTier.GROWTH : request.tier());
		customer.setStatus(CustomerStatus.ACTIVE);
		return toView(customerRepository.save(customer));
	}

	@Transactional(readOnly = true)
	public Customer getCustomerEntity(String customerNo) {
		return customerRepository.findByCustomerNo(customerNo)
			.orElseThrow(() -> new NotFoundException("Customer %s was not found".formatted(customerNo)));
	}

	@Transactional(readOnly = true)
	public long count() {
		return customerRepository.count();
	}

	private String nextCustomerNo() {
		return "CUST-%04d".formatted(customerRepository.findAll().size() + 1001);
	}

	private CustomerApiModels.CustomerView toView(Customer customer) {
		return new CustomerApiModels.CustomerView(
			customer.getCustomerNo(),
			customer.getName(),
			customer.getEmail(),
			customer.getCountry(),
			customer.getTier(),
			customer.getStatus(),
			customer.getCreatedAt());
	}
}
