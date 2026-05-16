package com.example.oms.service;

import com.example.oms.entity.Role;
import com.example.oms.entity.User;
import com.example.oms.repository.RoleRepository;
import com.example.oms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户管理服务层
 * 管理员专用：用户 CRUD、角色分配、状态管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户
     */
    @Transactional(readOnly = true)
    public Page<User> getUsersByPage(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByUsernameContainingOrRealNameContaining(
                keyword, keyword, pageable);
    }

    /**
     * 根据 ID 查询用户
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: id=" + id));
    }

    /**
     * 创建用户
     */
    @Transactional
    public User createUser(User user, List<String> roleNames) {
        log.info("创建用户: username={}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 设置角色
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("角色不存在: " + roleName));
            roles.add(role);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("用户创建成功: id={}", savedUser.getId());
        return savedUser;
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(Long id, User user, List<String> roleNames) {
        log.info("更新用户: id={}", id);

        User existingUser = getUserById(id);

        // 更新基本信息
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }

        // 如果提供了新密码，则更新密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 更新角色
        if (roleNames != null && !roleNames.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : roleNames) {
                Role role = roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new RuntimeException("角色不存在: " + roleName));
                roles.add(role);
            }
            existingUser.setRoles(roles);
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("用户更新成功: id={}", updatedUser.getId());
        return updatedUser;
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("删除用户: id={}", id);
        getUserById(id); // 检查是否存在
        userRepository.deleteById(id);
        log.info("用户删除成功: id={}", id);
    }

    /**
     * 启用/禁用用户
     */
    @Transactional
    public User toggleUserStatus(Long id) {
        log.info("切换用户状态: id={}", id);
        User user = getUserById(id);
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        User updatedUser = userRepository.save(user);
        log.info("用户状态已更新: id={}, status={}", updatedUser.getId(), updatedUser.getStatus());
        return updatedUser;
    }

    /**
     * 获取所有角色
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
