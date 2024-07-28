## How to run:

To run this application you should have docker and docker-compose installed in you machine then run the following command on the root of this directory:

```bash
    docker-compose -f docker-compose.yml up
```
Then you can run the services locally

## Notes:
    There is no major differences between spring profiles so this project is mainly for development environment.
    There is no Security model in this project as there was no time to implement it.

## tecnical tools:
    programming language: java 17
    framework: spring boot 3.3.2
    build tool: Apache Maven 3.9.0
    unit testing: Junit
    DB: postges 15
    Kafka: 7.0.1 - as dokcer image
    docker and docker-compose
    Spring Gateway
    Eureka server and clients


## This simple project has four services:

    1. Account-service
    2. Customer-service
    3. Gateway service
    4. Service-dicovery service

### Account-service

    model layer:
        1. Account
            - fields:
                accountNumber - String
                balance - BigDecimal
                status - Enum
                customerId - String
                accountType - Enum
            
            - The entity extends BaseEntity class
        2. BaseEntity
            - fields:
                version - Long
                creationDate - Date
                updateDate - Date

    repository layer:
        simple JPA repository that has the following methods:
            - getAccountsByCustomerId
            - getAccountByAccountNumber

    service layer:
         AccountService is a service class that provides business logic related to bank accounts.
         It offers methods to:
            - Retrieve account details by account number.
            - Retrieve all accounts for a given customer ID.
            - Create and update account information.
            - Deposit and withdraw funds from accounts, including validating account balance.
            - Validate account details before saving.
            - Communicate with the customer service via Kafka for updating customer balance.
            
        This class interacts with the AccountRepository to perform CRUD operations on accounts,
        and uses HttpRequester to communicate with external customer services.
        It also sends events to update customer balances using Kafka.

    controller layer:
        AccountController is a REST controller that exposes endpoints for managing bank accounts.
        It provides methods to:
            - Create a new account.
            - Update an existing account.
            - Retrieve all accounts for a given customer ID.
            - Retrieve an account by its account number.
            - Deposit funds into an account.
            - Withdraw funds from an account.
        
        This controller interacts with the AccountService to perform business logic operations on accounts.
        Each endpoint maps to a specific HTTP method and URL pattern, accepting and returning JSON data.

### Customer-service
    model layer:

        Customer
            Fields:
                customerId - String
                name - String
                customerType - Enum
                address - String
                customerClass - Enum
            The entity extends BaseEntity class

        BaseEntity
            Fields:
                version - Long
                creationDate - Date
                updateDate - Date
    Repository layer:
        Simple JPA repository that provides methods for CRUD operations on customers.

    Service layer:
        CustomerService is a service class that provides business logic related to customers. It offers methods to:
        Retrieve customer details by customer ID.
        Retrieve all customers.
        Add and update customer information.
        Calculate the total balance of a customer by querying related account services.
        Handle balance update events received through Kafka.
        Validate customer information.
        This class interacts with the CustomerRepository to perform CRUD operations on customers, and uses HttpRequester to communicate with external account services. It also listens to Kafka messages to update customer class based on their total balance.

    Controller layer:
        CustomerController is a REST controller that exposes endpoints for managing customers. It provides methods to:
        Create a new customer.
        Update an existing customer.
        Retrieve customer details by customer ID.
        Retrieve all customers.
        This controller interacts with the CustomerService to perform business logic operations on customers. Each endpoint maps to a specific HTTP method and URL pattern, accepting and returning JSON data.

### Gateway Service
    The Gateway service is a Spring Cloud Gateway that routes requests to the appropriate microservices. It acts as a reverse proxy, handling requests and directing them to the appropriate service based on the URL patterns.

    The gateway forwar requests to both account a customer services  

### Eureka Discovery Service
    The Eureka Discovery Service is used for service registration and discovery. It allows the microservices to find and communicate with each other without hard-coding the hostname and port.

    All other services are registerd as clients in the discovery server.

## Utils:

- In this project, I made a cutom Exception "BusinessException.java" that is used to handle validations and returns a meaningful message to the consumers
- Controller Advice: to handle the response of the exceptions.
- Junit: used to create a simple test cases for the services in the project.
- Kfaka: 
    Whenever a account balance is updated, an message from the Accoun-service will be published Kafka with a message that contains customer number and customer total balance from all accounts they have.
    The Customer-service is listening to Kafka events, whenever a messege is published, the service will consume it and update customer class to either: Normal, Special or Eliete.



