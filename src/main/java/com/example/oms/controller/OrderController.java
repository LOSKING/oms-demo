package com.example.oms.controller;

import com.example.oms.entity.Order;
import com.example.oms.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单 REST API 控制器
 * 
 * @RestController: 组合注解 = @Controller + @ResponseBody，返回 JSON 数据
 * @RequestMapping: 定义基础路径 /api/orders
 * @RequiredArgsConstructor: 构造器注入
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ===================== 全局异常处理 =====================

    /**
     * 处理所有 RuntimeException（业务异常）
     * 统一返回格式：{ "success": false, "message": "错误信息" }
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("业务异常: {}", ex.getMessage(), ex);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        log.error("参数校验失败: {}", ex.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        
        // 收集所有校验错误信息
        StringBuilder errorMsg = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errorMsg.append(error.getDefaultMessage()).append("; ")
        );
        response.put("message", errorMsg.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ===================== 统一响应格式 =====================

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

    // ===================== API 接口 =====================

    /**
     * 创建订单
     * POST /api/orders
     * 需要认证：所有登录用户都可以创建订单
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody Order order) {
        log.info("收到创建订单请求: {}", order);
        Order createdOrder = orderService.createOrder(order);
        return successResponse(createdOrder);
    }

    /**
     * 根据 ID 查询订单
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long id) {
        log.info("查询订单: id={}", id);
        Order order = orderService.getOrderById(id);
        return successResponse(order);
    }

    /**
     * 查询所有订单（不分页）
     * GET /api/orders/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        log.info("查询所有订单");
        List<Order> orders = orderService.getAllOrders();
        return successResponse(orders);
    }

    /**
     * 分页查询订单（支持条件筛选）
     * GET /api/orders?page=0&size=10&customerName=张&productName=手机&status=1
     * 
     * 所有筛选参数都是可选的
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrdersByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer status) {
        
        log.info("分页查询订单: page={}, size={}", page, size);
        Page<Order> orderPage = orderService.getOrdersByPage(page, size, customerName, productName, status);
        
        // 构建分页响应
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", orderPage.getContent());           // 当前页数据
        pageData.put("totalElements", orderPage.getTotalElements()); // 总记录数
        pageData.put("totalPages", orderPage.getTotalPages());       // 总页数
        pageData.put("number", orderPage.getNumber());               // 当前页码
        pageData.put("size", orderPage.getSize());                   // 每页大小
        
        return successResponse(pageData);
    }

    /**
     * 更新订单信息
     * PUT /api/orders/{id}
     * 需要认证：所有登录用户都可以更新订单
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody Order order) {
        log.info("更新订单: id={}", id);
        Order updatedOrder = orderService.updateOrder(id, order);
        return successResponse(updatedOrder);
    }

    /**
     * 更新订单状态
     * PATCH /api/orders/{id}/status
     * 需要管理员权限
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        if (status == null) {
            throw new RuntimeException("状态值不能为空");
        }
        log.info("更新订单状态: id={}, newStatus={}", id, status);
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return successResponse(updatedOrder);
    }

    /**
     * 删除订单
     * DELETE /api/orders/{id}
     * 需要管理员权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Long id) {
        log.info("删除订单: id={}", id);
        orderService.deleteOrder(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }
}
