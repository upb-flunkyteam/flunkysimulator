package simulator.control

import kotlinx.event.event

class MessageController {

    data class MessageEvent( val content: String)

    val onNewMessage = event<MessageEvent>()

    fun sendMessage(from: String, content: String){
        onNewMessage(MessageEvent("$from: $content"))
    }
}