package com.example.oms.config;

import com.example.oms.entity.Role;
import com.example.oms.entity.User;
import com.example.oms.repository.RoleRepository;
import com.example.oms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 数据初始化器
 * 应用启动时自动创建默认管理员和用户账号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("开始初始化系统数据...");

        // 初始化角色
        initRoles();

        // 初始化默认用户
        initDefaultUsers();

        log.info("系统数据初始化完成");
    }

    /**
     * 初始化角色数据
     */
    private void initRoles() {
        if (!roleRepository.existsByRoleName("ROLE_ADMIN")) {
            Role adminRole = new Role();
            adminRole.setRoleName("ROLE_ADMIN");
            adminRole.setDescription("系统管理员，拥有所有权限");
            roleRepository.save(adminRole);
            log.info("创建角色: ROLE_ADMIN");
        }

        if (!roleRepository.existsByRoleName("ROLE_USER")) {
            Role userRole = new Role();
            userRole.setRoleName("ROLE_USER");
            userRole.setDescription("普通用户，可以查看和创建订单");
            roleRepository.save(userRole);
            log.info("创建角色: ROLE_USER");
        }
    }

    /**
     * 初始化默认用户
     */
    private void initDefaultUsers() {
        // 创建管理员账号
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN").orElse(null);
            Role userRole = roleRepository.findByRoleName("ROLE_USER").orElse(null);

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRealName("系统管理员");
            admin.setEmail("admin@oms.com");
            admin.setPhone("13800000001");
            admin.setStatus(1);
            admin.setRoles(Set.of(adminRole, userRole));
            userRepository.save(admin);
            log.info("创建默认管理员账号: admin / admin123");
        }

        // 创建普通用户账号
        if (!userRepository.existsByUsername("user")) {
            Role userRole = roleRepository.findByRoleName("ROLE_USER").orElse(null);

            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRealName("普通用户");
            user.setEmail("user@oms.com");
            user.setPhone("13800000002");
            user.setStatus(1);
            user.setRoles(Set.of(userRole));
            userRepository.save(user);
            log.info("创建默认普通用户账号: user / user123");
        }
    }
}
