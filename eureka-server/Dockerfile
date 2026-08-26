FROM maven:3.9-eclipse-temurin-19 AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY auth-service/pom.xml auth-service/
COPY gym-service/pom.xml gym-service/
COPY workload-service/pom.xml workload-service/
COPY config/ config/
RUN mvn -q -pl eureka-server -am dependency:go-offline
COPY eureka-server/src eureka-server/src
RUN mvn -q -pl eureka-server -am clean package -DskipTests
RUN java -Djarmode=tools -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar extract --layers --destination extracted

FROM eclipse-temurin:19-jre
WORKDIR /application
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "eureka-server-0.0.1-SNAPSHOT.jar"]