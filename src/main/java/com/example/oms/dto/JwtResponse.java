package com.example.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JWT 响应 DTO
 * 登录成功后返回给前端
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token 类型
     */
    private String type = "Bearer";

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色列表
     */
    private List<String> roles;
}
