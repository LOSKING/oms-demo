-- ==========================================
-- OMS 订单管理系统 - 数据库初始化脚本
-- PostgreSQL
-- ==========================================

-- 如果表已存在则删除（开发环境用，生产环境慎用）
DROP TABLE IF EXISTS t_user_role CASCADE;
DROP TABLE IF EXISTS t_user CASCADE;
DROP TABLE IF EXISTS t_role CASCADE;
DROP TABLE IF EXISTS t_order CASCADE;

-- ==========================================
-- 订单表
-- ==========================================
CREATE TABLE t_order (
    id              BIGSERIAL       PRIMARY KEY,
    order_no        VARCHAR(64)     NOT NULL UNIQUE,
    customer_name   VARCHAR(128)    NOT NULL,
    product_name    VARCHAR(256)    NOT NULL,
    quantity        INTEGER         NOT NULL DEFAULT 1,
    amount          NUMERIC(12,2)   NOT NULL,
    status          INTEGER         NOT NULL DEFAULT 0,
    remark          VARCHAR(512),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       DEFAULT NOW()
);

CREATE INDEX idx_order_status ON t_order(status);
CREATE INDEX idx_order_customer_name ON t_order(customer_name);
CREATE INDEX idx_order_created_at ON t_order(created_at DESC);

-- ==========================================
-- 角色表
-- ==========================================
CREATE TABLE t_role (
    id              BIGSERIAL       PRIMARY KEY,
    role_name       VARCHAR(50)     NOT NULL UNIQUE,
    description     VARCHAR(255)
);

-- ==========================================
-- 用户表
-- ==========================================
CREATE TABLE t_user (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password        VARCHAR(100)    NOT NULL,
    real_name       VARCHAR(100),
    email           VARCHAR(100),
    phone           VARCHAR(20),
    status          INTEGER         NOT NULL DEFAULT 1,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       DEFAULT NOW()
);

CREATE INDEX idx_user_username ON t_user(username);

-- ==========================================
-- 用户-角色关联表（多对多）
-- ==========================================
CREATE TABLE t_user_role (
    user_id         BIGINT          NOT NULL REFERENCES t_user(id) ON DELETE CASCADE,
    role_id         BIGINT          NOT NULL REFERENCES t_role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ==========================================
-- 初始化角色数据
-- ==========================================
INSERT INTO t_role (role_name, description) VALUES
('ROLE_ADMIN', '系统管理员，拥有所有权限'),
('ROLE_USER', '普通用户，可以查看和创建订单');

-- ==========================================
-- 初始化用户数据
-- 密码使用 BCrypt 加密
-- admin123 -> $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
-- user123  -> $2a$10$YcF5tqkH7a9qK3qK3qK3qO8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
-- ==========================================
-- admin123 的 BCrypt 加密（用作管理员）
-- user123 的 BCrypt 加密（用作普通用户）
INSERT INTO t_user (username, password, real_name, email, phone, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'admin@oms.com', '13800000001', 1),
('user', '$2a$10$YcF5tqkH7a9qK3qK3qK3qO8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '普通用户', 'user@oms.com', '13800000002', 1);

-- ==========================================
-- 分配用户角色
-- ==========================================
-- admin 用户 (id=1) -> ROLE_ADMIN + ROLE_USER
-- user 用户 (id=2) -> ROLE_USER
INSERT INTO t_user_role (user_id, role_id) VALUES
(1, 1),  -- admin -> ROLE_ADMIN
(1, 2),  -- admin -> ROLE_USER
(2, 2);  -- user -> ROLE_USER

-- ==========================================
-- 订单测试数据
-- ==========================================
INSERT INTO t_order (order_no, customer_name, product_name, quantity, amount, status, remark) VALUES
('ORD-20260501-001', '张三', 'iPhone 15 Pro', 1, 8999.00, 0, '客户要求尽快发货'),
('ORD-20260501-002', '李四', 'MacBook Pro 14寸', 1, 16999.00, 1, '企业采购'),
('ORD-20260502-003', '王五', 'AirPods Pro 2', 2, 3598.00, 2, '送人用，请包装好'),
('ORD-20260503-004', '赵六', 'iPad Air', 1, 4799.00, 3, NULL),
('ORD-20260504-005', '张三', 'Apple Watch Ultra 2', 1, 5999.00, 4, '客户取消订单'),
('ORD-20260505-006', '孙七', 'Mac Mini M2', 2, 8998.00, 0, NULL),
('ORD-20260506-007', '周八', 'Studio Display', 1, 11499.00, 1, '搭配 Mac Pro 使用'),
('ORD-20260507-008', '吴九', 'Magic Keyboard', 3, 2997.00, 0, NULL),
('ORD-20260508-009', '郑十', 'Pro Display XDR', 1, 39999.00, 2, '专业设计用途'),
('ORD-20260509-010', '张三', 'HomePod mini', 4, 3196.00, 3, NULL);

-- 验证数据
SELECT count(*) AS total_orders FROM t_order;
SELECT count(*) AS total_users FROM t_user;
SELECT count(*) AS total_roles FROM t_role;
