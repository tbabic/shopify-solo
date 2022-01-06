create table social_media_post (
	id  bigserial not null,
	content text not null,
	date timestamp,
	int4 integer,
	is_done boolean,
	primary key (id)
)
; 
