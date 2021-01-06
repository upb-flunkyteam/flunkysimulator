package simulator.view

import io.grpc.Context
import io.grpc.Metadata
import simulator.model.Client


val CLIENT_SECRET_KEY =
    Metadata.Key.of("client_secret_key", Metadata.ASCII_STRING_MARSHALLER)

val CLIENT_CTX_KEY = Context.keyWithDefault<Client>("client", null)