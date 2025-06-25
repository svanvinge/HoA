# Use a slim Java 17 OpenJDK image
FROM eclipse-temurin:17-jre-focal

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the Maven target directory
# The JAR will be built in the hoa-service/target/ directory by Maven
COPY target/service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port on which the Spring Boot application will run
EXPOSE 8080

# Command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
