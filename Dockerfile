# Build stage - using Maven with Java 21
FROM eclipse-temurin:21-jdk AS build

WORKDIR /project

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .
# Copy Maven wrapper files if they exist
COPY .mvn/ ./.mvn/ 2>/dev/null || :
COPY mvnw mvnw.cmd ./ 2>/dev/null || :

# Make the Maven wrapper executable if it exists
RUN if [ -f mvnw ]; then chmod +x mvnw; fi

# Download dependencies
RUN mvn dependency:go-offline -B || echo "Dependencies may not be fully resolved"

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Runtime stage - using UBI9 with OpenJDK 21
FROM registry.access.redhat.com/ubi9/openjdk-21:1.19

ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --from=build --chown=185 /project/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /project/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /project/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /project/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
CMD ["sh", "-c", "export JAVA_OPTS_APPEND=\"$JAVA_OPTS_APPEND -Dquarkus.http.port=${PORT:-8080}\" && /opt/jboss/container/java/run/run-java.sh"]
