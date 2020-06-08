package simulator.view

import io.grpc.*
import java.util.logging.Logger


/**
 * A interceptor to handle server header.
 */
class ClientAuthenticationInterceptor : ServerInterceptor {
    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        requestHeaders: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        logger.info("header received from client:$requestHeaders")

        call.close(Status.UNAUTHENTICATED.withDescription("no or unknown secret"),requestHeaders)

        //TODO get client and put in context

        return next.startCall(call,requestHeaders)
    }

    private val logger =
        Logger.getLogger(ClientAuthenticationInterceptor::class.java.name)



}