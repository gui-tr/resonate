# Use GraalVM as base image
FROM ghcr.io/graalvm/graalvm-community:17 AS build

# Install native-image component
RUN gu install native-image

# Set working directory
WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src

# Build the native executable directly with GraalVM
RUN ./mvnw package -Dnative -DskipTests -Dquarkus.native.container-build=false

# Create a minimal runtime image
FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
WORKDIR /work/

# Copy the native executable
COPY --chmod=0755 target/*-runner /work/application

EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
