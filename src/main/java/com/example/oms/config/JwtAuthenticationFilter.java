package com.example.oms.config;

import com.example.oms.entity.User;
import com.example.oms.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 * 
 * 每次请求时拦截，从请求头提取 JWT Token，验证后设置 Spring Security 上下文
 * 继承 OncePerRequestFilter 确保每个请求只执行一次
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    /**
     * 过滤逻辑
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头获取 Token
            String jwt = getJwtFromRequest(request);

            // 如果 Token 存在且有效
            if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
                // 从 Token 中提取用户名
                String username = jwtUtils.getUsernameFromToken(jwt);

                // 查询用户信息
                User user = userRepository.findByUsername(username).orElse(null);

                if (user != null && user.getStatus() == 1) {
                    // 将角色转换为 Spring Security 的权限格式
                    var authorities = user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                            .collect(Collectors.toList());

                    // 创建认证令牌
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置到 Security 上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("用户 {} 认证成功，角色: {}", username, authorities);
                }
            }
        } catch (Exception ex) {
            log.error("JWT 认证失败: {}", ex.getMessage());
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 JWT Token
     * 格式: Authorization: Bearer <token>
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
