FROM alpine:latest

COPY  grpcwebproxy-v0.12.0-linux-x86_64 .
ENTRYPOINT ["./grpcwebproxy-v0.12.0-linux-x86_64", "--allow_all_origins", "--server_http_max_write_timeout=1h", "--run_tls_server=false"]
CMD ["--backend_addr=localhost:11049"]