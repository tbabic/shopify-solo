create table social_media_post (
	id  bigserial not null,
	content text not null,
	date timestamp,
	order_position int4,
	is_done boolean,
	primary key (id)
)
; 
