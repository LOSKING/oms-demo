package com.example.oms.controller;

import com.example.oms.dto.JwtResponse;
import com.example.oms.dto.LoginRequest;
import com.example.oms.entity.User;
import com.example.oms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理登录、注册等认证相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 全局异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("认证异常: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 用户登录
     * POST /api/auth/login
     *
     * 请求体示例:
     * {
     *   "username": "admin",
     *   "password": "admin123"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        log.info("登录请求: username={}", request.getUsername());
        
        JwtResponse jwtResponse = authService.login(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登录成功");
        response.put("data", jwtResponse);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 用户注册
     * POST /api/auth/register
     *
     * 请求体示例:
     * {
     *   "username": "newuser",
     *   "password": "password123",
     *   "realName": "新用户",
     *   "email": "user@example.com"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody User user) {
        log.info("注册请求: username={}", user.getUsername());
        
        User savedUser = authService.registerUser(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "注册成功");
        response.put("data", savedUser);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前登录用户信息
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestAttribute(value = "currentUser", required = false) User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", user);
        return ResponseEntity.ok(response);
    }
}
