FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/webdisk.jar /app/webdisk.jar
COPY sample /app/sample
CMD ["java", "-jar", "/app/webdisk.jar"]
