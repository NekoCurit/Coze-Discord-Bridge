# JDK版本
FROM openjdk:17-oracle

# 全局版本号
ARG VERSION=1.0.0223

# DOCKERFILE信息
LABEL maintainer="catxfeitu@gmail.com"
LABEL version="${VERSION}"
LABEL description="Coze-Discord-Bridge"

# 暴露端口配置
EXPOSE 8092 8093

# 持久化存储配置
RUN mkdir /data
WORKDIR /data

# Release
RUN mkdir /app
RUN curl -L "https://github.com/catx-feitu/Coze-Discord-Bridge/releases/download/${VERSION}/CozeDiscordBridge-${VERSION}-all.jar" -o "/app/CozeDiscordBridge.jar"

# 容器启动时运行JAR文件
# ENTRYPOINT中的JAR文件路径应该从工作目录启动
ENTRYPOINT ["java", "-server", "-jar", "/app/CozeDiscordBridge.jar"]