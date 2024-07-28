package com.warba.account.account_service.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import com.warba.account.account_service.dto.AccountRequest;
import com.warba.account.account_service.dto.AccountResponse;
import com.warba.account.account_service.dto.CustomerResponse;
import com.warba.account.account_service.dto.DepositDto;
import com.warba.account.account_service.dto.WithdrawalDto;
import com.warba.account.account_service.enumeration.AccountStatus;
import com.warba.account.account_service.enumeration.AccountType;
import com.warba.account.account_service.model.Account;
import com.warba.account.account_service.repo.AccountRepository;
import com.warba.account.account_service.service.AccountService;
import com.warba.account.account_service.util.BusinessException;
import com.warba.account.account_service.util.HttpRequester;

@SpringBootTest
public class AccountServiceTest {
	
	@Mock
    private AccountRepository accountRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private HttpRequester httpRequester;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAccountByAccountNumber() {
        Account account = new Account();
        account.setAccountNumber("1234567890");
        when(accountRepository.getAccountByAccountNumber("1234567890")).thenReturn(account);

        AccountResponse response = accountService.getAccountByAccountNumber("1234567890");

        assertEquals("1234567890", response.getAccountNumber());
        verify(accountRepository, times(1)).getAccountByAccountNumber("1234567890");
    }

    @Test
    public void testGetAccountsByCustomerId() {
        Account account1 = new Account();
        account1.setCustomerId("0123456");
        Account account2 = new Account();
        account2.setCustomerId("0123456");

        when(accountRepository.getAccountsByCustomerId("0123456")).thenReturn(Arrays.asList(account1, account2));

        List<AccountResponse> responses = accountService.getAccountsByCustomerId("0123456");

        assertEquals(2, responses.size());
        verify(accountRepository, times(1)).getAccountsByCustomerId("0123456");
    }

    @Test
    public void testCreateAccount_CustomerNotFound() {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCustomerId("0123456");
        accountRequest.setAccountNumber("0123456789");
        accountRequest.setBalance(BigDecimal.valueOf(1000));
        accountRequest.setStatus(AccountStatus.ACTIVE);
        accountRequest.setAccountType(AccountType.SAVING);

        String url = "http://localhost:8080/customer/" + accountRequest.getCustomerId();
        when(httpRequester.url(url)).thenReturn(httpRequester);
        when(httpRequester.method(HttpMethod.GET)).thenReturn(httpRequester);
        when(httpRequester.contentType(MediaType.APPLICATION_JSON)).thenReturn(httpRequester);
        when(httpRequester.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)).thenReturn(httpRequester);
        when(httpRequester.send(any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(null));

        assertThrows(BusinessException.class, () -> accountService.createAccount(accountRequest));
    }

    @Test
    public void testCreateAccount_Success() {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCustomerId("0123456");
        accountRequest.setAccountNumber("0123456789");
        accountRequest.setBalance(BigDecimal.valueOf(1000));
        accountRequest.setStatus(AccountStatus.ACTIVE);
        accountRequest.setAccountType(AccountType.SAVING);

        String url = "http://localhost:8080/customer/" + accountRequest.getCustomerId();
        when(httpRequester.url(url)).thenReturn(httpRequester);
        when(httpRequester.method(HttpMethod.GET)).thenReturn(httpRequester);
        when(httpRequester.contentType(MediaType.APPLICATION_JSON)).thenReturn(httpRequester);
        when(httpRequester.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)).thenReturn(httpRequester);
        when(httpRequester.send(any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(new CustomerResponse()));

        when(accountRepository.save(any(Account.class))).thenReturn(new Account());

        AccountResponse response = accountService.createAccount(accountRequest);

        assertEquals(accountRequest.getAccountNumber(), response.getAccountNumber());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void testDeposit_Success() {
        DepositDto depositDto = new DepositDto();
        depositDto.setAccountNumber("1234567890");
        depositDto.setAmount(BigDecimal.valueOf(100));

        Account account = new Account();
        account.setCustomerId("0123456");
        account.setBalance(BigDecimal.valueOf(500));
        when(accountRepository.getAccountByAccountNumber("1234567890")).thenReturn(account);

        AccountResponse accountResponse = AccountResponse.builder()
                .withAccountNumber("1234567890")
                .withBalance(BigDecimal.valueOf(500))
                .withCustomerId("0123456")
                .withStatus(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.getAccountsByCustomerId("0123456")).thenReturn(Collections.singletonList(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountService.deposit(depositDto);

        assertEquals(BigDecimal.valueOf(600), response.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }

    @Test
    public void testWithdrawal_InsufficientBalance() {
        WithdrawalDto withdrawalDto = new WithdrawalDto();
        withdrawalDto.setAccountNumber("1234567890");
        withdrawalDto.setAmount(BigDecimal.valueOf(600));

        Account account = new Account();
        account.setCustomerId("0123456");
        account.setBalance(BigDecimal.valueOf(500));
        when(accountRepository.getAccountByAccountNumber("1234567890")).thenReturn(account);

        assertThrows(BusinessException.class, () -> accountService.withdrawal(withdrawalDto));
    }

    @Test
    public void testWithdrawal_Success() {
        WithdrawalDto withdrawalDto = new WithdrawalDto();
        withdrawalDto.setAccountNumber("1234567890");
        withdrawalDto.setAmount(BigDecimal.valueOf(100));

        Account account = new Account();
        account.setCustomerId("0123456");
        account.setBalance(BigDecimal.valueOf(500));
        when(accountRepository.getAccountByAccountNumber("1234567890")).thenReturn(account);

        AccountResponse accountResponse = AccountResponse.builder()
                .withAccountNumber("1234567890")
                .withBalance(BigDecimal.valueOf(500))
                .withCustomerId("0123456")
                .withStatus(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.getAccountsByCustomerId("0123456")).thenReturn(Collections.singletonList(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountService.withdrawal(withdrawalDto);

        assertEquals(BigDecimal.valueOf(400), response.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }
    
    @Test
    public void testCreateAccount_MaxAccountsReached() {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCustomerId("0123456");
        accountRequest.setAccountNumber("0123456789");
        accountRequest.setBalance(BigDecimal.valueOf(1000));
        accountRequest.setStatus(AccountStatus.ACTIVE);
        accountRequest.setAccountType(AccountType.SAVING);

        String url = "http://localhost:8080/customer/" + accountRequest.getCustomerId();
        when(httpRequester.url(url)).thenReturn(httpRequester);
        when(httpRequester.method(HttpMethod.GET)).thenReturn(httpRequester);
        when(httpRequester.contentType(MediaType.APPLICATION_JSON)).thenReturn(httpRequester);
        when(httpRequester.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)).thenReturn(httpRequester);
        when(httpRequester.send(any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(new CustomerResponse()));

        List<Account> customerAccounts = Collections.nCopies(9, new Account());
        when(accountRepository.getAccountsByCustomerId("0123456")).thenReturn(customerAccounts);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountRequest));
        assertTrue(exception.getMessage().contains("customer reaches the maximum number of accounts."));
    }

    @Test
    public void testCreateAccount_DuplicateAccountNumber() {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCustomerId("0123456");
        accountRequest.setAccountNumber("0123456789");
        accountRequest.setBalance(BigDecimal.valueOf(1000));
        accountRequest.setStatus(AccountStatus.ACTIVE);
        accountRequest.setAccountType(AccountType.SAVING);

        String url = "http://localhost:8080/customer/" + accountRequest.getCustomerId();
        when(httpRequester.url(url)).thenReturn(httpRequester);
        when(httpRequester.method(HttpMethod.GET)).thenReturn(httpRequester);
        when(httpRequester.contentType(MediaType.APPLICATION_JSON)).thenReturn(httpRequester);
        when(httpRequester.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)).thenReturn(httpRequester);
        when(httpRequester.send(any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(new CustomerResponse()));

        Account existingAccount = new Account();
        existingAccount.setAccountNumber("0123456789");
        List<Account> customerAccounts = Collections.singletonList(existingAccount);
        when(accountRepository.getAccountsByCustomerId("0123456")).thenReturn(customerAccounts);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountRequest));
        assertTrue(exception.getMessage().contains("customer already has an account with the same account number."));
    }

    @Test
    public void testCreateAccount_MultipleSalaryAccounts() {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setCustomerId("0123456");
        accountRequest.setAccountNumber("0123456789");
        accountRequest.setBalance(BigDecimal.valueOf(1000));
        accountRequest.setStatus(AccountStatus.ACTIVE);
        accountRequest.setAccountType(AccountType.SALARY);

        String url = "http://localhost:8080/customer/" + accountRequest.getCustomerId();
        when(httpRequester.url(url)).thenReturn(httpRequester);
        when(httpRequester.method(HttpMethod.GET)).thenReturn(httpRequester);
        when(httpRequester.contentType(MediaType.APPLICATION_JSON)).thenReturn(httpRequester);
        when(httpRequester.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)).thenReturn(httpRequester);
        when(httpRequester.send(any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(new CustomerResponse()));

        Account existingAccount = new Account();
        existingAccount.setAccountType(AccountType.SALARY);
        List<Account> customerAccounts = Collections.singletonList(existingAccount);
        when(accountRepository.getAccountsByCustomerId("0123456")).thenReturn(customerAccounts);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(accountRequest));
        assertTrue(exception.getMessage().contains("customer already has a salary account."));
    }

    @Test
    public void testDeposit_ZeroAmount() {
        DepositDto depositDto = new DepositDto();
        depositDto.setAccountNumber("1234567890");
        depositDto.setAmount(BigDecimal.ZERO);

        Account account = new Account();
        account.setCustomerId("0123456");
        account.setBalance(BigDecimal.valueOf(500));
        when(accountRepository.getAccountByAccountNumber("1234567890")).thenReturn(account);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.deposit(depositDto));
        assertTrue(exception.getMessage().contains("Deposit amount must be greater than zero."));
    }

    @Test
    public void testWithdrawal_ZeroAmount() {
        WithdrawalDto withdrawalDto = new WithdrawalDto();
        withdrawalDto.setAccountNumber("1234567890");
        withdrawalDto.setAmount(BigDecimal.ZERO);

        Account account = new Account();
        account.setCustomerId("0123456");
        account.setBalance(BigDecimal.valueOf(500));
        when(accountRepository.getAccountByAccountNumber("1234567890")).thenReturn(account);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.withdrawal(withdrawalDto));
        assertTrue(exception.getMessage().contains("Withdrawal amount must be greater than zero."));
    }

    @Test
    public void testUpdateAccount() {
        Account account = new Account();
        account.setAccountNumber("1234567890");
        account.setCustomerId("0123456");
        account.setBalance(BigDecimal.valueOf(1000));
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountType(AccountType.SAVING);

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponse response = accountService.updateAccount(account);

        assertEquals("1234567890", response.getAccountNumber());
        assertEquals(BigDecimal.valueOf(1000), response.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void testIsAccountValid_MaxAccountsReached() {
        AccountRequest account = new AccountRequest();
        account.setCustomerId("0123456");

        List<Account> customerAccounts = Collections.nCopies(9, new Account());
        when(accountRepository.getAccountsByCustomerId("0123456")).thenReturn(customerAccounts);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(account));
        assertTrue(exception.getMessage().contains("customer reaches the maximum number of accounts."));
    }

    @Test
    public void testIsAccountValid_DuplicateAccountNumber() {
    	AccountRequest account = new AccountRequest();
        account.setCustomerId("0123456");
        account.setAccountNumber("0123456789");

        Account existingAccount = new Account();
        existingAccount.setAccountNumber("0123456789");
        List<Account> customerAccounts = Collections.singletonList(existingAccount);
        when(accountRepository.getAccountsByCustomerId("0123456")).thenReturn(customerAccounts);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(account));
        assertTrue(exception.getMessage().contains("customer already has an account with the same account number."));
    }

    @Test
    public void testIsAccountValid_MultipleSalaryAccounts() {
    	AccountRequest account = new AccountRequest();
        account.setCustomerId("0123456");
        account.setAccountType(AccountType.SALARY);

        Account existingAccount = new Account();
        existingAccount.setAccountType(AccountType.SALARY);
        List<Account> customerAccounts = Collections.singletonList(existingAccount);
        when(accountRepository.getAccountsByCustomerId("0123456")).thenReturn(customerAccounts);

        BusinessException exception = assertThrows(BusinessException.class, () -> accountService.createAccount(account));
        assertTrue(exception.getMessage().contains("customer already has a salary account."));
    }

}
