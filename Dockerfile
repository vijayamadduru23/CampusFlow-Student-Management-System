FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

ENV PORT=8080
EXPOSE 8080

COPY --from=build --chown=10001:0 /workspace/target/demo-1.0-SNAPSHOT.jar /app/app.jar

USER 10001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
