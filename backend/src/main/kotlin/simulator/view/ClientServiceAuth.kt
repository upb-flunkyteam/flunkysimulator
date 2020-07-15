package simulator.view

import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.stub.StreamObserver
import simulator.control.ClientsManager
import simulator.control.MessageController
import simulator.control.PlayerController
import java.util.logging.Logger

class ClientServiceAuth(
    private val clientsManager: ClientsManager,
    private val playerController: PlayerController,
    private val messageController: MessageController
) : ClientServiceAuthGrpc.ClientServiceAuthImplBase() {


    override fun registerPlayer(request: RegisterPlayerReq, responseObserver: StreamObserver<RegisterPlayerResp>) {
        val name = request.playerName

        val client = CLIENT_CTX_KEY.get()

        val loginStatus = clientsManager.registerPlayer(name, client)

        if (loginStatus.status == EnumLoginStatus.LOGIN_STATUS_SUCCESS)
            messageController.sendLogMessage(request.playerName, "hat sich registriert. Willkommen Athlet!")

        responseObserver.onNext(
            RegisterPlayerResp.newBuilder()
                .setStatus(loginStatus.status)
                .setRegisteredName(loginStatus.registeredName)
                .build()
        )
        responseObserver.onCompleted()
    }

    override fun deregisterPlayer(
        request: DeregisterPlayerReq,
        responseObserver: StreamObserver<DeregisterPlayerResp>
    ) {
        val player = playerController.getPlayer(request.playerName)
        val client = CLIENT_CTX_KEY.get()

        if (player == null){
            responseObserver.onError(Throwable("Name not set or unknown"))
            return
        }

        clientsManager.removePlayer(client,player.name)

        responseObserver.onNext(DeregisterPlayerResp.getDefaultInstance())
        responseObserver.onCompleted()
    }

    private val logger = Logger.getLogger(this::class.simpleName)
}