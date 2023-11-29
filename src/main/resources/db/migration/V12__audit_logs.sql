CREATE TABLE audit_log
(
   id uuid, 
   previous_state text, 
   next_state text,
   changed_by text,
   log_time timestamp
 ) 