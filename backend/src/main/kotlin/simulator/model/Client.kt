package simulator.model

import de.flunkyteam.endpoints.projects.simulator.AliveChallenge
import kotlinx.event.SetEvent
import kotlinx.event.event
import simulator.control.ClientsManager
import simulator.control.EventControllerBase
import java.util.concurrent.locks.ReentrantLock

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
