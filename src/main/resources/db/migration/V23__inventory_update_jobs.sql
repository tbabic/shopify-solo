ALTER TABLE public.managed_order
   ADD COLUMN inventory_job text;
   
UPDATE public.managed_order
	SET inventory_job = 'COMPLETED';