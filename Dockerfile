# Use GraalVM with Java 21
FROM ghcr.io/graalvm/graalvm-community:21 AS build

# Install native-image component
RUN gu install native-image

# Set working directory
WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src

# Make the Maven wrapper executable
RUN chmod +x ./mvnw

# Build the native executable directly with GraalVM
RUN ./mvnw package -Dnative -DskipTests -Dquarkus.native.container-build=false

# Create a minimal runtime image
FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/

# Copy the native executable
COPY --from=build /app/target/*-runner /work/application

# Configure permissions
RUN chmod 775 /work /work/application

EXPOSE 8080
USER quarkus

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
