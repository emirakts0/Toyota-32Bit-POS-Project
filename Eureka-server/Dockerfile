FROM openjdk:17
LABEL authors="Emir"
EXPOSE 8761
ADD target/Eureka-server-0.0.1-SNAPSHOT.jar eureka-server.jar
ENV TZ=Europe/Istanbul
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]