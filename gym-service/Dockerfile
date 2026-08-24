FROM maven:3.9-eclipse-temurin-19 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
COPY config ./config
RUN mvn -q clean package -DskipTests
RUN java -Djarmode=tools -jar target/gym-service-0.0.1-SNAPSHOT.jar extract --layers --destination extracted

FROM eclipse-temurin:19-jre
WORKDIR /application
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "gym-service-0.0.1-SNAPSHOT.jar"]