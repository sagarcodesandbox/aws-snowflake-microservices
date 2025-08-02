# Multi-stage build for Snowflake Product Service
# Stage 1: Build stage
FROM amazoncorretto:24-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradle/ ./gradle/
COPY gradlew ./
COPY build.gradle ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src/ ./src/

# Build the application and run the unpack task
RUN ./gradlew clean build unpack --no-daemon

# Stage 2: Runtime stage
FROM amazoncorretto:24-alpine AS runtime

# Install additional packages for better container support
RUN apk add --no-cache \
    dumb-init \
    && addgroup -g 1001 -S appgroup \
    && adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the unpacked application from build stage
COPY --from=builder /app/build/libs/ ./

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose the port the app runs on
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Use dumb-init to handle signals properly
ENTRYPOINT ["/usr/bin/dumb-init", "--"]

# Run the application using the unpacked structure
CMD ["java", "-cp", "BOOT-INF/classes:BOOT-INF/lib/*", "com.snowflake.snowflakeproductservice.SnowflakeProductServiceApplication"]
