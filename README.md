# OMS 订单管理系统 Demo

基于 Spring Boot + PostgreSQL 的订单管理系统示例项目。

## 技术栈

- **后端**: Java 17 + Spring Boot 3.2.5
- **数据库**: PostgreSQL
- **ORM**: Spring Data JPA + Hibernate
- **前端**: HTML + CSS + Vanilla JavaScript
- **构建工具**: Maven

## 项目结构

```
oms-demo/
├── pom.xml                                          # Maven 配置
└── src/
    └── main/
        ├── java/com/example/oms/
        │   ├── OmsApplication.java                  # 启动类
        │   ├── config/
        │   │   └── WebConfig.java                   # Web 配置（跨域等）
        │   ├── controller/
        │   │   └── OrderController.java             # REST API 控制器
        │   ├── entity/
        │   │   └── Order.java                       # 订单实体类
        │   ├── repository/
        │   │   └── OrderRepository.java             # 数据访问层
        │   └── service/
        │       └── OrderService.java                # 业务逻辑层
        └── resources/
            ├── application.yml                      # 应用配置
            ├── schema.sql                           # 数据库初始化脚本
            └── static/
                └── index.html                       # 前端页面
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL 12+

### 2. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE oms_db;

-- 创建用户（可选，如果需要单独的用户）
-- CREATE USER oms_user WITH PASSWORD 'oms_password';
-- GRANT ALL PRIVILEGES ON DATABASE oms_db TO oms_user;
```

### 3. 修改数据库配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/oms_db
    username: postgres          # 改为你的用户名
    password: postgres          # 改为你的密码
```

### 4. 启动应用

```bash
# 编译并启动
mvn spring-boot:run

# 或者先打包再运行
mvn clean package
java -jar target/oms-demo-1.0.0.jar
```

### 5. 访问应用

浏览器打开: http://localhost:8080

## API 接口文档

### 订单 CRUD

| 方法   | 路径                      | 说明           |
|--------|---------------------------|----------------|
| POST   | `/api/orders`             | 创建订单       |
| GET    | `/api/orders`             | 分页查询订单   |
| GET    | `/api/orders/all`         | 查询所有订单   |
| GET    | `/api/orders/{id}`        | 根据 ID 查询   |
| PUT    | `/api/orders/{id}`        | 更新订单       |
| PATCH  | `/api/orders/{id}/status` | 更新订单状态   |
| DELETE | `/api/orders/{id}`        | 删除订单       |

### 请求示例

**创建订单**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD-20260516-001",
    "customerName": "张三",
    "productName": "iPhone 15",
    "quantity": 1,
    "amount": 5999.00,
    "remark": "请尽快发货"
  }'
```

**分页查询**
```bash
curl "http://localhost:8080/api/orders?page=0&size=10&customerName=张&status=0"
```

**更新状态**
```bash
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": 1}'
```

## 订单状态说明

| 状态值 | 说明   |
|--------|--------|
| 0      | 待处理 |
| 1      | 已确认 |
| 2      | 已发货 |
| 3      | 已完成 |
| 4      | 已取消 |

## 调试指南

### 1. 查看后端日志

启动后控制台会打印详细日志，包括：
- SQL 语句（已格式化）
- SQL 参数值
- 业务操作日志（DEBUG 级别）

### 2. 前端调试

- 打开浏览器开发者工具（F12）
- Console 面板：查看 JavaScript 日志（带 `[OMS]` 前缀）
- Network 面板：查看 API 请求和响应
- 页面底部有 **API 调试日志** 面板，可展开查看所有 API 调用

### 3. 数据库调试

```sql
-- 查看所有订单
SELECT * FROM t_order ORDER BY created_at DESC;

-- 查看各状态订单数量
SELECT status, count(*) FROM t_order GROUP BY status;

-- 查看某客户的订单
SELECT * FROM t_order WHERE customer_name LIKE '%张三%';
```

### 4. 常见问题

**Q: 启动时报数据库连接错误？**
A: 检查 PostgreSQL 是否运行，以及 `application.yml` 中的连接信息是否正确。

**Q: 页面显示但数据加载失败？**
A: 检查浏览器 Console 是否有 CORS 错误，确保后端正常启动。

**Q: 如何重置数据库？**
A: 删除 `oms_db` 数据库后重新创建，应用启动时会自动执行 `schema.sql`。

## 扩展建议

- 添加用户认证（Spring Security）
- 添加订单导出功能（Excel/PDF）
- 添加消息队列处理异步任务
- 添加 Redis 缓存热点数据
- 前端改用 Vue.js/React 框架
