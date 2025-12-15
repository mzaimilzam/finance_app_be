# Build stage
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy gradle files first for dependency caching
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the fat jar
RUN gradle shadowJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/finance-app.jar ./app.jar

# Expose port (Render will use PORT env variable)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/health || exit 1

# Run the application with dynamic PORT support
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
