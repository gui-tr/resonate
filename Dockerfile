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

# Build a JVM-mode executable first to verify everything compiles
RUN ./mvnw package -DskipTests

# Create a minimal JVM-mode runtime image as a fallback
FROM registry.access.redhat.com/ubi9/openjdk-21:1.21 AS jvm-build
COPY --from=build /app/target/quarkus-app /deployments
EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]