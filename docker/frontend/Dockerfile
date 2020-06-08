FROM node:lts-alpine3.11

COPY client /frontend

WORKDIR /frontend
RUN apk update && apk add python
RUN npm install && npm run-script build
ENV BACKEND_URL none
ENTRYPOINT npm run-script start
EXPOSE 5000
