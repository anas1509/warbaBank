package com.warba.account.account_service.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.warba.account.account_service.dto.AccountRequest;
import com.warba.account.account_service.dto.AccountResponse;
import com.warba.account.account_service.dto.CustomerResponse;
import com.warba.account.account_service.dto.DepositDto;
import com.warba.account.account_service.dto.WithdrawalDto;
import com.warba.account.account_service.enumeration.AccountType;
import com.warba.account.account_service.event.UpdateCustomerBalanceEvent;
import com.warba.account.account_service.model.Account;
import com.warba.account.account_service.repo.AccountRepository;
import com.warba.account.account_service.util.BusinessException;
import com.warba.account.account_service.util.HttpRequester;
import com.warba.account.account_service.util.JSONUtil;

@Service("AccountService")
public class AccountService {

	/**
	 * AccountService is a service class that provides business logic related to bank accounts.
	 * It offers methods to:
	 * - Retrieve account details by account number.
	 * - Retrieve all accounts for a given customer ID.
	 * - Create and update account information.
	 * - Deposit and withdraw funds from accounts, including validating account balance.
	 * - Validate account details before saving.
	 * - Communicate with the customer service via Kafka for updating customer balance.
	 * 
	 * This class interacts with the AccountRepository to perform CRUD operations on accounts,
	 * and uses HttpRequester to communicate with external customer services.
	 * It also sends events to update customer balances using Kafka.
	 */
	
	@Value("${system.customer.base.url}")
	private String customerBaseUrl;
	
	@Autowired
	private HttpRequester httpRequester;
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	private AccountRepository repo;
	
	public AccountResponse getAccountByAccountNumber(String accountNumber) {
		Account account = repo.getAccountByAccountNumber(accountNumber);
		return mapToAccountResponse(account);
	}
	
	public List<AccountResponse> getAccountsByCustomerId(String customerId) {
		
		List<Account> customerAccounts = repo.getAccountsByCustomerId(customerId);
		List<AccountResponse> accountsResponse = customerAccounts.stream().map(account -> mapToAccountResponse(account)).collect(Collectors.toList());
		return accountsResponse;
	}
	
	public AccountResponse createAccount(AccountRequest accountRequest) {
		
		 String url = customerBaseUrl + "/" + accountRequest.getCustomerId();
		 ParameterizedTypeReference<CustomerResponse> responseType = new ParameterizedTypeReference<CustomerResponse>() {};
		 
		 
		 ResponseEntity<CustomerResponse> response = httpRequester.url(url)
	                .method(HttpMethod.GET)
	                .contentType(MediaType.APPLICATION_JSON)
	                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
	                .send(responseType);
	        
	     if(response.getBody() == null) {
	    	 throw new BusinessException("customerNotFound", "The customer is not found", 400);
	     }
		 Account account = Account.builder()
	                .withAccountNumber(accountRequest.getAccountNumber())
	                .withBalance(accountRequest.getBalance())
	                .withStatus(accountRequest.getStatus())
	                .withCustomerId(accountRequest.getCustomerId())
	                .withAccountType(accountRequest.getAccountType())
	                .build();
		 account.setCreationDate(new Date());
		return updateAccount(account);
	}
	public AccountResponse updateAccount(Account account) {
		
		if(isAccountValid(account)) {
			if(account.getId() != null){// if it is update
				account.setUpdateDate(new Date());
			}
			repo.save(account);
			return mapToAccountResponse(account);
		}
		else {
			throw new BusinessException("invalidAccount", "invalid account", 400);
		}
	}
	
	public AccountResponse deposit(DepositDto depositDto ) {
		
		Account account = repo.getAccountByAccountNumber(depositDto.getAccountNumber());
		
		List<AccountResponse> allAccounts = getAccountsByCustomerId(account.getCustomerId());
		
		BigDecimal totalBalance = allAccounts.stream()
                .map(AccountResponse::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
		
		
		totalBalance = totalBalance.add(depositDto.getAmount());
		
		// Send an event to let the customer service update customer class according to the balance
		UpdateCustomerBalanceEvent event = new UpdateCustomerBalanceEvent();
		event.setCustomerId(account.getCustomerId());
		event.setTotalAmount(totalBalance);
		updateCustomerBalanceEventSender(event);
		
		account.setBalance(account.getBalance().add(depositDto.getAmount()));
		return updateAccount(account);
		
		
	}
	
	public AccountResponse withdrawal(WithdrawalDto withdrawalDto) {
		
		Account account = repo.getAccountByAccountNumber(withdrawalDto.getAccountNumber());
		if (account.getBalance().compareTo(withdrawalDto.getAmount()) == -1) {
			throw new BusinessException("insufficientBalance", "You have insufficient balance in your account", 400);
		}
		
		List<AccountResponse> allAccounts = getAccountsByCustomerId(account.getCustomerId());
		
		BigDecimal totalBalance = allAccounts.stream()
                .map(AccountResponse::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
		
		totalBalance = totalBalance.subtract(withdrawalDto.getAmount());
		
		// Send an event to let the customer service update customer class according to the balance
		UpdateCustomerBalanceEvent event = new UpdateCustomerBalanceEvent();
		event.setCustomerId(account.getCustomerId());
		event.setTotalAmount(totalBalance);
		updateCustomerBalanceEventSender(event);
		
		
		account.setBalance(account.getBalance().subtract(withdrawalDto.getAmount()));
		return updateAccount(account);
	}
	
	private void updateCustomerBalanceEventSender(UpdateCustomerBalanceEvent event) {
		kafkaTemplate.send("customerBalanceTopic", JSONUtil.convertObjectToJson(event));
	}
	
	
	//a simple method to validate the account
	private Boolean isAccountValid(Account account) {
		
		if(account != null) {
			List<Account> customerAccounts = repo.getAccountsByCustomerId(account.getCustomerId());
			StringBuilder errorMessage = new StringBuilder("Account is invalid:");
			boolean invalid = false;
			if(customerAccounts.size() == 9) {
				errorMessage.append(System.lineSeparator()).append("customer reaches the maximum number of accounts.");
				invalid = true;
			}
			
			boolean isAccountNumberExists = (account.getId() == null) && customerAccounts.stream()
	                .anyMatch(a -> account.getAccountNumber().equals(a.getAccountNumber()));
			if(isAccountNumberExists) {
				errorMessage.append(System.lineSeparator()).append("customer already has an account with the same account number.");
				invalid = true;
			}
			
			boolean isSalaryAccountExists = (account.getId() == null) && customerAccounts.stream()
	                .anyMatch(a -> a.getAccountType().equals(AccountType.SALARY));
			
			if(isSalaryAccountExists && account.getAccountType().equals(AccountType.SALARY)) {
				errorMessage.append(System.lineSeparator()).append("customer already has a salary account.");
				invalid = true;
			}
			
			if(account.getAccountNumber() != null && account.getCustomerId() != null) {
				if(account.getAccountNumber().length() == 10) {
					if(!account.getAccountNumber().substring(0,7).equals(account.getCustomerId())) {
						errorMessage.append(System.lineSeparator()).append("account number should be 10 digits starting with the customer ID.");
						invalid = true;
					}
				}else {
					errorMessage.append(System.lineSeparator()).append("account number should be 10 digits starting with the customer ID.");
					invalid = true;
				}
			}
			
			if(invalid) {
				throw new BusinessException("invalidAccount", errorMessage.toString(), 400);
			}
		}else {
			return false;
		}
		return true;
	}
	
	// simple mapper to map an Account to AccountResponse DTO
	private AccountResponse mapToAccountResponse(Account account) {
		return AccountResponse.builder()
				.withAccountNumber(account.getAccountNumber())
				.withAccountType(account.getAccountType())
				.withBalance(account.getBalance())
				.withCustomerId(account.getCustomerId())
				.withStatus(account.getStatus())
				.build();
	}
}
