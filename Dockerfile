FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
RUN mkdir -p /app/logs && chown appuser:appgroup /app/logs
COPY app.jar app.jar
USER appuser
ENTRYPOINT ["java", \
  "-Duser.timezone=Asia/Seoul", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]
