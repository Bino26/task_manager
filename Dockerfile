FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ======== Stage : Run========
FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar
COPY pom.xml .
COPY src ./src

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
