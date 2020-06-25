package simulator.view

import io.grpc.*
import simulator.control.ClientManager
import java.util.logging.Level
import java.util.logging.Logger


/**
 * A interceptor to handle client secret header.
 */
class ClientAuthenticationInterceptor(private val clientManager: ClientManager) : ServerInterceptor {
    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        requestHeaders: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        logger.log(Level.FINEST,"header received from client:$requestHeaders")

        val secret = requestHeaders.get(CLIENT_SECRET_KEY)

        if (secret == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("no or unknown secret"), requestHeaders)
            return object : ServerCall.Listener<ReqT>() {} //dummy object https://github.com/grpc/grpc-java/issues/3298
        }

        val client = clientManager.getClient(secret)
        if (client == null){
            call.close(Status.UNAUTHENTICATED.withDescription("unknown secret"), requestHeaders)
            return object : ServerCall.Listener<ReqT>() {} //dummy object https://github.com/grpc/grpc-java/issues/3298
        }

        val context = Context.current().withValue(CLIENT_CTX_KEY, client)

        return Contexts.interceptCall(context, call, requestHeaders, next)
    }

    private val logger =
        Logger.getLogger(ClientAuthenticationInterceptor::class.java.name)



}