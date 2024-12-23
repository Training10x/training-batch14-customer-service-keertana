FROM openjdk:17-jdk-slim
EXPOSE 8080
COPY target/customer-service-image.jar customer-service-image.jar
ENTRYPOINT ["java", "-jar", "/customer-service-image.jar"]