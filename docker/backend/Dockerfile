FROM openjdk:11.0.7-jdk-slim-buster

COPY backend /backend/src
COPY protocol /backend/protocol

WORKDIR /backend/server
ENV VIDEO_LIST_URL none
RUN ./gradlew build
ENTRYPOINT ./gradlew run
EXPOSE 11049

