package com.warba.account.account_service.dto;

import java.math.BigDecimal;

import com.warba.account.account_service.enumeration.AccountStatus;
import com.warba.account.account_service.enumeration.AccountType;


public class AccountRequest {

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
	    
	    
}
