#!/bin/bash
# ==========================================
# OMS Demo 一键启动脚本
# ==========================================

echo "=========================================="
echo "   OMS 订单管理系统 - 启动脚本"
echo "=========================================="

# 检查 PostgreSQL
echo ""
echo "[1/4] 检查 PostgreSQL..."
if command -v psql &> /dev/null; then
    echo "  ✓ PostgreSQL 已安装"
    
    # 检查是否运行
    if pg_isready &> /dev/null; then
        echo "  ✓ PostgreSQL 正在运行"
    else
        echo "  ✗ PostgreSQL 未运行，正在启动..."
        brew services start postgresql 2>/dev/null || sudo systemctl start postgresql 2>/dev/null
        sleep 3
    fi
else
    echo "  ✗ 未找到 PostgreSQL"
    echo ""
    echo "  请先安装 PostgreSQL:"
    echo "    macOS: brew install postgresql && brew services start postgresql"
    echo "    Ubuntu: sudo apt install postgresql && sudo systemctl start postgresql"
    exit 1
fi

# 创建数据库
echo ""
echo "[2/4] 创建数据库..."
psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'oms_db'" | grep -q 1 || \
psql -U postgres -c "CREATE DATABASE oms_db;"
echo "  ✓ 数据库 oms_db 已就绪"

# 编译项目
echo ""
echo "[3/4] 编译项目..."
mvn clean compile -q
echo "  ✓ 编译完成"

# 启动应用
echo ""
echo "[4/4] 启动应用..."
echo ""
echo "=========================================="
echo "   应用启动中，请稍候..."
echo "   访问地址: http://localhost:8080"
echo "=========================================="
echo ""

mvn spring-boot:run
