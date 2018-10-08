FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD build/libs/messages-api.jar messages-api.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/messages-api.jar"]