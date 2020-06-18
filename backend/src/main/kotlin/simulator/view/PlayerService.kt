package simulator.view

import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.stub.StreamObserver
import simulator.control.MessageController
import simulator.control.PlayerController
import simulator.model.game.positionalName

class PlayerService(
    private val playerController: PlayerController,
    private val messageController: MessageController
) : PlayerServiceGrpc.PlayerServiceImplBase() {


    override fun registerPlayer(request: RegisterPlayerReq, responseObserver: StreamObserver<RegisterPlayerResp>) {
        val name = request.playerName

        val loginStatus = playerController.registerPlayer(name)

        if (loginStatus.status == EnumLoginStatus.LOGIN_STATUS_SUCCESS)
            messageController.sendMessage(request.playerName, "hat sich registriert. Willkommen Athlet!")

        responseObserver.onNext(
            RegisterPlayerResp.newBuilder()
                .setStatus(loginStatus.status)
                .setRegisteredName(loginStatus.registeredName)
                .build()
        )
        responseObserver.onCompleted()
    }

    override fun kickPlayer(request: KickPlayerReq?, responseObserver: StreamObserver<KickPlayerResp>?) {
        val name = request!!.targetName

        if (request.playerName.isNotBlank() && playerController.removePlayer(name))
            messageController.sendMessage(request.playerName, "hat ${name} rausgeworfen.")
        else
            messageController.sendMessage(request.playerName, "konnte ${name} nicht rauswerfen.")

        responseObserver?.onNext(KickPlayerResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun switchTeam(request: SwitchTeamReq?, responseObserver: StreamObserver<SwitchTeamResp>?) {
        val name = request!!.targetName
        val team = request.targetTeam

        if (request.playerName.isNotBlank() && playerController.setPlayerTeam(name, team))
            messageController.sendMessage(request.playerName, "hat $name nach ${team.positionalName()} verschoben.")
        else
            messageController.sendMessage(request.playerName, "konnte ${name} nicht verschieben.")

        responseObserver?.onNext(SwitchTeamResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }
}