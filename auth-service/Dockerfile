FROM maven:3.9-eclipse-temurin-19 AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY pom.xml .
COPY auth-service/pom.xml auth-service/
COPY gym-service/pom.xml gym-service/
COPY workload-service/pom.xml workload-service/
COPY config/ config/
RUN mvn -q -pl auth-service -am dependency:go-offline
COPY auth-service/src auth-service/src
RUN mvn -q -pl auth-service -am clean package -DskipTests
RUN java -Djarmode=tools -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar extract --layers --destination extracted

FROM eclipse-temurin:19-jre
WORKDIR /application
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "auth-service-0.0.1-SNAPSHOT.jar"]