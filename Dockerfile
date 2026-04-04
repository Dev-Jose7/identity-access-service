# Foundation placeholder for IAM service containerization.
# Runtime image/version can be adjusted in later slices without changing service semantics.
FROM eclipse-temurin:21-jre

WORKDIR /app

# Placeholder copy target for future build artifacts.
# Do not embed secrets in image layers.
COPY build/libs/*.jar app.jar

EXPOSE 8080

# Logs must be emitted to stdout/stderr.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
