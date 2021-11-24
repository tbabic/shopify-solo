ALTER TABLE public.item
    ADD COLUMN weight numeric(10, 4) DEFAULT 0;
    
ALTER TABLE public.item
    ADD COLUMN shopify_id text;