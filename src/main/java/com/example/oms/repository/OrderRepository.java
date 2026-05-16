package com.example.oms.repository;

import com.example.oms.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 订单数据访问层（Repository）
 * 
 * 继承 JpaRepository 后自动获得以下 CRUD 方法：
 *   - save(), findById(), findAll(), deleteById(), count(), existsById() 等
 * 
 * 自定义查询方法命名规则：
 *   findByXxx: 根据字段查询
 *   findByXxxAndYyy: 多条件 AND 查询
 *   findByXxxContaining: 模糊查询（LIKE %xxx%）
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根据订单编号查询订单
     * @param orderNo 订单编号
     * @return 订单对象（Optional 包装，避免空指针）
     */
    Optional<Order> findByOrderNo(String orderNo);

    /**
     * 根据订单状态查询订单列表
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> findByStatus(Integer status);

    /**
     * 根据客户名称模糊查询订单（分页）
     * @param customerName 客户名称（支持模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Order> findByCustomerNameContaining(String customerName, Pageable pageable);

    /**
     * 根据商品名称模糊查询订单（分页）
     * @param productName 商品名称（支持模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Order> findByProductNameContaining(String productName, Pageable pageable);

    /**
     * 多条件动态查询：根据客户名称、商品名称、订单状态组合查询（分页）
     * 使用 @Query 注解编写 JPQL（Java 持久化查询语言）
     * 
     * @param customerName 客户名称（可为空）
     * @param productName  商品名称（可为空）
     * @param status       订单状态（可为空）
     * @param pageable     分页参数
     * @return 分页结果
     */
    @Query("SELECT o FROM Order o WHERE " +
           "(:customerName IS NULL OR o.customerName LIKE %:customerName%) AND " +
           "(:productName IS NULL OR o.productName LIKE %:productName%) AND " +
           "(:status IS NULL OR o.status = :status)")
    Page<Order> findByConditions(
            @Param("customerName") String customerName,
            @Param("productName") String productName,
            @Param("status") Integer status,
            Pageable pageable);
}
