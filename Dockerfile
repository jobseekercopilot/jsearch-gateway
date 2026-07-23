FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/jsearch-gateway-1.0.0.jar app.jar
EXPOSE 8102
ENTRYPOINT ["java", "-jar", "app.jar"]
