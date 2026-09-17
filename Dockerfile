FROM eclipse-temurin:21-jre-alpine

RUN addgroup --system --gid 1001 appgroup \
    && adduser --system --uid 1001 --ingroup appgroup appuser

RUN apk add --no-cache curl

WORKDIR /app

RUN mkdir -p /data/uploads /data/logs \
    && chown -R appuser:appgroup /app /data

COPY --chown=appuser:appgroup yh-istream-web/target/*.jar app.jar

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