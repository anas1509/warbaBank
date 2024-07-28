package com.warba.customer.customer_service.event;

import java.io.Serializable;
import java.math.BigDecimal;

public class UpdateCustomerBalanceEvent implements Serializable{

	// simple POJO class to receive Kafka events messages
	private static final long serialVersionUID = -1L;
	
	
	private String customerId;
	private BigDecimal totalAmount;
	
	public UpdateCustomerBalanceEvent() {
    }
	
	public UpdateCustomerBalanceEvent(String customerId, BigDecimal totalAmount) {
        this.customerId = customerId;
        this.totalAmount = totalAmount;
    }
	
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
	
	
}
