ALTER TABLE managed_order RENAME COLUMN city  TO shipping_info_city;
ALTER TABLE managed_order RENAME COLUMN company_name  TO shipping_info_company_name;
ALTER TABLE managed_order RENAME COLUMN country  TO shipping_info_country;
ALTER TABLE managed_order RENAME COLUMN full_name  TO shipping_info_full_name;
ALTER TABLE managed_order RENAME COLUMN other  TO shipping_info_other;
ALTER TABLE managed_order RENAME COLUMN phone_number  TO shipping_info_phone_number;
ALTER TABLE managed_order RENAME COLUMN postal_code  TO shipping_info_postal_code;
ALTER TABLE managed_order RENAME COLUMN street_and_number  TO shipping_info_street_and_number;
ALTER TABLE managed_order RENAME COLUMN is_receipt_sent  TO invoice_is_sent;
ALTER TABLE managed_order RENAME COLUMN payment_date  TO invoice_date;

	
alter table if exists managed_order 
	add column cancel_invoice_id varchar(255);


alter table if exists managed_order 
	add column cancel_invoice_jir varchar(255);


alter table if exists managed_order 
	add column cancel_invoice_note text;


alter table if exists managed_order 
	add column cancel_invoice_number varchar(255);


alter table if exists managed_order 
	add column cancel_invoice_zki varchar(255);


alter table if exists managed_order 
	add column invoice_jir varchar(255);


alter table if exists managed_order 
	add column invoice_note text;


alter table if exists managed_order 
	add column invoice_zki varchar(255);

 
	
alter table if exists managed_order 
	add column cancel_invoice_date timestamp;


alter table if exists managed_order 
	add column cancel_invoice_is_sent boolean;
