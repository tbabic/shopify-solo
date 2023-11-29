alter table if exists product 
	add column version integer default 0; 
	
alter table if exists product_part_distribution
	add column version integer default 0; 

alter table if exists product_part
	add column version integer default 0; 