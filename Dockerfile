# Stage 1: Build the native executable using Java 21
FROM quay.io/quarkus/ubi-quarkus-native-image:23.1-java21 AS build
WORKDIR /project
COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn
# Cache the dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src
# Build the application
RUN ./mvnw package -Pnative -Dquarkus.native.container-build=true

# Stage 2: Create a minimal runtime image for native executable
FROM registry.access.redhat.com/ubi8/ubi-minimal:latest
WORKDIR /application
COPY --from=build /project/target/*-runner /application/application
# Create a non-root user
RUN chmod 110 /application/application && \
    chown 1001 /application/application
USER 1001
EXPOSE 8080

CMD ["/bin/sh", "-c", "/application/application -Dquarkus.http.port=${PORT:-8080}"]