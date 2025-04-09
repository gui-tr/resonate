# Use Oracle's official GraalVM image with Java 21
FROM container-registry.oracle.com/graalvm/native-image:21 AS build

# Set working directory
WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src

# Make the Maven wrapper executable
RUN chmod +x ./mvnw

# Build the native executable with increased memory allocation and runtime initialization for SSLConnectionSocketFactory
RUN ./mvnw package -Dnative -DskipTests -Dquarkus.native.container-build=false \
    -Dquarkus.native.native-image-xmx=4g \
    -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:23.1-java21 \
    -Dquarkus.native.additional-build-args=--initialize-at-run-time=org.apache.http.conn.ssl.SSLConnectionSocketFactory

# Create a minimal runtime image
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/

# Copy the native executable
COPY --from=build /app/target/*-runner /work/application

# Configure permissions
RUN chmod 775 /work /work/application

EXPOSE 8080
USER quarkus

# Use the PORT environment variable from Render
CMD ./application -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT:-8080}
