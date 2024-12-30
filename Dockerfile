FROM openjdk:17-jdk-slim
COPY target/customer-service-image.jar customer-service-image.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "customer-service-image.jar"]
