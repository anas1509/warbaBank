package com.warba.customer.customer_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.warba.customer.customer_service.enumeration.CustomerClass;
import com.warba.customer.customer_service.enumeration.CustomerType;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
@Entity
@Table(name = "customer")
public class Customer extends BaseEntity{
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@NotNull
    @Column(name = "name")
    private String name;

	@Pattern(regexp = "\\d{7}")
	@NotNull
    @Column(name = "customer_id", unique = true)
    private String customerId;

	@NotNull
	@Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType;
	
	// Additional field to used in Kafka events logic
	@Enumerated(EnumType.STRING)
    @Column(name = "customer_class")
    private CustomerClass customerClass;

	@NotNull
    @Column(name = "address")
    private String address;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public static CustomerBuilder builder() {
        return new CustomerBuilder();
    }

	
    public static class CustomerBuilder {
    	private Long id;
        private String name;
        private String customerId;
        private CustomerType customerType;
        private String address;
        private CustomerClass customerClass;

        public CustomerBuilder withId(Long id) {
            this.id = id;
            return this;
        }
        
        public CustomerBuilder withName(String name) {
            this.name = name;
            return this;
        }
        
        public CustomerBuilder withClass(CustomerClass customerClass) {
            this.customerClass = customerClass;
            return this;
        }

        public CustomerBuilder withCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public CustomerBuilder withCustomerType(CustomerType customerType) {
            this.customerType = customerType;
            return this;
        }

        public CustomerBuilder withAddress(String address) {
            this.address = address;
            return this;
        }

        public Customer build() {
        	Customer customer = new Customer();
        	customer.setId(id);
        	customer.setName(this.name);
        	customer.setCustomerId(this.customerId);
        	customer.setCustomerType(this.customerType);
        	customer.setAddress(this.address);
        	customer.setCustomerClass(customerClass);;
            return customer;
        }
    }
	
}
