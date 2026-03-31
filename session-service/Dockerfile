# Use an official Eclipse Temurin JDK 17 as a parent image
FROM eclipse-temurin:17-jdk-focal

# Set the working directory to /app
WORKDIR /app

# The application's jar file
ARG JAR_FILE=target/*.jar

# Add the application's jar to the container
COPY ${JAR_FILE} app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
