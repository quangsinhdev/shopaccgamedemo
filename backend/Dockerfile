FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim AS runtime
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar /app/backend-0.0.1-SNAPSHOT.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "backend-0.0.1-SNAPSHOT.jar"]