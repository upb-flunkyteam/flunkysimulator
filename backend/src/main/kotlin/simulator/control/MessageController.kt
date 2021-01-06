package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumMessageType
import de.flunkyteam.endpoints.projects.simulator.Message
import kotlin.concurrent.withLock

class MessageController: EventControllerBase<MessageController.MessageEvent>() {

    data class MessageEvent( val message: Message)

    private val onMessage = onEvent

    fun sendLogMessage(from: String, content: String){
        val message = Message.newBuilder()
            .setMessageType(EnumMessageType.MESSAGE_TYPE_LOG)
            .setContent(content)
            .setSender(from)
            .build()
        sendMessage(message)
    }

    fun sendMessage(message: Message) {
        handlerLock.withLock {
            onMessage(MessageEvent(message))
        }
    }
}