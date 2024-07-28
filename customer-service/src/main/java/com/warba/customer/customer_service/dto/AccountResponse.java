package com.warba.customer.customer_service.dto;

import java.math.BigDecimal;

import com.warba.customer.customer_service.enumeration.AccountStatus;
import com.warba.customer.customer_service.enumeration.AccountType;


public class AccountResponse {

	private String accountNumber;
    private BigDecimal balance;
    private AccountStatus status;
    private String customerId;
    private AccountType accountType;
    
    
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public AccountStatus getStatus() {
		return status;
	}
	public void setStatus(AccountStatus status) {
		this.status = status;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public AccountType getAccountType() {
		return accountType;
	}
	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}
	
	
	public static AccountResponseBuilder builder() {
        return new AccountResponseBuilder();
    }

    public static class AccountResponseBuilder {
        private String accountNumber;
        private BigDecimal balance;
        private AccountStatus status;
        private String customerId;
        private AccountType accountType;

        public AccountResponseBuilder withAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public AccountResponseBuilder withBalance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public AccountResponseBuilder withStatus(AccountStatus status) {
            this.status = status;
            return this;
        }

        public AccountResponseBuilder withCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public AccountResponseBuilder withAccountType(AccountType accountType) {
            this.accountType = accountType;
            return this;
        }

        public AccountResponse build() {
        	AccountResponse accountResponse = new AccountResponse();
        	accountResponse.setAccountNumber(this.accountNumber);
        	accountResponse.setBalance(this.balance);
        	accountResponse.setStatus(this.status);
        	accountResponse.setCustomerId(this.customerId);
        	accountResponse.setAccountType(this.accountType);
            return accountResponse;
        }
    }

}
