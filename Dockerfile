FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY position-analyser ./position-analyser
COPY bootstrap ./bootstrap

RUN mvn -pl bootstrap -am clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
