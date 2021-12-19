package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumMessageType
import de.flunkyteam.endpoints.projects.simulator.Message
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.math.min

class MessageController : EventControllerBase<MessageController.MessageEvent>() {

    data class MessageEvent(val message: Message)

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
        if (text.startsWith(".stoppuhr")) {
            val seconds = (text.substring(".stoppuhr".length).trim()).toIntOrNull() ?: 5
            if (seconds < 2) return

            countdown(message.sender, min(seconds, 15))
        }
        handlerLock.withLock {
            onMessage(MessageEvent(message))
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
}