# Start with a base image that has Java 17
FROM eclipse-temurin:17-jdk

# Set working directory inside the container
WORKDIR /app

# Copy built JAR from local to container
COPY target/audiotranscriber-0.0.1-SNAPSHOT.jar app.jar

# Command to run the app
CMD ["java", "-jar", "app.jar"]
