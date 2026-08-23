FROM maven:3.9-eclipse-temurin-17@sha256:1ed5d1f54416b706707b4f3238f63a20bb06aab27c6d240090a2bb9ad895ed45 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY docs ./docs
RUN mvn -B --no-transfer-progress clean verify

FROM eclipse-temurin:17-jre-alpine@sha256:90b7615cb81e3a75f69124fb480e48981c7d56dbc9f32c614d789d3a1c3e32fe

WORKDIR /app
COPY --from=build /app/target/jsearch-gateway-1.0.0.jar app.jar
EXPOSE 8102
ENTRYPOINT ["java", "-jar", "app.jar"]
