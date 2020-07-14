package simulator.model

import de.flunkyteam.endpoints.projects.simulator.AliveChallenge
import simulator.control.ClientsManager
import simulator.control.EventControllerBase

private var ids = 1

/***
 * Represents a client eg. an opened tab in browser.
 */
data class Client(
    val secret: String,
    val aliveChallenge: () -> Boolean,
    val players: List<Player> = listOf(),
    val id: Int = ids++
): EventControllerBase<ClientsManager.ClientEvent>() 
