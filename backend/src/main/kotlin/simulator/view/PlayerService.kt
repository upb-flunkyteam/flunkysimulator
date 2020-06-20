package simulator.view

import com.google.protobuf.Empty
import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.stub.StreamObserver
import simulator.buildRegisterHandler
import simulator.control.MessageController
import simulator.control.PlayerController
import simulator.model.game.Team
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

    override fun switchTeam(request: SwitchTeamReq, responseObserver: StreamObserver<SwitchTeamResp>) {
        val name = request.targetName
        val team = request.targetTeam

        if (request.playerName.isNotBlank() && playerController.setPlayerTeam(name, team))
            messageController.sendMessage(request.playerName, "hat $name nach ${team.positionalName()} verschoben.")
        else
            messageController.sendMessage(request.playerName, "konnte ${name} nicht verschieben.")

        responseObserver.onNext(SwitchTeamResp.getDefaultInstance())
        responseObserver.onCompleted()
    }

    override fun shuffleTeams(request: ShuffleTeamsReq, responseObserver: StreamObserver<ShuffleTeamsResp>) {
        val name = request.playerName

        if (request.playerName.isNotBlank() && playerController.getPlayer(name) != null) {
            playerController.shuffleTeams()
            messageController.sendMessage(request.playerName, "hat die Teams neu gemischt.")
        } else
            messageController.sendMessage(request.playerName, "konnte die teams nicht neu mischen.")

        responseObserver.onNext(ShuffleTeamsResp.getDefaultInstance())
        responseObserver.onCompleted()
    }

    override fun streamAllPlayers(request: Empty?, responseObserver: StreamObserver<PlayerListResp>?) {

        responseObserver?.onNext(
            PlayerListResp.newBuilder()
                .addAllPlayers(playerController.allPlayers.map { it.toGRPC() })
                .build()
        )


        val handler =
            buildRegisterHandler { _: PlayerController.PlayersEvent ->
                responseObserver?.onNext(
                    PlayerListResp.newBuilder()
                        .addAllPlayers(playerController.allPlayers.map { it.toGRPC() })
                        .build()
                )
            }

        playerController.addEventHandler(handler::doAction)
    }

    override fun streamTeamAPlayers(request: Empty?, responseObserver: StreamObserver<PlayerListResp>?) {

        responseObserver?.onNext(
            PlayerListResp.newBuilder()
                .addAllPlayers(playerController.TeamA.map { it.toGRPC() })
                .build()
        )


        val handler =
            buildRegisterHandler { event: PlayerController.PlayersEvent ->
                if (event.updateOf.contains(Team.A))
                    responseObserver?.onNext(
                        PlayerListResp.newBuilder()
                            .addAllPlayers(playerController.TeamA.map { it.toGRPC() })
                            .build()
                    )
            }

        playerController.addEventHandler(handler::doAction)
    }

    override fun streamTeamBPlayers(request: Empty?, responseObserver: StreamObserver<PlayerListResp>?) {

        responseObserver?.onNext(
            PlayerListResp.newBuilder()
                .addAllPlayers(playerController.TeamB.map { it.toGRPC() })
                .build()
        )


        val handler =
            buildRegisterHandler { event: PlayerController.PlayersEvent ->
                if (event.updateOf.contains(Team.B))
                    responseObserver?.onNext(
                        PlayerListResp.newBuilder()
                            .addAllPlayers(playerController.TeamB.map { it.toGRPC() })
                            .build()
                    )
            }

        playerController.addEventHandler(handler::doAction)
    }

    override fun streamSpectators(request: Empty?, responseObserver: StreamObserver<PlayerListResp>?) {

        responseObserver?.onNext(
            PlayerListResp.newBuilder()
                .addAllPlayers(playerController.Spectators.map { it.toGRPC() })
                .build()
        )


        val handler =
            buildRegisterHandler { event: PlayerController.PlayersEvent ->
                if (event.updateOf.contains(Team.Spectator))
                    responseObserver?.onNext(
                        PlayerListResp.newBuilder()
                            .addAllPlayers(playerController.Spectators.map { it.toGRPC() })
                            .build()
                    )
            }

        playerController.addEventHandler(handler::doAction)
    }
}