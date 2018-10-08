FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD build/libs/messages-backend.jar messages-backend.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/messages-backend.jar"]