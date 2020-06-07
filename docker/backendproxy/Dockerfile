# deploy stage
FROM alpine:latest
LABEL maintainer="esplo@users.noreply.github.com"

COPY  grpcwebproxy-v0.12.0-linux-x86_64 .

CMD [ "./grpcwebproxy-v0.12.0-linux-x86_64", \
      "--backend_addr=localhost:11049", \
      "--allow_all_origins",  "--server_http_max_write_timeout=1h", "--run_tls_server=false" \
]


# ./grpcwebproxy-v0.12.0-linux-x86_64 --backend_addr=localhost:11049 --allow_all_origins --server_http_max_write_timeout=1h --run_tls_server=false
# ./grpcwebproxy-v0.12.0-linux-x86_64 --backend_addr=localhost:11049 --allow_all_origins --backend_tls_noverify --server_http_max_write_timeout=1h