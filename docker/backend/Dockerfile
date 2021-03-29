FROM openjdk:11-jdk-slim-buster as build

COPY backend /backend/src
COPY protocol /backend/protocol
WORKDIR /backend/src
RUN ./gradlew build


FROM azul/zulu-openjdk-alpine:11-jre
COPY --from=build /backend/src/build/libs/serverKotlin-all.jar /opt/backend.jar
WORKDIR /opt
ENTRYPOINT java -jar backend.jar
EXPOSE 11049

