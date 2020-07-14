package simulator.view

import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import simulator.buildRegisterHandler
import simulator.control.ClientsManager
import java.util.logging.Logger

class ClientServiceUnAuth(private val clientsManager: ClientsManager): ClientServiceGrpc.ClientServiceImplBase(){

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
                    logger.info("A client was poked and found dead. Message: \n ${e.message}")
                } else
                    throw e
            }
            success
        }

        val client = clientsManager.registerClient(aliveChallenge)

        val handler = buildRegisterHandler<ClientsManager.ClientEvent> { clientEvent: ClientsManager.ClientEvent ->
            when (clientEvent){
                is ClientsManager.ClientEvent.OwnedPlayersUpdate ->
                    responseObserver.onNext(ClientStreamResp.newBuilder()
                        .setOwnedPlayersUpdated(OwnedPlayersUpdate.newBuilder()
                            .addAllPlayers(clientEvent.players)
                            .build())
                        .build())
            }
        }

        client.addEventHandler(handler)

        responseObserver.onNext(ClientStreamResp.newBuilder()
            .setClientRegistered(ClientRegistered.newBuilder()
                .setSecret(client.secret)
                .build())
            .build())
    }
    private val logger = Logger.getLogger(this::class.simpleName)
}


