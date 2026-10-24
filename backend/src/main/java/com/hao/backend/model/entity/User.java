package com.hao.backend.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户表 实体类。
 *
 * @author <a href="https://github.com/2713088477">艺艺生辉</a>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Id(keyType = KeyType.Generator,value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户账号
     */
    @Column("userAccount")
    private String userAccount;

    /**
     * 用户密码
     */
    @Column("userPassword")
    private String userPassword;

    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 角色 admin:管理员 user:用户
     */
    private String role;

    /**
     * 是否删除 0:否 1:是
     */
    @Column(value = "isDeleted", isLogicDelete = true)
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    @Column("createdAt")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column("updatedAt")
    private LocalDateTime updatedAt;

}
