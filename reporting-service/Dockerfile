FROM openjdk:17
LABEL authors="Emir"
EXPOSE 8084
ADD target/reporting-service-0.0.1-SNAPSHOT.jar reporting-service.jar
ADD src/main/resources/fonts /reporting-service/src/main/resources/fonts
ENTRYPOINT ["java", "-jar", "reporting-service.jar"]