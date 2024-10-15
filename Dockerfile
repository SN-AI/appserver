# Use an official Gradle image to build the application
FROM gradle:8.10.2-jdk21 AS build

# Install PostgreSQL client
RUN apt-get update && apt-get install -y postgresql-client

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle build files and source code
COPY build.gradle.kts settings.gradle.kts gradle.properties .env /app/
COPY src /app/src

# Build the application
RUN gradle build --no-daemon

# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21

# Set the working directory in the container
WORKDIR /app

# Copy the application JAR file from the build stage
COPY --from=build /app/build/libs/com.dataserver-all.jar /app/com.dataserver-all.jar
COPY .env /app/.env

# Expose the port the application runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "/app/com.dataserver-all.jar"]