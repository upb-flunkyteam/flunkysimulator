package simulator.view

import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import simulator.control.ClientManager

class ClientService(private val clientManager: ClientManager): ClientServiceGrpc.ClientServiceImplBase(){

    override fun clientStream(request: ClientStreamReq, responseObserver: StreamObserver<ClientStreamResp>) {

        val aliveChallenge = {
            var success = false
            try {
                //fails if stream is closed
                responseObserver.onNext(ClientStreamResp.newBuilder()
                    .setAliveChallenge( AliveChallenge.getDefaultInstance())
                    .build())
                success = true
            } catch (e: StatusRuntimeException) {
                if (e.status.code == Status.Code.CANCELLED) {
                    println("A client was poked and found dead. Message: \n ${e.message}")
                } else
                    throw e
            }
            success
        }

        val secret = clientManager.registerClient (aliveChallenge)

        responseObserver.onNext(ClientStreamResp.newBuilder()
            .setClientRegisterd(ClientRegisterd.newBuilder()
                .setSecret(secret)
                .build())
            .build())
    }
}
