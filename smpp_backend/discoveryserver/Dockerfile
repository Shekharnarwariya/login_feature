# Use the official OpenJDK 17 base image
FROM openjdk:17

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/DISCOVERY_SERVER.jar /app/DISCOVERY_SERVER.jar

# Expose the port the application runs on
EXPOSE 8082

# Specify the default command to run when the container starts
ENTRYPOINT ["java", "-jar", "DISCOVERY_SERVER.jar"]
