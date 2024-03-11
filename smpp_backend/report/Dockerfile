# Use the official OpenJDK 17 base image
FROM openjdk:17

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/REPORT_SERVICE.jar /app/REPORT_SERVICE.jar

# Expose the port the application runs on
EXPOSE 8094

# Specify the default command to run when the container starts
ENTRYPOINT ["java", "-jar", "REPORT_SERVICE.jar"]
