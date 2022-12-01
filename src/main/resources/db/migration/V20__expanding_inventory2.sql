
    
    create table product (
       id uuid not null,
        name varchar(255),
        webshop_info_id varchar(255),
        primary key (id)
    );
    
    create table product_part (
       id uuid not null,
        title varchar(255) not null,
        quantity int4 not null,
        description varchar(255),
        link varchar(255),
        alternative_description varchar(255),
        alternative_description2 varchar(255),
        alternative_link varchar(255),
        alternative_link2 varchar(255),
        primary key (id)
    );
    
    create table product_part_distribution (
       id uuid not null,
        product_id uuid not null,
        product_part_id uuid not null,
        assigned_quantity int4 not null,
        parts_used int4 not null,
        primary key (id)
    );
    
    alter table if exists product_part_distribution 
       add constraint FK85w28xctcat70sbxgn0nroc97 
       foreign key (product_id) 
       references product;
    
    alter table if exists product_part_distribution 
       add constraint FK5nhgm8hqhbb965700dmvnogec 
       foreign key (product_part_id) 
       references product_part