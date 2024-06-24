FROM openjdk:17
LABEL authors="Emir"
EXPOSE 8080
ADD target/product-service-0.0.1-SNAPSHOT.jar product-service.jar
ENV TZ=Europe/Istanbul
ENTRYPOINT ["java", "-jar", "product-service.jar"]