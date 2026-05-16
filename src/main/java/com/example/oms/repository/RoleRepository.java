package com.example.oms.repository;

import com.example.oms.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色数据访问层
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 根据角色名称查询角色
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * 检查角色名称是否存在
     */
    boolean existsByRoleName(String roleName);
}
