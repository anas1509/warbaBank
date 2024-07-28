CREATE TABLE customer (
    id serial8 NOT NULL,
    name VARCHAR(255) NOT NULL,
    customer_id VARCHAR(10) NOT NULL,
    address VARCHAR(255) NOT NULL,
    customer_type VARCHAR(255) NOT NULL,
    customer_class VARCHAR(50) NULL,
    "version" int8 NOT null DEFAULT 0,
	creation_date timestamp(6) DEFAULT now(),
	update_date timestamp(6) NULL,
    CONSTRAINT customer_pk PRIMARY key (id),
    CONSTRAINT chk_customer_id CHECK (customer_id ~ '^[0-9]{7}$')
);

