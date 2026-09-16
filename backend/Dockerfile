# syntax=docker/dockerfile:1
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q -DskipTests package && cp target/rescuesync-backend-*.jar app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 rescuesync
COPY --from=build /app/app.jar app.jar
USER rescuesync
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
