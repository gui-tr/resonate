# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy the project files
COPY . .

# Make the Maven wrapper executable
RUN chmod +x ./mvnw

# Build the application in JVM mode
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

# Set Java options
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

# Start the application
ENTRYPOINT ["/opt/jboss/container/java/run/run-java.sh"]
