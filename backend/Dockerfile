FROM maven:3.8.8-eclipse-temurin-8 AS builder
WORKDIR /app

# Use wildcards so it finds the file regardless of depth
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the src folder
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:8-jre
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Use a wildcard for the jar to avoid version mismatches
COPY --from=builder /app/target/*.jar currency-api.jar

EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar currency-api.jar"]
