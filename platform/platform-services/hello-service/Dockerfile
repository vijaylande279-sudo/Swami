FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY platform/pom.xml platform/pom.xml
COPY platform/common platform/common
COPY platform/platform-services/hello-service platform/platform-services/hello-service
RUN mvn -f platform/pom.xml -pl platform-services/hello-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/platform/platform-services/hello-service/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
