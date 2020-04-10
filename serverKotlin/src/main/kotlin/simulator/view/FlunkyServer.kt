package simulator.view

import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.stub.StreamObserver
import simulator.control.GameController
import simulator.control.MessageController
import simulator.model.GameState

class FlunkyServer(
    private val gameController: GameController,
    private val messageController: MessageController
) : SimulatorGrpc.SimulatorImplBase() {

    override fun throw_(request: ThrowReq?, responseObserver: StreamObserver<ThrowResp>?) {
        gameController.throwBall(request!!.playerName, request.strength)

        responseObserver?.onNext(ThrowResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun registerPlayer(request: RegisterPlayerReq?, responseObserver: StreamObserver<RegisterPlayerResp>?) {
        val name = request!!.playerName

        gameController.registerPlayer(name)

        responseObserver!!.onNext(RegisterPlayerResp.getDefaultInstance())
        responseObserver.onCompleted()
    }

    override fun kickPlayer(request: KickPlayerReq?, responseObserver: StreamObserver<KickPlayerResp>?) {
        val name = request!!.targeName

        gameController.removePlayer(name)

        responseObserver?.onNext(KickPlayerResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun resetGame(request: ResetGameReq?, responseObserver: StreamObserver<ResetGameResp>?) {

        gameController.resetGameAndShuffleTeams()

        responseObserver?.onNext(ResetGameResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun selectThrowingPlayer(
        request: SelectThrowingPlayerReq?,
        responseObserver: StreamObserver<SelectThrowingPlayerResp>?
    ) {
        gameController.forceThrowingPlayer(request!!.targeName)

        responseObserver?.onNext(SelectThrowingPlayerResp.getDefaultInstance())
        responseObserver?.onCompleted()
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
            } finally {
                handler?.let { gameController.onNewGameState -= it }
            }
        }

        gameController.onNewGameState += handler
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
            } finally {
                handler?.let { messageController.onNewMessage -= it }
            }
        }

        messageController.onNewMessage += handler
    }
}