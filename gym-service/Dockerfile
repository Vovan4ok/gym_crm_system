FROM maven:3.9-eclipse-temurin-19 AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY pom.xml .
COPY auth-service/pom.xml auth-service/
COPY gym-service/pom.xml gym-service/
COPY workload-service/pom.xml workload-service/
COPY gateway-service/pom.xml gateway-service/
COPY config/ config/
RUN mvn -q -pl gym-service -am dependency:go-offline
COPY gym-service/src gym-service/src
RUN mvn -q -pl gym-service -am clean package -DskipTests
RUN java -Djarmode=tools -jar gym-service/target/gym-service-0.0.1-SNAPSHOT.jar extract --layers --destination extracted

FROM eclipse-temurin:19-jre
WORKDIR /application
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "gym-service-0.0.1-SNAPSHOT.jar"]