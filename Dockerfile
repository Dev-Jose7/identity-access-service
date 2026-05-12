FROM eclipse-temurin:21-jre

WORKDIR /app

# Do not embed secrets in image layers.
COPY build/libs/identity-access-service.jar app.jar

EXPOSE 8080

# Logs must be emitted to stdout/stderr.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
