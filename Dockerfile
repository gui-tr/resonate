# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy the project files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src

# Make the Maven wrapper executable
RUN chmod +x ./mvnw

# Build a JVM-mode executable
RUN ./mvnw package -DskipTests

# Runtime stage
FROM registry.access.redhat.com/ubi9/openjdk-21:1.21

# Copy application from build stage
COPY --chown=185 --from=build /app/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 --from=build /app/target/quarkus-app/*.jar /deployments/
COPY --chown=185 --from=build /app/target/quarkus-app/app/ /deployments/app/
COPY --chown=185 --from=build /app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

# Use the PORT environment variable from Render
CMD ["/opt/jboss/container/java/run/run-java.sh", "-Dquarkus.http.port=${PORT:-8080}"]