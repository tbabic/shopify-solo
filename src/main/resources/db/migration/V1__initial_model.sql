create table item (
   id  bigserial not null,
	discount varchar(255),
	name varchar(255),
	price varchar(255),
	quantity int4 not null,
	tax_rate varchar(255),
	order_id int8 not null,
	primary key (id)
)
; 

create table managed_order (
   type varchar(31) not null,
	id  bigserial not null,
	contact varchar(255),
	creation_date timestamp,
	is_canceled boolean not null,
	is_fulfilled boolean not null,
	personal_takeover boolean not null,
	sending_date timestamp,
	city varchar(255),
	company_name varchar(255),
	country varchar(255),
	full_name varchar(255),
	other varchar(255),
	phone_number varchar(255),
	postal_code varchar(255),
	street_and_number varchar(255),
	tracking_number varchar(255),
	currency varchar(255),
	invoice_id varchar(255),
	invoice_number varchar(255),
	is_paid boolean,
	is_receipt_sent boolean,
	is_tender_sent boolean,
	note varchar(255),
	payment_date timestamp,
	payment_type varchar(255),
	shopify_order_id varchar(255),
	shopify_order_number varchar(255),
	tender_id varchar(255),
	tender_number varchar(255),
	giveaway_platform varchar(255),
	primary key (id)
)
; 

alter table if exists managed_order 
   add constraint UK_managed_order_invoice_id unique (invoice_id)
; 

alter table if exists managed_order 
   add constraint UK_managed_order_shopify_order_id unique (shopify_order_id)
; 

alter table if exists managed_order 
   add constraint UK_managed_order_tender_id unique (tender_id)
; 

alter table if exists item 
   add constraint FK_item_managed_order
   foreign key (order_id) 
   references managed_order
;