# 创建数据库
create database if not exists code_generator;

# 创建用户表
create table if not exists user (
    id bigint primary key auto_increment comment '用户ID',
    userAccount varchar(256) not null comment '用户账号',
    userPassword varchar(512) not null comment '用户密码',
    username varchar(256) null comment '用户名',
    avatar varchar(1024)  null comment '头像',
    bio varchar(512) null comment '个人简介',
    role varchar(20) default 'user' not null comment '角色 admin:管理员 user:用户',
    isDeleted tinyint(1) not null default 0 comment '是否删除 0:否 1:是',
    createdAt datetime not null default current_timestamp comment '创建时间',
    updatedAt datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    unique key uk_userAccount (userAccount),
    key idx_username (username)
) comment '用户表' collate = utf8mb4_unicode_ci;

