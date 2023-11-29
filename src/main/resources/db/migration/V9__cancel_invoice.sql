create table refund (
   id  bigserial not null,
    invoice_date timestamp,
    invoice_id varchar(255),
    invoice_is_sent boolean not null,
    invoice_jir varchar(255),
    invoice_note varchar(255),
    invoice_number varchar(255),
    invoice_zki varchar(255),
    order_id int8,
    primary key (id)
);


alter table if exists item 
   add column refund_id int8;
    
    
alter table if exists item 
   add constraint FK_item_refund
   foreign key (refund_id) 
   references refund;

alter table if exists refund 
   add constraint FK_refund_managed_order
   foreign key (order_id)
   references managed_order;
   
ALTER TABLE item
   ALTER COLUMN order_id DROP NOT NULL;