# Construction phase (build)
# We only copy necesary config files to build 
# dependency:go-offline -B --> cache the dependencies
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Execution phase (runtime)
# We copy only the source files
# Expose MUST match the port from our server
# TODO need to manage version automatically
COPY src ./src
RUN mvn clean package
FROM openjdk:21-jdk AS runner
WORKDIR /app
COPY --from=builder ./app/target/scraper-service-0.0.1-SNAPSHOT.jar ./app.jar
EXPOSE 4015

ENTRYPOINT ["java", "-jar", "app.jar"]

# EXPOSE 8080
# EXPOSE 5005
# ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]
