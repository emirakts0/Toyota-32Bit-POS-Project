FROM openjdk:17
LABEL authors="Emir"
EXPOSE 8082
ADD target/sale-service-0.0.1-SNAPSHOT.jar sale-service.jar
ENTRYPOINT ["java", "-jar", "sale-service.jar"]