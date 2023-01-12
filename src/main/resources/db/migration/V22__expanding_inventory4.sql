alter table if exists public.product 
	add column version integer default 0; 
	
alter table if exists public.product_part_distribution
	add column version integer default 0; 

alter table if exists public.product_part
	add column version integer default 0; 