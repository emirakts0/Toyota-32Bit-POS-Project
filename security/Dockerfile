FROM openjdk:17
LABEL authors="Emir"
EXPOSE 8086
ADD target/security-0.0.1-SNAPSHOT.jar security.jar
ENTRYPOINT ["java", "-jar", "security.jar"]