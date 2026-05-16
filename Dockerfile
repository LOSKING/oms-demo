FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 复制 Maven 构建的 jar 包
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
