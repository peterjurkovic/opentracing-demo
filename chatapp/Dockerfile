FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD build/libs/chatapp.jar chatapp.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/chatapp.jar"]