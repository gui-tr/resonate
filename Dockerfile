# Stage 1: Build the native executable using the Quarkus native image builder (Java 21)
FROM quay.io/quarkus/ubi-quarkus-native-image:23.0-java21 AS build
WORKDIR /project

# Copy Maven configuration and wrapper to leverage Docker caching
COPY pom.xml .
COPY mvnw .
COPY .mvn/ .mvn/

# Copy application source code
COPY src/ src/

# Ensure the Maven wrapper is executable, then build the native executable in container mode
RUN chmod +x ./mvnw && \
    echo "Starting native build with container build mode enabled (Java 21)..." && \
    ./mvnw package -Dnative -DskipTests -Dquarkus.native.container-build=true \
        -Dquarkus.native.native-image-xmx=4g \
        -Dquarkus.native.additional-build-args="--initialize-at-run-time=org.apache.http.conn.ssl.SSLConnectionSocketFactory" && \
    echo "Native build completed successfully!"

# Stage 2: Create the runtime image
FROM registry.access.redhat.com/ubi8/ubi-minimal AS run
WORKDIR /work/

# Copy the native executable from the build stage (adjust pattern if needed)
COPY --from=build /project/target/*-runner /work/application

# Ensure the application is executable
RUN chmod +x /work/application

# Expose the port used by your application (modify as required)
EXPOSE 8080

# Run the native executable when the container starts
CMD ["./application"]
