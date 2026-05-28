FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jar-alpine
COPY --from=builder /app/target/*.jar healthcare-app.jar

EXPOSE 8081

CMD ["java", "-jar", "healthcare-app.jar"]
