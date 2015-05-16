create table users(
	user_id int unsigned auto_increment primary key,
    username varchar(30) unique,
    full_name varchar(50),
    location varchar(20),
    bio varchar(300),
    web_site varchar(50),
    profile_picture text
);

drop table users;

insert into users (username, full_name) 
values ('hotienvu', 'Ho Tien Vu');

select * from users;