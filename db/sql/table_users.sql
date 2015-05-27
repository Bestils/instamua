create table user(
	user_id int unsigned auto_increment primary key,
    username varchar(30) unique,
    full_name varchar(50),
    location varchar(20),
    bio varchar(300),
    web_site varchar(50),
    profile_picture text
);

drop table user;

insert into user (username, full_name) 
values ('alice', 'Alice');

select * from user;


select x2.`user_id`, x2.`username`, x2.`full_name`, x2.`profile_picture` from `user` x2 
where (x2.`username` like '%ho%') or (x2.`full_name` like '%ho%');

create table user_private_info (
	user_id int unsigned,
    passwd char(60),
    salt varchar(60),
    email varchar(30),
    gender tinyint unsigned,
    profile_picture text,
    
    constraint user_private_info_user_fk foreign key (user_id) 
		references user(user_id)
        on update restrict
        on delete cascade
);

drop table user_private_info;

create table follower (
	user_id int unsigned,
    follower_id int unsigned,
    follow_back boolean,
    
    constraint follower_user_fk foreign key (user_id)
		references user(user_id)
        on update restrict
        on delete cascade,
        
	constraint follower_follower_fk foreign key (follower_id)
		references user(user_id)
        on update restrict
        on delete cascade
);

