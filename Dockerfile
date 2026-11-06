# ============================================================
# 构建阶段：镜像内从源码编译
# ============================================================
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# 保证镜像与代码版本一致
COPY . .

RUN mvn -B -ntp -DskipTests package

# ============================================================
# 运行阶段：仅保留 JRE 与可执行 jar
# ============================================================
FROM eclipse-temurin:25-jre-alpine

RUN addgroup --system --gid 1001 appgroup \
    && adduser --system --uid 1001 --ingroup appgroup appuser

RUN apk add --no-cache curl

WORKDIR /app

RUN mkdir -p /data/uploads /data/logs \
    && chown -R appuser:appgroup /app /data

COPY --from=builder --chown=appuser:appgroup /build/yh-istream-server/target/yh-istream-server-*.jar app.jar

RUN chmod 500 app.jar

USER appuser:appgroup

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Duser.timezone=Asia/Shanghai", \
    "-cp", "app.jar", \
    "org.springframework.boot.loader.launch.JarLauncher", \
    "--spring.profiles.active=prod"]