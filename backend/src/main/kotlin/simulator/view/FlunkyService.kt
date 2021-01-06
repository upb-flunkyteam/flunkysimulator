package simulator.view

import com.google.protobuf.Empty
import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.stub.StreamObserver
import simulator.buildRegisterHandler
import simulator.control.GameController
import simulator.control.MessageController
import simulator.model.game.positionalName

class FlunkyService(
    private val gameController: GameController,
    private val messageController: MessageController
) : FlunkyServiceGrpc.FlunkyServiceImplBase() {

    override fun throw_(request: ThrowReq?, responseObserver: StreamObserver<ThrowResp>?) {

        val result = gameController.throwBall(request!!.playerName, request.strength)
        when (result) {
            EnumThrowRespStatus.THROW_STATUS_SUCCESS ->
                messageController.sendLogMessage(
                    request.playerName,
                    "hat ${request.strength.toPrettyString()} geworfen"
                )
            EnumThrowRespStatus.THROW_STATUS_RESTING_PERIOD ->
                messageController.sendLogMessage(
                    request.playerName,
                    "darf noch nicht werfen, da wir uns noch vom letzten Wurf erholen."
                )
            EnumThrowRespStatus.THROW_STATUS_NOT_THROWING_PLAYER ->
                messageController.sendLogMessage(
                    request.playerName,
                    "ist nicht dran und darf daher nicht werfen."
                )
            else -> messageController.sendLogMessage(
                request.playerName,
                "darf nicht werfen"
            )
        }

        responseObserver?.onNext(
            ThrowResp.newBuilder()
                .setStatus(result)
                .build()
        )
        responseObserver?.onCompleted()
    }

    override fun modifyStrafbierCount(
        request: ModifyStrafbierCountReq,
        responseObserver: StreamObserver<ModifyStrafbierCountResp>?
    ) {
        if (request.playerName.isNotBlank() && gameController.modifyStrafbierCount(
                request.targetTeam,
                request.increment
            )
        ) {
            val text = "hat ein Strafbier für ${request.targetTeam.positionalName()} " +
                    if (request.increment)
                        "hinzugefügt"
                    else
                        "entfernt"
            messageController.sendLogMessage(request.playerName, text)
        } else {
            messageController.sendLogMessage(
                request.playerName,
                " hat die Strafbiere für ${request.targetTeam.positionalName()} nicht verändert."
            )
        }

        responseObserver?.onNext(ModifyStrafbierCountResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun resetGame(request: ResetGameReq, responseObserver: StreamObserver<ResetGameResp>?) {

        if (request.playerName.isNotBlank() && gameController.resetGame())
            messageController.sendLogMessage(
                request.playerName,
                "hat den Ground neu ausgemessen, die Kreide nachgezeichnet, die Center nachgefüllt und den Ball aufgepumt."
            )
        else
            messageController.sendLogMessage(request.playerName, "konnte das Spiel nicht neustarten")

        responseObserver?.onNext(ResetGameResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun selectThrowingPlayer(
        request: SelectThrowingPlayerReq,
        responseObserver: StreamObserver<SelectThrowingPlayerResp>?
    ) {
        if (request.playerName.isNotBlank() && gameController.forceThrowingPlayer(request.targetName))
            messageController.sendLogMessage(
                request.playerName,
                "hat ${request.targetName} als werfenden Spieler festgelegt."
            )
        else
            messageController.sendLogMessage(
                request.playerName,
                "konnte ${request.targetName} nicht als Werfer/in festlegen. Spielt nicht mit oder nicht existent?"
            )

        responseObserver?.onNext(SelectThrowingPlayerResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun abgegeben(request: AbgegebenReq, responseObserver: StreamObserver<AbgegebenResp>?) {
        val result = gameController.setAbgegeben(
            request.playerName,
            request.targetName,
            request.setTo
        )

        when (result) {
            EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_SUCCESS -> {
                var text =
                    "hat ${request.targetName}"
                if (request.setTo) {
                    if (!request.targetName.endsWith("s"))
                        text += "s"
                    text += " Abgabe abgenommen."
                } else
                    text += " ein Bier geöffnet."
                messageController.sendLogMessage(request.playerName, text)
            }
            EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_OWN_TEAM -> {
                var text = "kann ${request.targetName}"
                if (!request.targetName.endsWith("s"))
                    text += "s"
                text += " Abgabe nicht abnehmen, da sie im selben Team sind."
                messageController.sendLogMessage(request.playerName, text)
            }
            else ->
                messageController.sendLogMessage(
                    request.playerName,
                    "konnte ${request.targetName} Abgabestatus nicht ändern."
                )
        }

        responseObserver?.onNext(
            AbgegebenResp.newBuilder()
                .setStatus(result)
                .build()
        )
        responseObserver?.onCompleted()
    }

    override fun streamState(request: StreamStateReq?, responseObserver: StreamObserver<StreamStateResp>?) {
        // output current state
        responseObserver?.onNext(
            StreamStateResp.newBuilder()
                .setState(gameController.gameState.toGRPC())
                .build()
        )

        // output future states
        val handler =
            buildRegisterHandler { event: GameController.GameStateEvent ->
                //fails if stream is closed
                responseObserver?.onNext(
                    StreamStateResp.newBuilder()
                        .setState(event.state.toGRPC())
                        .build()
                )
            }

        gameController.addEventHandler(handler::doAction)
    }


    // -- Debug rpcs --

    override fun hardReset(request: Empty?, responseObserver: StreamObserver<Empty>) {
        gameController.hardReset()
        responseObserver.onNext(Empty.getDefaultInstance())
        responseObserver.onCompleted()
    }

    override fun setRestingPeriod(request: RestingPeriodReq, responseObserver: StreamObserver<Empty>) {
        gameController.setRestingPeriod(request.milliseconds)
        responseObserver.onNext(Empty.getDefaultInstance())
        responseObserver.onCompleted()
    }



    private fun EnumThrowStrength.toPrettyString() = when (this) {
        EnumThrowStrength.UNKNOWN_THROW_STRENGTH -> "unbekannt"
        EnumThrowStrength.SOFT_THROW_STRENGTH -> "leicht"
        EnumThrowStrength.MEDIUM_THROW_STRENGTH -> "mittel"
        EnumThrowStrength.HARD_THROW_STRENGTH -> "stark"
        EnumThrowStrength.UNRECOGNIZED -> "unbekannt"
    }

}