FROM node:lts-stretch-slim

COPY client /frontend

WORKDIR /frontend
# RUN npm install && npm run-script build
ENTRYPOINT npm run-script start
EXPOSE 5000
