# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the application's JAR file into the container
COPY target/fambud-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that the application will run on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
