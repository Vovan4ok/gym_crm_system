FROM maven:3.9-eclipse-temurin-19 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
COPY config ./config
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:19-jre
WORKDIR /app
COPY --from=build /app/target/workload-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]