package simulator

import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.stub.StreamObserver

class FlunkyServer(): SimulatorGrpc.SimulatorImplBase() {

    override fun throw_(request: ThrowReq?, responseObserver: StreamObserver<ThrowResp>?) {
        super.throw_(request, responseObserver)
    }

    override fun registerPlayer(request: RegisterPlayerReq?, responseObserver: StreamObserver<RegisterPlayerResp>?) {
        super.registerPlayer(request, responseObserver)
    }

    override fun kickPlayer(request: KickPlayerReq?, responseObserver: StreamObserver<KickPlayerResp>?) {
        super.kickPlayer(request, responseObserver)
    }

    override fun resetGame(request: ResetGameReq?, responseObserver: StreamObserver<ResetGameResp>?) {
        super.resetGame(request, responseObserver)
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
        super.streamState(request, responseObserver)
    }

    override fun streamEvents(request: StreamEventsReq?, responseObserver: StreamObserver<StreamEventsResp>?) {
        super.streamEvents(request, responseObserver)
    }

    override fun streamLog(request: LogReq?, responseObserver: StreamObserver<LogResp>?) {
        super.streamLog(request, responseObserver)
    }
}