package simulator.model

import simulator.control.ClientsManager
import simulator.control.EventControllerBase

private var ids = 1

/***
 * Represents a client eg. an opened tab in browser.
 */
data class Client(
    val secret: String,
    val aliveChallenge: () -> Boolean,
    val players: List<String> = listOf(),
    val id: Int = ids++,
    val clientEvent: ClientEvent = ClientEvent()
)

/**
 * Composition wrapper for event, so it will be copied correctly by the data class.
 * Inheritance does not work well with data classes.
 */
class ClientEvent : EventControllerBase<ClientsManager.ClientEvent>()
