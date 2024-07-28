package com.warba.customer.customer_service.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.warba.customer.customer_service.dto.CustomerRequest;
import com.warba.customer.customer_service.dto.CustomerResponse;
import com.warba.customer.customer_service.model.Customer;
import com.warba.customer.customer_service.service.CustomerService;

@RestController
@RequestMapping("/api")
public class CustomerController {

	@Autowired
	private CustomerService customerService;
	
	@PostMapping("/create")
	public ResponseEntity<CustomerResponse> addCustomer(@RequestBody CustomerRequest customerRequest) {
		return new ResponseEntity<CustomerResponse>(customerService.addCustomer(customerRequest), HttpStatus.OK);
	}
	
	@PostMapping("/update")
	public ResponseEntity<CustomerResponse> updateCustomer(@RequestBody Customer customer) {
		return new ResponseEntity<CustomerResponse>(customerService.updateCustomer(customer), HttpStatus.OK);
	}
	
	 @GetMapping("/getAll")
	 public ResponseEntity<List<CustomerResponse>> getAllCustomers(){
		 return new ResponseEntity<List<CustomerResponse>>(customerService.getAllCustomers(), HttpStatus.OK);
	 }
	 
	 @GetMapping("/{customerId}")
	 public ResponseEntity<CustomerResponse> getCustomerByCustomerId(@PathVariable String customerId){
		 return new ResponseEntity<CustomerResponse>(customerService.getCustomerByCustomerId(customerId), HttpStatus.OK);
	 }
	 
	 @GetMapping("/balance/{customerId}")
	 public ResponseEntity<BigDecimal> getCustomerBalance(@PathVariable String customerId){
		 return new ResponseEntity<BigDecimal>(customerService.getCustomerBalance(customerId), HttpStatus.OK);
	 }

	
}
