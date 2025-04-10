# Stage 1: Build the application using Maven with Java 21
FROM maven:3.9.0-eclipse-temurin-21 AS build
WORKDIR /build

# Copy the Maven configuration first to leverage caching
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw

# Download Maven dependencies
RUN ./mvnw dependency:resolve

# Copy source files and build the application (skip tests)
COPY src/ src/
RUN ./mvnw package -DskipTests

# Stage 2: Create the runtime image using a minimal Java 21 runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR (adjust the pattern if your artifact name differs)
COPY --from=build /build/target/*-runner.jar app.jar

# Expose the application port (change if necessary)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
