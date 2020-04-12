package simulator.control

import kotlin.concurrent.withLock

class MessageController: EventController<MessageController.MessageEvent>() {

    data class MessageEvent( val content: String)

    private val onMessage = onEvent

    fun sendMessage(from: String, content: String){
        val message = "$from $content"
        handlerLock.withLock {
            onMessage(MessageEvent(message))
        }
    }
}