# Build on JDK 21: the temurin-19 Maven images are frozen at Maven 3.9.0, too old for the
# protobuf plugin. The build still targets Java 19 (maven.compiler.release), so it runs on the 19 JRE below.
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY pom.xml .
COPY auth-service/pom.xml auth-service/
COPY gym-service/pom.xml gym-service/
COPY workload-service/pom.xml workload-service/
COPY gateway-service/pom.xml gateway-service/
COPY integration-tests/pom.xml integration-tests/
COPY grpc-contract/pom.xml grpc-contract/
COPY config/ config/
RUN mvn -q -pl workload-service -am dependency:go-offline
COPY grpc-contract/src grpc-contract/src
COPY workload-service/src workload-service/src
RUN mvn -q -pl workload-service -am clean package -DskipTests
RUN java -Djarmode=tools -jar workload-service/target/workload-service-0.0.1-SNAPSHOT.jar extract --layers --destination extracted

FROM eclipse-temurin:19-jre
WORKDIR /application
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "workload-service-0.0.1-SNAPSHOT.jar"]