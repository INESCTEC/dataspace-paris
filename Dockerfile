FROM eclipse-temurin:21-jdk-jammy
# Hotspot based on ubuntu 22.04 LTS (jammy)

VOLUME /tmp
ENV PARTICIPANTS_IMPORT_DIRECTORY=/data/participants
# copy only the artifacts we need from the first stage and discard the rest
COPY /target/*.jar /app.jar

CMD ["java", "-jar", "/app.jar"]
