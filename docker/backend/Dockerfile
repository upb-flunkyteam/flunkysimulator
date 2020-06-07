FROM openjdk:11.0.7-jdk-slim-buster

COPY serverKotlin /backend/server
COPY protocol /backend/protocol

WORKDIR /backend/server
ENV VIDEO_LIST_URL banane
ENTRYPOINT ./gradlew run
EXPOSE 11049

