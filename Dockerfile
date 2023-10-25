FROM openjdk:21

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Add the application's jar to the container
ADD build/libs/*-SNAPSHOT.jar app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
