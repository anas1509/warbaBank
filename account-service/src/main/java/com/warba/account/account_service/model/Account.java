package com.warba.account.account_service.model;

import java.math.BigDecimal;

import com.warba.account.account_service.enumeration.AccountStatus;
import com.warba.account.account_service.enumeration.AccountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


@Entity
@Table(name = "account")
public class Account extends BaseEntity{
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Pattern(regexp = "\\d{10}")
    @Column(unique = true)
    private String accountNumber;

    @NotNull
    @Column(name = "balance")
    private BigDecimal balance;

    @NotNull
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @NotNull
    @Pattern(regexp = "\\d{7}")
    @Column(name = "customer_id")
    private String customerId;
    
    @NotNull
    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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
	

    public static AccountBuilder builder() {
        return new AccountBuilder();
    }

    public static class AccountBuilder {
        private String accountNumber;
        private BigDecimal balance;
        private AccountStatus status;
        private String customerId;
        private AccountType accountType;

        public AccountBuilder withAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public AccountBuilder withBalance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public AccountBuilder withStatus(AccountStatus status) {
            this.status = status;
            return this;
        }

        public AccountBuilder withCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public AccountBuilder withAccountType(AccountType accountType) {
            this.accountType = accountType;
            return this;
        }

        public Account build() {
            Account account = new Account();
            account.setAccountNumber(this.accountNumber);
            account.setBalance(this.balance);
            account.setStatus(this.status);
            account.setCustomerId(this.customerId);
            account.setAccountType(this.accountType);
            return account;
        }
    }

  
}
