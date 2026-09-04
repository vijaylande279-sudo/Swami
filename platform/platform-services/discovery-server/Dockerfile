FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY platform/pom.xml platform/pom.xml
COPY platform/common platform/common
COPY platform/platform-services/discovery-server platform/platform-services/discovery-server
RUN mvn -f platform/pom.xml -pl platform-services/discovery-server -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/platform/platform-services/discovery-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
