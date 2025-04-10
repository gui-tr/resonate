# Stage 1: Build the native executable using Oracle’s GraalVM Native Image with Java 21
FROM container-registry.oracle.com/graalvm/native-image:21 AS build
WORKDIR /app

# Copy project files
COPY pom.xml .
COPY mvnw .
COPY .mvn/ .mvn
COPY src/ src

# Make the Maven wrapper executable
RUN chmod +x ./mvnw

# Build the native executable (local build)
RUN ./mvnw package -Dnative -DskipTests -Dquarkus.native.container-build=true \
    -Dquarkus.native.native-image-xmx=4g \
    -Dquarkus.native.additional-build-args=--initialize-at-run-time=org.apache.http.conn.ssl.SSLConnectionSocketFactory

# Stage 2: Create the minimal runtime image using Quarkus Micro Image
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/

# Copy the native executable from the build stage
COPY --from=build /app/target/*-runner /work/application

# Configure permissions as recommended by the official docs
RUN chmod 775 /work /work/application && \
    chown -R 1001 /work && \
    chmod -R "g+rwX" /work && \
    chown -R 1001:root /work

EXPOSE 8080
USER 1001

# Run the application; the PORT environment variable can override the default port (8080)
CMD ./application -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT:-8080}
