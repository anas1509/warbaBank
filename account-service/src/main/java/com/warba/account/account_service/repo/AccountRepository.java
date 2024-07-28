package com.warba.account.account_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.warba.account.account_service.model.Account;

@Repository("AccountRepository")
public interface AccountRepository extends JpaRepository<Account, Long>{

	
	@Query("SELECT a FROM Account a where customerId = :customerId")
	List<Account> getAccountsByCustomerId(@Param("customerId") String customerId);
	
	@Query("SELECT a FROM Account a where accountNumber = :accountNumber")
	Account getAccountByAccountNumber(@Param("accountNumber") String accountNumber);
}
