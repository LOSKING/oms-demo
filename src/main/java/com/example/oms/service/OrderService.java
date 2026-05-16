package com.example.oms.service;

import com.example.oms.entity.Order;
import com.example.oms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 订单业务逻辑层（Service）
 * 
 * @Slf4j: Lombok 注解，自动注入日志对象（log.info, log.error 等）
 * @Service: 标记为 Spring 服务层组件
 * @RequiredArgsConstructor: 自动生成包含 final 字段的构造函数（用于依赖注入）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    /**
     * 注入 OrderRepository
     * 使用 final + @RequiredArgsConstructor 实现构造器注入（推荐方式）
     */
    private final OrderRepository orderRepository;

    /**
     * 创建订单
     * 
     * @Transactional: 声明式事务，方法执行失败时自动回滚
     * 
     * @param order 订单对象
     * @return 创建后的订单（包含自动生成的 ID 和时间）
     */
    @Transactional
    public Order createOrder(Order order) {
        log.info("创建订单: orderNo={}, customerName={}", order.getOrderNo(), order.getCustomerName());
        
        // 检查订单编号是否已存在
        if (orderRepository.findByOrderNo(order.getOrderNo()).isPresent()) {
            throw new RuntimeException("订单编号已存在: " + order.getOrderNo());
        }
        
        // 保存订单到数据库
        Order savedOrder = orderRepository.save(order);
        log.info("订单创建成功: id={}", savedOrder.getId());
        return savedOrder;
    }

    /**
     * 根据 ID 查询订单
     * 
     * @param id 订单主键 ID
     * @return 订单对象
     * @throws RuntimeException 如果订单不存在
     */
    @Transactional(readOnly = true)  // 只读事务，性能优化
    public Order getOrderById(Long id) {
        log.debug("根据 ID 查询订单: id={}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在: id=" + id));
    }

    /**
     * 根据订单编号查询订单
     * 
     * @param orderNo 订单编号
     * @return 订单对象
     * @throws RuntimeException 如果订单不存在
     */
    @Transactional(readOnly = true)
    public Order getOrderByOrderNo(String orderNo) {
        log.debug("根据订单编号查询订单: orderNo={}", orderNo);
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在: orderNo=" + orderNo));
    }

    /**
     * 查询所有订单（不分页）
     * 
     * @return 订单列表
     */
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        log.debug("查询所有订单");
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * 分页查询订单（支持条件筛选）
     * 
     * @param page         页码（从 0 开始）
     * @param size         每页大小
     * @param customerName 客户名称（模糊查询，可为空）
     * @param productName  商品名称（模糊查询，可为空）
     * @param status       订单状态（可为空）
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByPage(int page, int size, String customerName, 
                                        String productName, Integer status) {
        log.debug("分页查询订单: page={}, size={}, customerName={}, productName={}, status={}", 
                  page, size, customerName, productName, status);
        
        // 构建分页请求：按创建时间倒序排列
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 如果没有任何筛选条件，查询全部
        if (customerName == null && productName == null && status == null) {
            return orderRepository.findAll(pageRequest);
        }
        
        // 有条件时执行条件查询
        return orderRepository.findByConditions(customerName, productName, status, pageRequest);
    }

    /**
     * 更新订单信息
     * 
     * @param id    订单 ID
     * @param order 更新的订单数据
     * @return 更新后的订单
     */
    @Transactional
    public Order updateOrder(Long id, Order order) {
        log.info("更新订单: id={}", id);
        
        // 先查询订单是否存在
        Order existingOrder = getOrderById(id);
        
        // 只更新非空字段（部分更新）
        if (order.getCustomerName() != null) {
            existingOrder.setCustomerName(order.getCustomerName());
        }
        if (order.getProductName() != null) {
            existingOrder.setProductName(order.getProductName());
        }
        if (order.getQuantity() != null) {
            existingOrder.setQuantity(order.getQuantity());
        }
        if (order.getAmount() != null) {
            existingOrder.setAmount(order.getAmount());
        }
        if (order.getRemark() != null) {
            existingOrder.setRemark(order.getRemark());
        }
        // 注意：orderNo 和 status 不允许通过此方法修改
        
        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("订单更新成功: id={}", updatedOrder.getId());
        return updatedOrder;
    }

    /**
     * 更新订单状态
     * 
     * @param id     订单 ID
     * @param status 新状态
     * @return 更新后的订单
     */
    @Transactional
    public Order updateOrderStatus(Long id, Integer status) {
        log.info("更新订单状态: id={}, newStatus={}", id, status);
        
        Order order = getOrderById(id);
        order.setStatus(status);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("订单状态更新成功: id={}, status={}", updatedOrder.getId(), updatedOrder.getStatus());
        return updatedOrder;
    }

    /**
     * 删除订单
     * 
     * @param id 订单 ID
     */
    @Transactional
    public void deleteOrder(Long id) {
        log.info("删除订单: id={}", id);
        
        // 先检查订单是否存在
        getOrderById(id);
        
        orderRepository.deleteById(id);
        log.info("订单删除成功: id={}", id);
    }
}
