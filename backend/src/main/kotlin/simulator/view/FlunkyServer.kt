package simulator.view

import com.google.protobuf.Empty
import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import simulator.DeactiveableHandler
import simulator.control.GameController
import simulator.control.MessageController
import simulator.control.VideoController
import simulator.control.VideoEvent
import simulator.model.game.positionalName

class FlunkyServer(
    private val gameController: GameController,
    private val messageController: MessageController,
    private val videoController: VideoController
) : SimulatorGrpc.SimulatorImplBase() {

    override fun throw_(request: ThrowReq?, responseObserver: StreamObserver<ThrowResp>?) {

        val result = gameController.throwBall(request!!.playerName, request.strength)
        when (result) {
            EnumThrowRespStatus.THROW_STATUS_SUCCESS ->
                messageController.sendMessage(
                    request.playerName,
                    "hat ${request.strength.toPrettyString()} geworfen"
                )
            EnumThrowRespStatus.THROW_STATUS_RESTING_PERIOD ->
                messageController.sendMessage(
                    request.playerName,
                    "darf noch nicht werfen, da wir uns noch vom letzten Wurf erholen."
                )
            EnumThrowRespStatus.THROW_STATUS_NOT_THROWING_PLAYER ->
                messageController.sendMessage(
                    request.playerName,
                    "ist nicht dran und darf daher nicht werfen."
                )
            else -> messageController.sendMessage(
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

    override fun registerPlayer(request: RegisterPlayerReq, responseObserver: StreamObserver<RegisterPlayerResp>) {
        val name = request.playerName

        val loginStatus = gameController.registerPlayer(name)

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

        if (request.playerName.isNotBlank() && gameController.removePlayer(name))
            messageController.sendMessage(request.playerName, "hat ${name} rausgeworfen.")
        else
            messageController.sendMessage(request.playerName, "konnte ${name} nicht rauswerfen.")

        responseObserver?.onNext(KickPlayerResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun switchTeam(request: SwitchTeamReq?, responseObserver: StreamObserver<SwitchTeamResp>?) {
        val name = request!!.targetName
        val team = request.targetTeam



        if (request.playerName.isNotBlank() && gameController.setPlayerTeam(name, team))
            messageController.sendMessage(request.playerName, "hat $name nach ${team.positionalName()} verschoben.")
        else
            messageController.sendMessage(request.playerName, "konnte ${name} nicht verschieben.")

        responseObserver?.onNext(SwitchTeamResp.getDefaultInstance())
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
            messageController.sendMessage(request.playerName, text)
        } else {
            messageController.sendMessage(
                request.playerName,
                " hat die Strafbiere für ${request.targetTeam.positionalName()} nicht verändert."
            )
        }

        responseObserver?.onNext(ModifyStrafbierCountResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun resetGame(request: ResetGameReq, responseObserver: StreamObserver<ResetGameResp>?) {

        if (request.playerName.isNotBlank() && gameController.resetGameAndShuffleTeams())
            messageController.sendMessage(
                request.playerName,
                "hat den Ground neu ausgemessen, die Kreide nachgezeichnet, die Teams gemischt, die Center nachgefüllt und den Ball aufgepumt."
            )
        else
            messageController.sendMessage(request.playerName, "konnte das Spiel nicht neustarten")

        responseObserver?.onNext(ResetGameResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun selectThrowingPlayer(
        request: SelectThrowingPlayerReq,
        responseObserver: StreamObserver<SelectThrowingPlayerResp>?
    ) {
        if (request.playerName.isNotBlank() && gameController.forceThrowingPlayer(request.targetName))
            messageController.sendMessage(
                request.playerName,
                "hat ${request.targetName} als werfenden Spieler festgelegt."
            )
        else
            messageController.sendMessage(
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
                messageController.sendMessage(request.playerName, text)
            }
            EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_OWN_TEAM -> {
                var text = "kann ${request.targetName}"
                if (!request.targetName.endsWith("s"))
                    text += "s"
                text += " Abgabe nicht abnehmen, da sie im selben Team sind."
                messageController.sendMessage(request.playerName, text)
            }
            else ->
                messageController.sendMessage(
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

    override fun sendMessage(request: SendMessageReq?, responseObserver: StreamObserver<SendMessageResp>?) {
        messageController.sendMessage(request!!.playerName, ":"+request.content)

        responseObserver?.onNext(SendMessageResp.getDefaultInstance())
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

    override fun streamVideoEvents(
        request: StreamVideoEventsReq?,
        responseObserver: StreamObserver<StreamVideoEventsResp>?
    ) {

        videoController.getAllPreparedVideoEvents().forEach {
            responseObserver?.onNext(
                StreamVideoEventsResp.newBuilder()
                    .setEvent(it.toGrpc())
                    .build()
            )
        }

        val handler =
            buildRegisterHandler { event: VideoEvent ->
                responseObserver?.onNext(
                    StreamVideoEventsResp.newBuilder()
                        .setEvent(event.toGrpc())
                        .build()
                )
            }

        videoController.addEventHandler(handler::doAction)
    }

    override fun streamLog(request: LogReq, responseObserver: StreamObserver<LogResp>) {

        val handler =
            buildRegisterHandler { event: MessageController.MessageEvent ->
                responseObserver.onNext(
                    LogResp.newBuilder()
                        .setContent(event.content)
                        .setSender(event.sender)
                        .build()
                )
            }

        responseObserver.onNext(LogResp.newBuilder()
            .setSender("Server:")
            .setContent(patchNotes)
            .build())

        messageController.addEventHandler(handler::doAction)


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

    /***
     * action should put something in a responseObserver
     */
    private fun <Event> buildRegisterHandler(action: (Event) -> Unit): DeactiveableHandler<Event> =
        DeactiveableHandler({ event: Event,
                              deactiveableHandler: DeactiveableHandler<Event> ->
            try {
                //fails if stream is closed
                action(event)
            } catch (e: StatusRuntimeException) {
                if (e.status.code == Status.Code.CANCELLED) {
                    println("Another stream bites the dust. Message: \n ${e.message}")
                    deactiveableHandler.enabled = false
                    /*TODO delete handlers when connection gone but not while iterating
                         through handlers like in this position, because this would casue
                         concurrency modification errors because of the underlying HashSet
                         in the Event plugin.
                         handler?.let { gameController.removeEventHandler(it) }
                         */
                } else
                    throw e
            }
        })

    private fun EnumThrowStrength.toPrettyString() = when (this) {
        EnumThrowStrength.UNKNOWN_THROW_STRENGTH -> "unbekannt"
        EnumThrowStrength.SOFT_THROW_STRENGTH -> "leicht"
        EnumThrowStrength.MEDIUM_THROW_STRENGTH -> "mittel"
        EnumThrowStrength.HARD_THROW_STRENGTH -> "stark"
        EnumThrowStrength.UNRECOGNIZED -> "unbekannt"
    }

    private val patchNotes = """Version 2.2:
    - Strafbiere haben auch Videos
    - Gesamte Infrastruktur auf Docker-Images umgestellt
    - Infrastruktur umgezogen"""
}