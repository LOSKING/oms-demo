package com.example.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OMS（订单管理系统）启动类
 * 
 * @SpringBootApplication 注解组合了三个注解：
 *   - @Configuration: 标记为配置类
 *   - @EnableAutoConfiguration: 启用 Spring Boot 自动配置
 *   - @ComponentScan: 自动扫描当前包及子包下的组件
 */
@SpringBootApplication
public class OmsApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(OmsApplication.class, args);
        System.out.println("===========================================");
        System.out.println("   OMS 订单管理系统启动成功！");
        System.out.println("   访问地址: http://localhost:8080");
        System.out.println("===========================================");
    }
}
