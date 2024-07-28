package com.warba.account.account_service.controller;

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

import com.warba.account.account_service.dto.AccountRequest;
import com.warba.account.account_service.dto.AccountResponse;
import com.warba.account.account_service.dto.DepositDto;
import com.warba.account.account_service.dto.WithdrawalDto;
import com.warba.account.account_service.model.Account;
import com.warba.account.account_service.service.AccountService;

@RestController
@RequestMapping("/api")
public class AccountController {
	
	@Autowired
	private AccountService accountservice;
	
	
	@PostMapping("/create")
	public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest accountRequest) {
		return new ResponseEntity<AccountResponse>(accountservice.createAccount(accountRequest), HttpStatus.OK);
	}
	
	@PostMapping("/update")
	public ResponseEntity<AccountResponse> updateAccount(@RequestBody Account account) {
		return new ResponseEntity<AccountResponse>(accountservice.updateAccount(account), HttpStatus.OK);
	}
	
	 @GetMapping("/customerAccounts/{customerId}")
	 public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(@PathVariable String customerId){
		 return new ResponseEntity<List<AccountResponse>>(accountservice.getAccountsByCustomerId(customerId), HttpStatus.OK);
	 }
	 
	 @GetMapping("/{accountNumber}")
	 public ResponseEntity<AccountResponse> getAccountByAccountNumber(@PathVariable String accountNumber){
		 return new ResponseEntity<AccountResponse>(accountservice.getAccountByAccountNumber(accountNumber), HttpStatus.OK);
	 }

	 @PostMapping("/withdrawal")
	 public ResponseEntity<AccountResponse> withdrawal(@RequestBody WithdrawalDto withdrawal) {
		return new ResponseEntity<AccountResponse>(accountservice.withdrawal(withdrawal), HttpStatus.OK);
	}
	 
	 @PostMapping("/deposit")
	 public ResponseEntity<AccountResponse> deposit(@RequestBody DepositDto depositDto) {
		return new ResponseEntity<AccountResponse>(accountservice.deposit(depositDto), HttpStatus.OK);
	}
}
