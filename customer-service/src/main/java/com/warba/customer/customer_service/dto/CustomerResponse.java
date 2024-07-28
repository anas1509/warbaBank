package com.warba.customer.customer_service.dto;

import com.warba.customer.customer_service.enumeration.CustomerType;


public class CustomerResponse {
	
    private String name;
    private String customerId;
    private CustomerType customerType;
    private String address;
    
    
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
    
	 public static CustomerResponseBuilder builder() {
	        return new CustomerResponseBuilder();
	    }

	    public static class CustomerResponseBuilder {
	        private String name;
	        private String customerId;
	        private CustomerType customerType;
	        private String address;

	        public CustomerResponseBuilder withName(String name) {
	            this.name = name;
	            return this;
	        }

	        public CustomerResponseBuilder withCustomerId(String customerId) {
	            this.customerId = customerId;
	            return this;
	        }

	        public CustomerResponseBuilder withCustomerType(CustomerType customerType) {
	            this.customerType = customerType;
	            return this;
	        }

	        public CustomerResponseBuilder withAddress(String address) {
	            this.address = address;
	            return this;
	        }

	        public CustomerResponse build() {
	            CustomerResponse customerResponse = new CustomerResponse();
	            customerResponse.setName(this.name);
	            customerResponse.setCustomerId(this.customerId);
	            customerResponse.setCustomerType(this.customerType);
	            customerResponse.setAddress(this.address);
	            return customerResponse;
	        }
	    }
    
}
