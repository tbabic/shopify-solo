alter table if exists public.product 
	add column webshop_info_status text default 'active',
	add column webshop_info_quantity integer default 0; 
	
alter table if exists public.product_part_distribution
	add column optional boolean default false;

alter table if exists public.product_part
	add column optional boolean default false;