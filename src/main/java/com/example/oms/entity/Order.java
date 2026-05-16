package com.example.oms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 
 * 对应数据库表: t_order
 * 使用 JPA 注解映射 ORM 关系
 */
@Data                   // Lombok: 自动生成 getter/setter/toString/equals/hashCode
@NoArgsConstructor      // Lombok: 生成无参构造函数（JPA 要求）
@AllArgsConstructor     // Lombok: 生成全参构造函数
@Entity                 // JPA: 标记为实体类
@Table(name = "t_order") // 指定数据库表名（order 是 SQL 关键字，所以用 t_order）
public class Order {

    /**
     * 主键 ID
     * GenerationType.IDENTITY: 使用数据库自增策略（PostgreSQL 的 SERIAL）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单编号（业务唯一标识）
     * 不允许为空，最大长度 64 字符
     */
    @NotBlank(message = "订单编号不能为空")
    @Column(name = "order_no", nullable = false, unique = true, length = 64)
    private String orderNo;

    /**
     * 客户名称
     */
    @NotBlank(message = "客户名称不能为空")
    @Column(name = "customer_name", nullable = false, length = 128)
    private String customerName;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Column(name = "product_name", nullable = false, length = 256)
    private String productName;

    /**
     * 订单数量
     */
    @NotNull(message = "数量不能为空")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 订单金额
     * DecimalMin: 金额必须 >= 0.01
     */
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * 订单状态
     * 0-待处理, 1-已确认, 2-已发货, 3-已完成, 4-已取消
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 备注信息
     */
    @Column(name = "remark", length = 512)
    private String remark;

    /**
     * 创建时间
     * 插入时自动填充当前时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 每次更新时自动填充当前时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * JPA 回调方法：在实体持久化（INSERT）之前自动调用
     * 用于自动设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA 回调方法：在实体更新（UPDATE）之前自动调用
     * 用于自动更新更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
