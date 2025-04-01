# Stage 1: Build stage using GraalVM
FROM ghcr.io/graalvm/jdk-community:21 as build

WORKDIR /app

# Copy all necessary files into container
COPY . /app

# Grant executable permissions to Maven wrapper
RUN chmod +x ./mvnw

# Build the native executable
RUN ./mvnw package -Dnative

# Stage 2: Runtime stage (minimal image)
FROM registry.access.redhat.com/ubi9/ubi-minimal:9.5

WORKDIR /work

RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

# Copy native executable from the build stage
COPY --from=build --chown=1001:root --chmod=0755 /app/target/*-runner /work/application

EXPOSE 8080

USER 1001

ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
