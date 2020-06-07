FROM node:lts-alpine3.11

COPY . /frontend

WORKDIR /frontend
RUN apk update && apk add python
RUN npm install && npm run-script build
ENTRYPOINT npm run-script start
EXPOSE 5000
