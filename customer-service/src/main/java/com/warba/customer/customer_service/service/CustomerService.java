package com.warba.customer.customer_service.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.warba.customer.customer_service.dto.AccountResponse;
import com.warba.customer.customer_service.dto.CustomerRequest;
import com.warba.customer.customer_service.dto.CustomerResponse;
import com.warba.customer.customer_service.enumeration.CustomerClass;
import com.warba.customer.customer_service.event.UpdateCustomerBalanceEvent;
import com.warba.customer.customer_service.model.Customer;
import com.warba.customer.customer_service.repo.CustomerRepository;
import com.warba.customer.customer_service.util.BusinessException;
import com.warba.customer.customer_service.util.HttpRequester;
import com.warba.customer.customer_service.util.JSONUtil;

import org.springframework.core.ParameterizedTypeReference;



@Service("CustomerService")
public class CustomerService {
	
	/**
	 * CustomerService is a service class that provides business logic related to customers.
	 * It offers methods to:
	 * - Retrieve customer details by customer ID.
	 * - Retrieve all customers.
	 * - Add and update customer information.
	 * - Calculate the total balance of a customer by querying related account services.
	 * - Handle balance update events received through Kafka.
	 * - Validate customer information.
	 * 
	 * This class interacts with the CustomerRepository to perform CRUD operations on customers,
	 * and uses HttpRequester to communicate with external account services.
	 * It also listens to Kafka messages to update customer class based on their total balance.
	 */
	
	@Value("${system.account.base.url}")
	private String accountBaseUrl;
	
	@Autowired
	private CustomerRepository repo;
	
	@Autowired
	private HttpRequester httpRequester;

	public CustomerResponse getCustomerByCustomerId(String customerId) {
		Customer customer = repo.getCustomerByCustomerId(customerId);
		return mapToCustomerResponse(customer);
	}
	
	public List<CustomerResponse> getAllCustomers() {
		List<Customer> customers = repo.findAll();
		return customers.stream().map(customer -> mapToCustomerResponse(customer)).collect(Collectors.toList());
	}
	
	public CustomerResponse addCustomer(CustomerRequest customerRequest) {
		
		Customer customer = Customer.builder()
				.withAddress(customerRequest.getAddress())
				.withCustomerId(customerRequest.getCustomerId())
				.withCustomerType(customerRequest.getCustomerType())
				.withName(customerRequest.getName())
				.withClass(customerRequest.getCustomerClass())
				.withId(null).build();
		customer.setCreationDate(new Date());
		return updateCustomer(customer);
	}
	
	public CustomerResponse updateCustomer(Customer customer) {
		
		if(isCustomerValid(customer)) {
			if(customer.getId() != null) { // if update
				customer.setUpdateDate(new Date());
			}
			repo.save(customer);
			return mapToCustomerResponse(customer);
		}
		else {
			throw new BusinessException("invalidAccount", "invalid account", 400);
		}
	}
	
	public BigDecimal getCustomerBalance(String customerId) {
		
		String url = accountBaseUrl + "/customerAccounts/" + customerId;
		
		ParameterizedTypeReference<List<AccountResponse>> responseType = new ParameterizedTypeReference<List<AccountResponse>>() {};
		ResponseEntity<List<AccountResponse>> response = httpRequester.url(url)
                .method(HttpMethod.GET)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .send(responseType);


        // Calculate balance based on response
        BigDecimal totalBalance = response.getBody().stream()
                .map(AccountResponse::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalBalance;
	}
	
	// a simple event listener that listen to kafka events whenever a customer account balance is updated to change the customer class accordingly
	@KafkaListener(topics = "customerBalanceTopic")
	public void handleCustomerUpdateBalanceEvent(String eventString) {
		
		// when customer total balance:
		// 0 - 5000 : then customer class is NORMAL
		// 5000 - 20000 : then customer class is SPECIAL
		// grater than 20000  : then customer class is ELIETE
		UpdateCustomerBalanceEvent event = JSONUtil.convertJsonToObject(eventString, UpdateCustomerBalanceEvent.class);
		Customer customer = repo.getCustomerByCustomerId(event.getCustomerId());
		
		if(event.getTotalAmount().compareTo(BigDecimal.valueOf(5000L))  == -1) {
			customer.setCustomerClass(CustomerClass.NORMAL);
		}
		
		if((event.getTotalAmount().compareTo(BigDecimal.valueOf(5000L))  >= 0) && event.getTotalAmount().compareTo(BigDecimal.valueOf(20000L))  == -1) {
			customer.setCustomerClass(CustomerClass.SPECIAL);
		}
		
		if(event.getTotalAmount().compareTo(BigDecimal.valueOf(20000))  >= 0) {
			customer.setCustomerClass(CustomerClass.ELIETE);
		}
		
		repo.save(customer);
	}
	
	public boolean isCustomerValid(Customer customer) {
		
		StringBuilder errorMessage = new StringBuilder("Customer is invalid:");
		boolean invalid = false;
		
		
		boolean isCustomerIdExists = repo.getCustomerByCustomerId(customer.getCustomerId()) == null ? false:true;
		if(isCustomerIdExists) {
			errorMessage.append(System.lineSeparator()).append("there is an existing customer with the same customer ID.");
			invalid = true;
		}
		

		if(customer.getCustomerId() == null || customer.getCustomerId().length() != 7) {
			errorMessage.append(System.lineSeparator()).append("custoemr ID should be 7 digits number.");
			invalid = true;
		}
		
		if(invalid) {
			throw new BusinessException("invalidAccount", errorMessage.toString(), 400);
		}
		
		return true;
	}
	
	// simple mapper to map a Customer to CustomerResponse DTO
	private CustomerResponse mapToCustomerResponse(Customer customer) {
		return CustomerResponse.builder()
				.withAddress(customer.getAddress())
				.withCustomerId(customer.getCustomerId())
				.withCustomerType(customer.getCustomerType())
				.withName(customer.getName())
				.build();
	}
	
}
