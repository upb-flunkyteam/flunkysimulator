FROM timbru31/java-node:11-jdk

COPY serverKotlin /backend/server
COPY protocol /backend/protocol
COPY grpcwebproxy-v0.12.0-linux-x86_64 /backend

COPY client /frontend

WORKDIR /backend/server
ENV VIDEO_LIST_URL banane
RUN ./gradlew run
WORKDIR /backend
#RUN ./grpcwebproxy-v0.12.0-linux-x86_64 --backend_addr=localhost:11049 --allow_all_origins\
#    --backend_tls_noverify --server_http_max_write_timeout=1h &
EXPOSE 8443

#WORKDIR /frontend
#RUN node build
#RUN node start
#EXPOSE 443
