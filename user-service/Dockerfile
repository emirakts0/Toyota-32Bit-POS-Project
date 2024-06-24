FROM openjdk:17
LABEL authors="Emir"
EXPOSE 8081
ADD target/user-service-0.0.1-SNAPSHOT.jar user-service.jar
ENV TZ=Europe/Istanbul
ENTRYPOINT ["java", "-jar", "user-service.jar"]