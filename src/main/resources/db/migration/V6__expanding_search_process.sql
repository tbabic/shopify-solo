ALTER TABLE public.managed_order
  ADD COLUMN shipping_search_status text NOT NULL DEFAULT 'NONE';
