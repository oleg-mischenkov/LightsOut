# Stage 1
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package

# Stage 2
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/LightsOut-1.0-SNAPSHOT.jar lightsout
COPY --from=build /app/src/main/resources/ .

CMD ["/bin/bash"]
