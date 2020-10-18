CREATE TABLE public.inventory
(
   id bigserial, 
   item text, 
   quantity integer, 
   links_json text, 
   CONSTRAINT inventory_pk PRIMARY KEY (id)
) 