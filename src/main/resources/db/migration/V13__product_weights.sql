ALTER TABLE item
    ADD COLUMN weight numeric(10, 4) DEFAULT 0;
    
ALTER TABLE item
    ADD COLUMN shopify_id text;