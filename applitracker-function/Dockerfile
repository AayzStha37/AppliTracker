
FROM eclipse-temurin:17-jre-jammy

# Set the working directory inside the container to /app
WORKDIR /app

COPY target/applitracker-function-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]