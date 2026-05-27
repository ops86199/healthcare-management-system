FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/*.jar healthcare-app.jar

EXPOSE 8080

CMD ["java", "-jar", "healthcare-app.jar"]
