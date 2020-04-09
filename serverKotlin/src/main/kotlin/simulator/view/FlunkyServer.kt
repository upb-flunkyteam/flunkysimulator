package simulator.view

import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.stub.StreamObserver
import simulator.control.GameController

class FlunkyServer(private val gameController: GameController): SimulatorGrpc.SimulatorImplBase() {

    //todo register listner

    override fun throw_(request: ThrowReq?, responseObserver: StreamObserver<ThrowResp>?) {
        super.throw_(request, responseObserver)
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
        super.selectThrowingPlayer(request, responseObserver)
    }

    override fun sendMessage(request: SendMessageReq?, responseObserver: StreamObserver<SendMessageResp>?) {
        super.sendMessage(request, responseObserver)
    }

    override fun streamState(request: StreamStateReq?, responseObserver: StreamObserver<StreamStateResp>?) {
        responseObserver?.onNext(
            StreamStateResp.newBuilder()
            .setState(gameController.state.toGRPC())
            .build())

        gameController.newGameStateEvent += { (gameState) ->
            try {
                responseObserver?.onNext(
                    StreamStateResp.newBuilder()
                        .setState(gameState.toGRPC())
                        .build()
                )
            }finally {

            }
        }
    }

    override fun streamEvents(request: StreamEventsReq?, responseObserver: StreamObserver<StreamEventsResp>?) {
        super.streamEvents(request, responseObserver)
    }

    override fun streamLog(request: LogReq?, responseObserver: StreamObserver<LogResp>?) {
        super.streamLog(request, responseObserver)
    }
}