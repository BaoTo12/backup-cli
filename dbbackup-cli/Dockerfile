# Stage 1: Build the application using Maven
FROM maven:3.8.5-openjdk-17 AS build
# Set the working directory
WORKDIR /app
# Copy the Maven project files
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline
# Copy the rest of the source code
COPY src ./src
# Build the application, creating the executable JAR
RUN mvn clean install -DskipTests

# Stage 2: Create the final, lightweight image
FROM eclipse-temurin:17-jre-alpine
# Install PostgreSQL client (Alpine usually has the latest version)
RUN apk add --no-cache postgresql-client
# Set the working directory
WORKDIR /app
# Copy the executable JAR from the build stage
COPY --from=build /app/target/dbbackup-cli-0.0.1-SNAPSHOT.jar ./app.jar
# Expose the port the application runs on (for REST/Actuator endpoints)
EXPOSE 8080
# The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
