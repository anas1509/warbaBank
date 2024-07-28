package com.warba.customer.customer_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.warba.customer.customer_service.dto.AccountResponse;
import com.warba.customer.customer_service.dto.CustomerRequest;
import com.warba.customer.customer_service.dto.CustomerResponse;
import com.warba.customer.customer_service.enumeration.CustomerClass;
import com.warba.customer.customer_service.enumeration.CustomerType;
import com.warba.customer.customer_service.event.UpdateCustomerBalanceEvent;
import com.warba.customer.customer_service.model.Customer;
import com.warba.customer.customer_service.repo.CustomerRepository;
import com.warba.customer.customer_service.util.BusinessException;
import com.warba.customer.customer_service.util.HttpRequester;
import com.warba.customer.customer_service.util.JSONUtil;

public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private HttpRequester httpRequester;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }





    @Test
    public void testUpdateCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId("0123456");
        customer.setAddress("123 Main St");
        customer.setCustomerType(CustomerType.CORPORATE);
        customer.setName("John Doe");
        customer.setCustomerClass(CustomerClass.NORMAL);

        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse response = customerService.updateCustomer(customer);

        assertEquals("0123456", response.getCustomerId());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    public void testGetCustomerBalance() {
        String customerId = "0123456";
        String url = "http://localhost:8080/account/customerAccounts/" + customerId;

        AccountResponse accountResponse1 = AccountResponse.builder().withBalance(BigDecimal.valueOf(1000)).build();
        AccountResponse accountResponse2 = AccountResponse.builder().withBalance(BigDecimal.valueOf(2000)).build();
        List<AccountResponse> accountResponses = Arrays.asList(accountResponse1, accountResponse2);

        when(httpRequester.url(url)).thenReturn(httpRequester);
        when(httpRequester.method(HttpMethod.GET)).thenReturn(httpRequester);
        when(httpRequester.contentType(MediaType.APPLICATION_JSON)).thenReturn(httpRequester);
        when(httpRequester.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)).thenReturn(httpRequester);
        when(httpRequester.send(any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(accountResponses));

        BigDecimal balance = customerService.getCustomerBalance(customerId);

        assertEquals(BigDecimal.valueOf(3000), balance);
        verify(httpRequester, times(1)).send(any(ParameterizedTypeReference.class));
    }

    @Test
    public void testHandleCustomerUpdateBalanceEvent() {
        UpdateCustomerBalanceEvent event = new UpdateCustomerBalanceEvent();
        event.setCustomerId("0123456");
        event.setTotalAmount(BigDecimal.valueOf(10000));

        Customer customer = new Customer();
        customer.setCustomerId("0123456");
        when(customerRepository.getCustomerByCustomerId("0123456")).thenReturn(customer);

        customerService.handleCustomerUpdateBalanceEvent(JSONUtil.convertObjectToJson(event));

        verify(customerRepository, times(1)).getCustomerByCustomerId("0123456");
        verify(customerRepository, times(1)).save(any(Customer.class));
        assertEquals(CustomerClass.SPECIAL, customer.getCustomerClass());
    }

    @Test
    public void testIsCustomerValid() {
        Customer customer = new Customer();
        customer.setCustomerId("0123456");
        customer.setAddress("123 Main St");
        customer.setCustomerType(CustomerType.CORPORATE);
        customer.setName("John Doe");
        customer.setCustomerClass(CustomerClass.NORMAL);

        when(customerRepository.getCustomerByCustomerId("0123456")).thenReturn(null);

        boolean isValid = customerService.isCustomerValid(customer);

        assertTrue(isValid);
        verify(customerRepository, times(1)).getCustomerByCustomerId("0123456");
    }

    @Test
    public void testIsCustomerValid_DuplicateCustomerId() {
        Customer customer = new Customer();
        customer.setCustomerId("0123456");
        customer.setAddress("123 Main St");
        customer.setCustomerType(CustomerType.CORPORATE);
        customer.setName("John Doe");
        customer.setCustomerClass(CustomerClass.NORMAL);

        when(customerRepository.getCustomerByCustomerId("0123456")).thenReturn(customer);

        BusinessException exception = assertThrows(BusinessException.class, () -> customerService.isCustomerValid(customer));
        assertTrue(exception.getMessage().contains("there is an existing customer with the same customer ID."));
    }

    @Test
    public void testIsCustomerValid_InvalidCustomerId() {
        Customer customer = new Customer();
        customer.setCustomerId("01234");  // Invalid ID (length != 7)
        customer.setAddress("123 Main St");
        customer.setCustomerType(CustomerType.CORPORATE);
        customer.setName("John Doe");
        customer.setCustomerClass(CustomerClass.NORMAL);

        BusinessException exception = assertThrows(BusinessException.class, () -> customerService.isCustomerValid(customer));
        assertTrue(exception.getMessage().contains("custoemr ID should be 7 digits number."));
    }
    
    @Test
    public void testGetCustomerByCustomerId() {
        Customer customer = new Customer();
        customer.setCustomerId("0123456");
        when(customerRepository.getCustomerByCustomerId("0123456")).thenReturn(customer);

        CustomerResponse response = customerService.getCustomerByCustomerId("0123456");

        assertEquals("0123456", response.getCustomerId());
        verify(customerRepository, times(1)).getCustomerByCustomerId("0123456");
    }

    @Test
    public void testGetAllCustomers() {
        Customer customer1 = new Customer();
        customer1.setCustomerId("0123456");
        Customer customer2 = new Customer();
        customer2.setCustomerId("1234567");

        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer1, customer2));

        List<CustomerResponse> responses = customerService.getAllCustomers();

        assertEquals(2, responses.size());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    public void testAddCustomer() {
        CustomerRequest customerRequest = new CustomerRequest();
        customerRequest.setCustomerId("0123456");
        customerRequest.setAddress("123 Main St");
        customerRequest.setCustomerType(CustomerType.CORPORATE);
        customerRequest.setName("John Doe");
        customerRequest.setCustomerClass(CustomerClass.NORMAL);

        when(customerRepository.save(any(Customer.class))).thenReturn(new Customer());

        CustomerResponse response = customerService.addCustomer(customerRequest);

        assertEquals("0123456", response.getCustomerId());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }


    

    @Test
    public void testGetCustomerBalance_NoAccounts() {
        String customerId = "0123456";
        String url = "http://localhost:8080/account/customerAccounts/" + customerId;

        when(httpRequester.url(url)).thenReturn(httpRequester);
        when(httpRequester.method(HttpMethod.GET)).thenReturn(httpRequester);
        when(httpRequester.contentType(MediaType.APPLICATION_JSON)).thenReturn(httpRequester);
        when(httpRequester.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)).thenReturn(httpRequester);
        when(httpRequester.send(any(ParameterizedTypeReference.class))).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        BigDecimal balance = customerService.getCustomerBalance(customerId);

        assertEquals(BigDecimal.ZERO, balance);
        verify(httpRequester, times(1)).send(any(ParameterizedTypeReference.class));
    }

    @Test
    public void testHandleCustomerUpdateBalanceEvent_HighBalance() {
        UpdateCustomerBalanceEvent event = new UpdateCustomerBalanceEvent();
        event.setCustomerId("0123456");
        event.setTotalAmount(BigDecimal.valueOf(2000000));

        Customer customer = new Customer();
        customer.setCustomerId("0123456");
        when(customerRepository.getCustomerByCustomerId("0123456")).thenReturn(customer);

        customerService.handleCustomerUpdateBalanceEvent(JSONUtil.convertObjectToJson(event));

        verify(customerRepository, times(1)).getCustomerByCustomerId("0123456");
        verify(customerRepository, times(1)).save(any(Customer.class));
        assertEquals(CustomerClass.ELIETE, customer.getCustomerClass());
    }

    @Test
    public void testHandleCustomerUpdateBalanceEvent_LowBalance() {
        UpdateCustomerBalanceEvent event = new UpdateCustomerBalanceEvent();
        event.setCustomerId("0123456");
        event.setTotalAmount(BigDecimal.valueOf(-100));

        Customer customer = new Customer();
        customer.setCustomerId("0123456");
        when(customerRepository.getCustomerByCustomerId("0123456")).thenReturn(customer);

        customerService.handleCustomerUpdateBalanceEvent(JSONUtil.convertObjectToJson(event));

        verify(customerRepository, times(1)).getCustomerByCustomerId("0123456");
        verify(customerRepository, times(1)).save(any(Customer.class));
        assertEquals(CustomerClass.NORMAL, customer.getCustomerClass());
    }

    @Test
    public void testIsCustomerValid_NullCustomerId() {
        Customer customer = new Customer();
        customer.setCustomerId(null);
        customer.setAddress("123 Main St");
        customer.setCustomerType(CustomerType.CORPORATE);
        customer.setName("John Doe");
        customer.setCustomerClass(CustomerClass.NORMAL);

        BusinessException exception = assertThrows(BusinessException.class, () -> customerService.isCustomerValid(customer));
        assertTrue(exception.getMessage().contains("custoemr ID should be 7 digits number."));
    }

    @Test
    public void testIsCustomerValid_CustomerIdWithNonDigitCharacters() {
        Customer customer = new Customer();
        customer.setCustomerId("abc1234");
        customer.setAddress("123 Main St");
        customer.setCustomerType(CustomerType.CORPORATE);
        customer.setName("John Doe");
        customer.setCustomerClass(CustomerClass.NORMAL);

        BusinessException exception = assertThrows(BusinessException.class, () -> customerService.isCustomerValid(customer));
        assertTrue(exception.getMessage().contains("custoemr ID should be 7 digits number."));
    }

    @Test
    public void testIsCustomerValid_NullName() {
        Customer customer = new Customer();
        customer.setCustomerId("0123456");
        customer.setAddress("123 Main St");
        customer.setCustomerType(CustomerType.CORPORATE);
        customer.setName(null);
        customer.setCustomerClass(CustomerClass.NORMAL);

        BusinessException exception = assertThrows(BusinessException.class, () -> customerService.isCustomerValid(customer));
        assertTrue(exception.getMessage().contains("name is required."));
    }

    @Test
    public void testIsCustomerValid_NullAddress() {
        Customer customer = new Customer();
        customer.setCustomerId("0123456");
        customer.setAddress(null);
        customer.setCustomerType(CustomerType.CORPORATE);
        customer.setName("John Doe");
        customer.setCustomerClass(CustomerClass.NORMAL);

        BusinessException exception = assertThrows(BusinessException.class, () -> customerService.isCustomerValid(customer));
        assertTrue(exception.getMessage().contains("address is required."));
    }

    @Test
    public void testIsCustomerValid_NullCustomerType() {
        Customer customer = new Customer();
        customer.setCustomerId("0123456");
        customer.setAddress("123 Main St");
        customer.setCustomerType(null);
        customer.setName("John Doe");
        customer.setCustomerClass(CustomerClass.NORMAL);

        BusinessException exception = assertThrows(BusinessException.class, () -> customerService.isCustomerValid(customer));
        assertTrue(exception.getMessage().contains("customer type is required."));
    }
}

