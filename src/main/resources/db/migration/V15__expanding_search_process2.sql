ALTER TABLE public.managed_order
  ADD COLUMN search_procedure_status text NOT NULL DEFAULT 'NONE';
ALTER TABLE public.managed_order
  ADD COLUMN search_procedure_history_json text;
ALTER TABLE public.managed_order
  ADD COLUMN old_tracking_number varchar(255);
  
ALTER TABLE public.managed_order 
  ADD COLUMN shipping_search_status_date timestamp;
ALTER TABLE public.managed_order 
  ADD COLUMN search_procedure_status_date timestamp;

