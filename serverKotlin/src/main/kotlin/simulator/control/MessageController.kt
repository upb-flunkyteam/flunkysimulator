package simulator.control

import kotlinx.event.event

class MessageController {

    data class MessageEvent( val content: String)

    val onNewMessage = event<MessageEvent>()

    fun sendMessage(from: String, content: String){
        val message = "$from: $content"
        println(message)
        onNewMessage(MessageEvent(message))
    }
}