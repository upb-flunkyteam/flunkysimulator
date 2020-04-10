package simulator.control

import kotlinx.event.event
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MessageController {

    data class MessageEvent( val content: String)

    private val handlerLock = ReentrantLock()
    private val onNewMessage = event<MessageEvent>()
        @Synchronized get

    fun addEventHandler(handler: ((MessageEvent) -> Unit)){
        handlerLock.withLock { onNewMessage += handler }
    }

    fun removeEventHandler(handler: ((MessageEvent) -> Unit)){
        handlerLock.withLock { onNewMessage -= handler }
    }

    fun sendMessage(from: String, content: String){
        val message = "$from: $content"
        println(message)
        onNewMessage(MessageEvent(message))
    }
}