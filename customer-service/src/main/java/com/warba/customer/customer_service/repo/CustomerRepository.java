package com.warba.customer.customer_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.warba.customer.customer_service.model.Customer;

@Repository("CustomerRepository")
public interface CustomerRepository extends JpaRepository<Customer, Long>{
	
	@Query("SELECT c from Customer c where c.customerId = :customerId")
	Customer getCustomerByCustomerId(@Param("customerId") String customerId);

}
