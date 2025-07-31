# Use official Maven image to build the project
FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /build
COPY . .
RUN mvn clean install -DskipTests

# Use minimal JDK image to run the app
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY --from=build /build/target/audiotranscriber-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
