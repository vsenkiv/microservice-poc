# Step 1: Use an official OpenJDK 21 base image
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/inventory-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m -Dcom.fasterxml.jackson.core.StreamWriteConstraints.maxNestingDepth=2000"

# Step 6: Use JAVA_OPTS when running the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
