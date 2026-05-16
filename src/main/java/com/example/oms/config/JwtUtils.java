package com.example.oms.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类
 * 负责生成、解析和验证 JWT Token
 */
@Component
public class JwtUtils {

    /**
     * JWT 密钥（从配置文件读取）
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token 过期时间（毫秒），默认 24 小时
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token
     *
     * @param username  用户名
     * @param userId    用户 ID
     * @param roles     角色列表
     * @return JWT Token 字符串
     */
    public String generateToken(String username, Long userId, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)                    // 主题：用户名
                .claim("userId", userId)              // 自定义声明：用户 ID
                .claim("roles", roles)                // 自定义声明：角色列表
                .issuedAt(now)                        // 签发时间
                .expiration(expiryDate)               // 过期时间
                .signWith(getSigningKey())            // 签名
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Long.class);
    }

    /**
     * 从 Token 中获取角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles", List.class);
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token 无效、过期或签名不匹配
            return false;
        }
    }
}
