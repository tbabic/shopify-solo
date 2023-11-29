ALTER TABLE managed_order
   ADD COLUMN inventory_job text;
   
UPDATE managed_order
	SET inventory_job = 'COMPLETED';