FROM maven:3.9-eclipse-temurin-17@sha256:1ed5d1f54416b706707b4f3238f63a20bb06aab27c6d240090a2bb9ad895ed45 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY docs ./docs
RUN mvn -B --no-transfer-progress clean verify

FROM eclipse-temurin:17-jre@sha256:1824944ef1bd572d1ff0952afeb2fec7931d77c972c4fbc4dfcdf89f758fb490

WORKDIR /app
COPY --from=build /app/target/jsearch-gateway-1.0.0.jar app.jar
EXPOSE 8102
ENTRYPOINT ["java", "-jar", "app.jar"]
