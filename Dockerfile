# ---- Build Stage ----
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src
    
# Package the application
RUN mvn clean package -DskipTests

# ---- Run Stage ----
FROM eclipse-temurin:21.0.8_9-jre
WORKDIR /app
    
# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar
    
# Expose application port
EXPOSE 8081
    
# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]