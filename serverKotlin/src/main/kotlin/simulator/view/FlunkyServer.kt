package simulator.view

import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import simulator.control.GameController
import simulator.control.MessageController
import simulator.model.GameState

class FlunkyServer(
    private val gameController: GameController,
    private val messageController: MessageController
) : SimulatorGrpc.SimulatorImplBase() {

    override fun throw_(request: ThrowReq?, responseObserver: StreamObserver<ThrowResp>?) {

        if (gameController.throwBall(request!!.playerName, request.strength))
            messageController.sendMessage(request.playerName, "hat geworfen")
        else
            messageController.sendMessage(request.playerName, "darf nicht werfen")

        responseObserver?.onNext(ThrowResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun registerPlayer(request: RegisterPlayerReq?, responseObserver: StreamObserver<RegisterPlayerResp>?) {
        val name = request!!.playerName

        if (gameController.registerPlayer(name))
            messageController.sendMessage(request.playerName, "hat sich registriert. Willkommen Athlet!")
        else
            messageController.sendMessage(request.playerName, "konnte sich nicht registrieren. Name schon vergeben?")

        responseObserver!!.onNext(RegisterPlayerResp.getDefaultInstance())
        responseObserver.onCompleted()
    }

    override fun kickPlayer(request: KickPlayerReq?, responseObserver: StreamObserver<KickPlayerResp>?) {
        val name = request!!.targeName

        if(gameController.removePlayer(name))
            messageController.sendMessage(request.playerName, "hat ${name} rausgeworfen.")
        else
            messageController.sendMessage(request.playerName, "konnte ${name} nicht rauswerfen.")

        responseObserver?.onNext(KickPlayerResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun switchTeam(request: SwitchTeamReq?, responseObserver: StreamObserver<SwitchTeamResp>?) {
        val name = request!!.targetName;
        val team = request.targetTeam

        if(gameController.switchTeam(name, team))
            messageController.sendMessage(request.playerName, "hat $name nach ${team.toString()} verschoben.")
        else
            messageController.sendMessage(request.playerName, "konnte ${name} nicht verschieben.")

        responseObserver?.onNext(SwitchTeamResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun modifyStrafbierCount(
        request: ModifyStrafbierCountReq?,
        responseObserver: StreamObserver<ModifyStrafbierCountResp>?
    ) {
        super.modifyStrafbierCount(request, responseObserver)
    }

    override fun resetGame(request: ResetGameReq?, responseObserver: StreamObserver<ResetGameResp>?) {

        if(gameController.resetGameAndShuffleTeams())
            messageController.sendMessage(request!!.playerName, "das den Ground neu ausgemessen, die Kreide nachgezeichnet, die Teams neu gemischt, die Center nachgefüllt und den Ball aufgepumt.")
        else
            messageController.sendMessage(request!!.playerName, "konnte das Spiel nicht neustarten")

        responseObserver?.onNext(ResetGameResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun selectThrowingPlayer(
        request: SelectThrowingPlayerReq?,
        responseObserver: StreamObserver<SelectThrowingPlayerResp>?
    ) {
        if(gameController.forceThrowingPlayer(request!!.targeName))
            messageController.sendMessage(request.playerName, "hat ${request!!.targeName} als werfenden Spieler festgelegt.")
        else
            messageController.sendMessage(request.playerName, "konnte ${request!!.targeName} nicht als Werfer festlegen. Spielt nicht mit oder nicht existent?")

        responseObserver?.onNext(SelectThrowingPlayerResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun abgegeben(request: AbgegebenReq?, responseObserver: StreamObserver<AbgegebenResp>?) {
        super.abgegeben(request, responseObserver)
    }

    override fun sendMessage(request: SendMessageReq?, responseObserver: StreamObserver<SendMessageResp>?) {
        messageController.sendMessage(request!!.playerName, request.content)

        responseObserver?.onNext(SendMessageResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun streamState(request: StreamStateReq?, responseObserver: StreamObserver<StreamStateResp>?) {
        responseObserver?.onNext(
            StreamStateResp.newBuilder()
                .setState(gameController.gameState.toGRPC())
                .build()
        )

        var handler: ((GameController.GameStateEvent) -> Unit)? = null
        handler = { (gameState: GameState) ->
            try {
                //fails if stream is closed
                responseObserver?.onNext(
                    StreamStateResp.newBuilder()
                        .setState(gameState.toGRPC())
                        .build()
                )
            } catch (e: io.grpc.StatusRuntimeException){
                if (e.status.code == Status.Code.CANCELLED)
                    println("Another stream bites the dust.")
                else
                    throw e
            } finally {
                handler?.let { gameController.removeEventHandler(it) }
            }
        }

        gameController.addEventHandler { it }
    }

    override fun streamEvents(request: StreamEventsReq?, responseObserver: StreamObserver<StreamEventsResp>?) {
        super.streamEvents(request, responseObserver)
    }

    override fun streamLog(request: LogReq?, responseObserver: StreamObserver<LogResp>?) {
        var handler: ((MessageController.MessageEvent) -> Unit)? = null
        handler = { event: MessageController.MessageEvent ->
            try {
                //fails if stream is closed
                responseObserver?.onNext(
                    LogResp.newBuilder()
                        .setContent(event.content)
                        .build()
                )
            } catch (e: io.grpc.StatusRuntimeException){
                if (e.status.code == Status.Code.CANCELLED)
                    println("Another one bites the dust.")
                else
                    throw e
            } finally {
                handler?.let { messageController.removeEventHandler(it) }
            }
        }

        messageController.addEventHandler { handler }
    }
}