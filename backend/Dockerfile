# Build stage
FROM gradle:7.6.1-jdk17-alpine AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Set up health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q -O /dev/null http://localhost:8080/api/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]