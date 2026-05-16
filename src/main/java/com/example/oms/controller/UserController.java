package com.example.oms.controller;

import com.example.oms.entity.User;
import com.example.oms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器（管理员专用）
 * 所有接口都需要 ROLE_ADMIN 权限
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // 所有接口都需要管理员权限
public class UserController {

    private final UserService userService;

    /**
     * 全局异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("用户管理异常: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 分页查询用户
     * GET /api/users?page=0&size=10&keyword=admin
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        log.info("分页查询用户: page={}, size={}, keyword={}", page, size, keyword);
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userService.getUsersByPage(keyword, pageRequest);
        
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", userPage.getContent());
        pageData.put("totalElements", userPage.getTotalElements());
        pageData.put("totalPages", userPage.getTotalPages());
        pageData.put("number", userPage.getNumber());
        pageData.put("size", userPage.getSize());
        
        return successResponse(pageData);
    }

    /**
     * 根据 ID 查询用户
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        log.info("查询用户: id={}", id);
        User user = userService.getUserById(id);
        return successResponse(user);
    }

    /**
     * 创建用户
     * POST /api/users
     *
     * 请求体示例:
     * {
     *   "username": "newuser",
     *   "password": "password123",
     *   "realName": "新用户",
     *   "email": "user@example.com",
     *   "roleNames": ["ROLE_USER"]
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("创建用户: username={}", request.getUsername());
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        
        User createdUser = userService.createUser(user, request.getRoleNames());
        return successResponse(createdUser);
    }

    /**
     * 更新用户
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        
        log.info("更新用户: id={}", id);
        
        User user = new User();
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus());
        user.setPassword(request.getPassword());
        
        User updatedUser = userService.updateUser(id, user, request.getRoleNames());
        return successResponse(updatedUser);
    }

    /**
     * 删除用户
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        log.info("删除用户: id={}", id);
        userService.deleteUser(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 启用/禁用用户
     * PATCH /api/users/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        log.info("切换用户状态: id={}", id);
        User user = userService.toggleUserStatus(id);
        return successResponse(user);
    }

    /**
     * 获取所有角色
     * GET /api/users/roles
     */
    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        log.info("查询所有角色");
        return successResponse(userService.getAllRoles());
    }

    /**
     * 构建成功响应
     */
    private ResponseEntity<Map<String, Object>> successResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "操作成功");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    // ===================== 内部 DTO 类 =====================

    /**
     * 创建用户请求 DTO
     */
    @lombok.Data
    public static class CreateUserRequest {
        @jakarta.validation.constraints.NotBlank
        private String username;
        @jakarta.validation.constraints.NotBlank
        private String password;
        private String realName;
        private String email;
        private String phone;
        private Integer status = 1;
        private List<String> roleNames;
    }

    /**
     * 更新用户请求 DTO
     */
    @lombok.Data
    public static class UpdateUserRequest {
        private String password;
        private String realName;
        private String email;
        private String phone;
        private Integer status;
        private List<String> roleNames;
    }
}
