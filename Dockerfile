# Use Eclipse Temurin's Java 21 JDK image as base
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for better layer caching)
COPY mvnw ./
COPY .mvn ./.mvn
COPY pom.xml ./

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests to save time and resources)
RUN ./mvnw package -DskipTests

# Use a smaller runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built application from the previous stage
COPY --from=0 /app/target/quarkus-app/lib/ /app/lib/
COPY --from=0 /app/target/quarkus-app/*.jar /app/
COPY --from=0 /app/target/quarkus-app/app/ /app/app/
COPY --from=0 /app/target/quarkus-app/quarkus/ /app/quarkus/

# Create a non-root user
RUN adduser -D appuser
USER appuser

EXPOSE 8080

CMD ["sh", "-c", "java -Dquarkus.http.port=${PORT:-8080} -jar quarkus-run.jar"]
