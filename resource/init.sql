create database fish_game;
use fish_game;
create table record(
       id bigint auto_increment primary key comment '主键',
       create_time DATETIME comment '创建时间',
       consume int comment '消耗',
       account varchar(30) comment '账号'
);

create table coin
(
    id      int auto_increment comment '序号'
        primary key,
    num     varchar(30) default '0' not null comment '数量',
    account varchar(20)             not null comment '账号',
    rocket  int                     null comment '鱼雷',
    bomb    int                     null comment '炸弹',
    chao    int                     null comment '超声波'
);

create table user
(
    id       int auto_increment comment '序号'
        primary key,
    account  varchar(30) default '123456' not null comment '账号',
    password varchar(20) default '123456' not null comment '密码'
);

insert into user (id, account, password) values (null,'player','123456');