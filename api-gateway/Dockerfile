FROM openjdk:17
LABEL authors="Emir"
EXPOSE 8889
ADD target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar
ENV TZ=Europe/Istanbul
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]