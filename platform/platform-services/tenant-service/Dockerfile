FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY platform platform
RUN mvn -f platform/pom.xml -pl platform-services/tenant-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/platform/platform-services/tenant-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
