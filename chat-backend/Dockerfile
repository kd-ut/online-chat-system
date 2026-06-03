FROM maven:3.9-eclipse-temurin-21 AS base
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

FROM base AS dev
COPY src ./src
EXPOSE 8080
CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=dev"]

FROM base AS build
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:21-jre AS prod
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -sf http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
