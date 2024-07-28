package com.warba.customer.customer_service.dto;

import com.warba.customer.customer_service.enumeration.CustomerClass;
import com.warba.customer.customer_service.enumeration.CustomerType;

public class CustomerRequest {

	 	private String name;
	    private String customerId;
	    private CustomerType customerType;
	    private String address;
	    private CustomerClass customerClass;
	    
	    
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getCustomerId() {
			return customerId;
		}
		public void setCustomerId(String customerId) {
			this.customerId = customerId;
		}
		public CustomerType getCustomerType() {
			return customerType;
		}
		public void setCustomerType(CustomerType customerType) {
			this.customerType = customerType;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public CustomerClass getCustomerClass() {
			return customerClass;
		}
		public void setCustomerClass(CustomerClass customerClass) {
			this.customerClass = customerClass;
		}
	    
		 
}
