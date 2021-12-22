package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumMessageType
import de.flunkyteam.endpoints.projects.simulator.Message
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.math.min

class MessageController : EventControllerBase<MessageController.MessageEvent>() {
    private val rpsController = RPSController(this)
    private val onMessage = onEvent

    fun sendLogMessage(from: String, content: String) {
        val message = Message.newBuilder()
            .setMessageType(EnumMessageType.MESSAGE_TYPE_LOG)
            .setContent(content)
            .setSender(from)
            .build()
        sendMessage(message)
    }

    fun sendMessage(message: Message) {
        val text = message.content
        val messageProcessed = when {
            text.startsWith(".stoppuhr") -> {
                val seconds = (text.substring(".stoppuhr".length).trim()).toIntOrNull() ?: 5
                if (seconds < 2) return
                countdown(message.sender, min(seconds, 15))
                false
            }
            text.startsWith(".ssp") -> rpsController.handleMessage(message)
            else -> false
        }
        if (!messageProcessed) {
            handlerLock.withLock {
                onMessage(MessageEvent(message))
            }
        }
    }

    private fun countdown(sender: String, seconds: Int) {
        thread {
            handlerLock.withLock {
                val msg = MessageEvent(
                    Message.newBuilder()
                        .setContent("Stoppuhr von $sender für $seconds Sekunden gestartet.")
                        .setMessageType(EnumMessageType.MESSAGE_TYPE_LOG)
                        .setSender("Server")
                        .build()
                )
                onMessage(msg)
            }
            var counter = seconds
            while (counter > 0) {
                handlerLock.withLock {
                    val msg = MessageEvent(
                        Message.newBuilder()
                            .setContent("Stoppuhr: ${counter--}")
                            .setMessageType(EnumMessageType.MESSAGE_TYPE_LOG)
                            .setSender("Server")
                            .build()
                    )
                    onMessage(msg)
                }
                sleep(1000)
            }
            handlerLock.withLock {
                val msg = MessageEvent(
                    Message.newBuilder()
                        .setContent("Stoppuhr abgelaufen!")
                        .setMessageType(EnumMessageType.MESSAGE_TYPE_LOG)
                        .setSender("Server")
                        .build()
                )
                onMessage(msg)
            }
        }
    }

    data class MessageEvent(val message: Message)

    /**
     * Rock, Paper, Scissors controller
     */
    private class RPSController(val messageController: MessageController) {

        var roundsToWin = 2
        var player1 = ""
        var player2 = ""
        var state = State.DONE
        var playerWins: Map<String, Int> = emptyMap()
        var playerSelection: Map<String, SELECTION> = emptyMap()

        fun handleMessage(message: Message): Boolean {
            val text = message.content.substring(".ssp".length).trim().lowercase()
            when {
                state == State.DONE && text.startsWith("start") -> {
                    roundsToWin = ((text.substring(".ssp start".length)
                        .replace('[', ' ')
                        .replace(']', ' ')
                        .trim())
                        .toIntOrNull() ?: 3) / 2 + 1

                    messageController.sendLogMessage(
                        "SSP",
                        """${message.sender} hat eine Runde Stein, Schere, Papier (best of ${roundsToWin}) gestartet.
                            |Wenn du mitspielen möchtest schreibe '.ssp hier'.""".trimMargin()
                    )

                    state = State.WAITING_FOR_PLAYER2
                    player1 = message.sender
                    playerWins = emptyMap()
                    playerSelection = emptyMap()
                    return false
                }
                state == State.WAITING_FOR_PLAYER2 && text.startsWith("hier") -> {
                    if (player1 == message.sender) {
                        messageController.sendLogMessage(
                            "SSP",
                            """${message.sender}, bitte nicht in der Öffentlichkeit mit dir selber spielen.""".trimMargin()
                        )
                        return false
                    }
                    state = State.RUNNING
                    player2 = message.sender
                    messageController.sendLogMessage(
                        "SSP",
                        """${message.sender} hat die Herausforderung von $player1 angenommen!""".trimMargin()
                    )
                    messageController.sendLogMessage(
                        "SSP",
                        """Setzt euere Hände mit ".ssp Schere|X|Papier|P|Stein|O.""".trimMargin()
                    )
                    return false
                }
                state == State.RUNNING -> {
                    handleInGameMessage(message)
                    return true
                }
                else -> {
                    printHelp()
                    return false
                }
            }
        }

        private fun printHelp() {
            messageController.sendLogMessage(
                "SSP",
                """Schere,Stein,Papier Simulator 1.0:
                                    | .ssp start [RundenZahl] - startet ein Spiel
                                    | .ssp hier - nimmt eine Spielherausforderung an
                                    | .ssp schere - Wähle Schere für die aktuelle Runde.
                                    | .ssp X - Wähle Schere für die aktuelle Runde.
                                    | .ssp stein - Wähle Stein für die aktuelle Runde.
                                    | .ssp O - Wähle Stein für die aktuelle Runde.
                                    | .ssp papier - Wähle Papier für die aktuelle Runde.
                                    | .ssp P - Wähle Papier für die aktuelle Runde.
                                    | .ssp hilfe - Gibt diesen Text aus.
                                """.trimMargin()
            )
        }

        private fun handleInGameMessage(message: Message) {
            val text = message.content.substring(".ssp".length).trim().lowercase()
            when {
                message.sender != player1 && message.sender != player2 ->
                    return
                text.startsWith("schere")
                        || text == "x" -> {
                    playerSelection = playerSelection + (message.sender to SELECTION.SCISSORS)
                }
                text.startsWith("stein")
                        || text == "o" -> {
                    playerSelection = playerSelection + (message.sender to SELECTION.STONE)
                }
                text.startsWith("papier")
                        || text == "p" -> {
                    playerSelection = playerSelection + (message.sender to SELECTION.PAPER)
                }
                text.startsWith("spock")
                        || text.startsWith("echse")
                        || text.startsWith("brunnen") -> {
                    messageController.sendLogMessage(
                        "SSP",
                        """Spock ist in den Brunnen gefallen und hat alle Echsen gegessen.""".trimMargin()
                    )
                    return
                }
                else -> {
                    printHelp()
                    return
                }
            }
            if (playerSelection.size < 2) {
                messageController.sendLogMessage(
                    "SSP",
                    """${message.sender} hat eine auswahl getroffen.""".trimMargin()
                )
                return
            }
            if (playerSelection.size == 2) {
                val selection1 = playerSelection[player1] ?: return
                val selection2 = playerSelection[player2] ?: return
                if (selection1 == selection2) {
                    messageController.sendLogMessage(
                        "SSP",
                        """${selection1.symbol.random()} Unentschieden! ${selection1.symbol.random()}""".trimMargin()
                    )
                    playerSelection = emptyMap()
                    return
                }
                val winningPlayer = if (selection1.winsAgainst == selection2.ordinal) {
                    player1
                } else {
                    player2
                }
                messageController.sendLogMessage(
                    "SSP",
                    """$winningPlayer hat die Runde gewonnen! ${selection1.symbol.random()} gegen ${selection2.symbol.random()}""".trimMargin()
                )
                playerSelection = emptyMap()

                val winningPlayerWins = playerWins.getOrDefault(winningPlayer, 0) + 1
                playerWins = playerWins + (winningPlayer to winningPlayerWins)

                if (winningPlayerWins >= roundsToWin) {
                    state = State.DONE
                    messageController.sendLogMessage(
                        "SSP",
                        """$winningPlayer hat das Spiel ${playerWins[player1]} zu ${playerWins[player2]} gewonnen!""".trimMargin()
                    )
                }
                return
            }
        }

        private enum class State {
            DONE,
            WAITING_FOR_PLAYER2,
            RUNNING
        }

        private enum class SELECTION(val symbol: List<String>, val winsAgainst: Int) {
            SCISSORS(listOf("✂"), 2),
            STONE(listOf("⛰","🗻","🏔","🥌","🌑","☄"), 0),
            PAPER(listOf("🧻","📝","📃","📜","📄"), 1)
        }
    }
}