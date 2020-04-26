package simulator.control

import kotlin.concurrent.withLock

class MessageController: EventControllerBase<MessageController.MessageEvent>() {

    data class MessageEvent( val content: String, val sender: String)

    private val onMessage = onEvent

    fun sendMessage(from: String, content: String){
        handlerLock.withLock {
            onMessage(MessageEvent(content, from))
        }
    }
}