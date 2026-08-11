FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY docs ./docs
RUN mvn -B --no-transfer-progress clean verify

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/target/jsearch-gateway-1.0.0.jar app.jar
EXPOSE 8102
ENTRYPOINT ["java", "-jar", "app.jar"]
