ALTER TABLE item
  ADD COLUMN net_price NUMERIC(12,4);
ALTER TABLE item
  ADD COLUMN gross_price NUMERIC(12,4);
  
  
alter table if exists managed_order 
	add column invoice_vat_amount NUMERIC(12,4);
	
alter table if exists refund 
	add column invoice_vat_amount NUMERIC(12,4);