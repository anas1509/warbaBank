CREATE TABLE account (
    id serial8 NOT NULL,
    account_number VARCHAR(10) NOT NULL UNIQUE,
    balance numeric(19, 2) NOT NULL,
    status VARCHAR(255) NOT NULL,
    customer_id VARCHAR(10) NOT NULL,
    account_type VARCHAR(255) NOT NULL,
    "version" int8 NOT null DEFAULT 0,
	creation_date timestamp(6) DEFAULT now(),
	update_date timestamp(6) NULL,
    CONSTRAINT account_pk PRIMARY key (id),
    CONSTRAINT chk_account_number CHECK (account_number ~ '^[0-9]{10}$'),
    CONSTRAINT chk_customer_id CHECK (customer_id ~ '^[0-9]{7}$')
);