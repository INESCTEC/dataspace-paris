FROM eclipse-temurin:25-jdk-noble
# Hotspot based on ubuntu 24.04 LTS (noble)

VOLUME /tmp
ENV PARTICIPANTS_IMPORT_DIRECTORY=/data/participants
# copy only the artifacts we need from the first stage and discard the rest
COPY /target/*.jar /app.jar

CMD ["java", "-jar", "/app.jar"]
