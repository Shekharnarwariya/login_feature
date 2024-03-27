# Use the official OpenJDK 17 base image
FROM openjdk:17

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/TWOWAY_SERVICE.jar /app/TWOWAY_SERVICE.jar

# Expose the port the application runs on
EXPOSE 8090

# Specify the default command to run when the container starts
ENTRYPOINT ["java", "-jar", "TWOWAY_SERVICE.jar"]
