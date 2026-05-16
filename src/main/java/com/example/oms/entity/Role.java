package com.example.oms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 角色实体类
 * 定义系统中的用户角色（管理员、普通用户等）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色名称（唯一）
     * ROLE_ADMIN: 管理员
     * ROLE_USER: 普通用户
     */
    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    /**
     * 角色描述
     */
    @Column(name = "description", length = 255)
    private String description;
}
