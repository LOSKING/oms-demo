package com.example.oms.repository;

import com.example.oms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户（用于登录认证）
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(String email);

    /**
     * 根据用户名或真实姓名模糊查询（分页）
     */
    Page<User> findByUsernameContainingOrRealNameContaining(
            String username, String realName, Pageable pageable);
}
