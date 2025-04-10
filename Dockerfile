# Stage 1: Build native binary with Mandrel (Java 21)
FROM quay.io/quarkus/ubi9-quarkus-mandrel-builder-image:23.1.6.0-Final-java21 AS build
WORKDIR /project
COPY pom.xml . 
COPY src/ ./src/
# Install AWS and Apache extensions in Maven (if not already in pom.xml)
# RUN ./mvnw quarkus:add-extension -Dextensions=\"io.quarkiverse.amazonservices:quarkus-amazon-services-bom:3.3.0,io.quarkiverse.amazonservices:quarkus-amazon-s3,io.quarkus:quarkus-apache-httpclient\"
RUN ./mvnw package -Pnative -DskipTests \
    -Dquarkus.native.native-image-xmx=6g \
    -Dquarkus.native.additional-build-args=\"--initialize-at-run-time=org.apache.http.impl.auth.NTLMEngineImpl,org.apache.http.conn.ssl.SSLConnectionSocketFactory\"

# Stage 2: Create minimal runtime image
FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
WORKDIR /work/
COPY --from=build /project/target/*-runner /work/application
# Set permissions for non-root execution
RUN chown 1001 /work && chmod g+rwX /work && chown 1001:root /work
EXPOSE 8080
USER 1001
CMD ["./application"]
