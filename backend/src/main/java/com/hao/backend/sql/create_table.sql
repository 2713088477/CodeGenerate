# 创建数据库
create database if not exists code_generator;

# 创建用户表
create table if not exists user (
    id bigint primary key auto_increment comment '用户ID',
    userAccount varchar(32) not null comment '用户账号',
    userPassword varchar(64) not null comment '用户密码',
    username varchar(32) null comment '用户名',
    avatar varchar(1024)  null comment '头像',
    bio varchar(512) null comment '个人简介',
    role varchar(20) default 'user' not null comment '角色 admin:管理员 user:用户',
    isDeleted tinyint(1) not null default 0 comment '是否删除 0:否 1:是',
    createdAt datetime not null default current_timestamp comment '创建时间',
    updatedAt datetime not null default current_timestamp on update current_timestamp comment '更新时间',
    unique key uk_userAccount (userAccount),
    key idx_username (username)
) comment '用户表' collate = utf8mb4_unicode_ci;

-- 应用表
create table app
(
    id           bigint primary key auto_increment comment 'id',
    appName      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initPrompt   text                               null comment '应用初始化的 prompt',
    codeGenType  varchar(64)                        null comment '代码生成类型（枚举）',
    deployKey    varchar(64)                        null comment '部署标识',
    deployedTime datetime                           null comment '部署时间',
    priority     int      default 99                not null comment '优先级，数值越小越靠前，<=10 为精选应用，默认 99',
    isDeleted     tinyint(1)  default 0             not null comment '是否删除',
    userId       bigint                             not null comment '创建用户id',
    createdAt   datetime default CURRENT_TIMESTAMP  not null comment '创建时间',
    updatedAt   datetime default CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE KEY uk_deployKey (deployKey), -- 确保部署标识唯一
    INDEX idx_appName (appName),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (userId)            -- 提升基于用户 ID 的查询性能
) comment '应用表' collate = utf8mb4_unicode_ci;
