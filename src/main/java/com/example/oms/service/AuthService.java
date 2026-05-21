package com.example.oms.service;

import com.example.oms.dto.JwtResponse;
import com.example.oms.dto.LoginRequest;
import com.example.oms.entity.Role;
import com.example.oms.entity.User;
import com.example.oms.repository.RoleRepository;
import com.example.oms.repository.UserRepository;
import com.example.oms.config.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务层
 * 处理用户登录、注册、Token 生成等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * 用户登录
     *
     * @param request 登录请求（用户名 + 密码）
     * @return JWT 响应（包含 Token 和用户信息）
     */
    @Transactional
    public JwtResponse login(LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());

        // 查询用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 获取用户角色
        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        // 生成 JWT Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), roles);

        log.info("用户登录成功: username={}, roles={}", user.getUsername(), roles);

        return new JwtResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                roles
        );
    }

    /**
     * 注册用户
     *
     * @param user 用户信息
     * @return 注册后的用户
     */
    @Transactional
    public User registerUser(User user) {
        log.info("注册用户: username={}", user.getUsername());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 分配默认角色（普通用户）
        Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("默认角色不存在，请先初始化角色数据"));
        user.setRoles(Set.of(userRole));

        // 保存用户
        User savedUser = userRepository.save(user);
        log.info("用户注册成功: username={}, id={}", savedUser.getUsername(), savedUser.getId());

        return savedUser;
    }
}
