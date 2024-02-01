# 检查是否有匹配Gradle 8.2和JDK 17版本的官方基础镜像
# 如果没有直接的匹配，你可能需要选择最接近的版本，或者使用jdk基础镜像手动安装Gradle

# 定义用于构建的阶段名
FROM openjdk:17 AS build

# 安装Gradle
RUN wget https://services.gradle.org/distributions/gradle-8.2-bin.zip -P /tmp
RUN unzip -d /opt/gradle /tmp/gradle-8.2-bin.zip

# 添加Gradle到环境变量中
ENV GRADLE_HOME=/opt/gradle/gradle-8.2
ENV PATH=${GRADLE_HOME}/bin:${PATH}

# 设置工作目录
WORKDIR /home/gradle/project

# 复制Gradle配置文件
COPY build.gradle.kts settings.gradle.kts /home/gradle/project/

# 强制刷新依赖项
RUN gradle --refresh-dependencies

# 复制源码和其他构建所需文件到容器中
COPY src /home/gradle/project/src

# 构建项目，跳过测试
RUN gradle build -x test

# 使用OpenJDK 17创建运行镜像
FROM openjdk:17-slim

# 设置工作目录
WORKDIR /app

# 将构建生成的jar文件从构建阶段拷贝到当前镜像中
COPY --from=build /home/gradle/project/build/libs/*.jar /app/CozeDiscordBridge-build.jar

# 运行jar文件
CMD ["java", "-jar", "/app/CozeDiscordBridge-build.jar"]