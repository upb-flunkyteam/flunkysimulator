FROM node:lts-alpine3.11

COPY frontend/nodejs /frontend
COPY protocol /proto

RUN apk update && apk add python3 protoc protobuf-dev curl
RUN curl -OL https://github.com/grpc/grpc-web/releases/download/1.0.3/protoc-gen-grpc-web-1.0.3-linux-x86_64 &&\
    chmod +x protoc-gen-grpc-web-1.0.3-linux-x86_64 && \
    mv protoc-gen-grpc-web-1.0.3-linux-x86_64 /usr/local/bin/protoc-gen-grpc-web
RUN mkdir /frontend/public/js/generated
RUN protoc -I=/proto \
  --js_out=import_style=commonjs:/frontend/public/js/generated\
  --grpc-web_out=mode=grpcwebtext,import_style=commonjs:/frontend/public/js/generated\
  /proto/*.proto

WORKDIR /frontend
RUN npm install && npm run-script build
ENV BACKEND_URL none
ENTRYPOINT npm run-script start
EXPOSE 5000
