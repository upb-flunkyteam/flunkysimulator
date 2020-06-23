package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumConnectionStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import simulator.getRandomString
import simulator.model.Client
import simulator.model.Player
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ClientManager {

    private val clientLock = ReentrantLock()

    private var clients: Set<Client> = emptySet()
    private var playerToClients: Map<Player, Int> = emptyMap()
    private var idsToAliveChallenges: Map<Int, () -> Boolean> = emptyMap()

    private fun getOwner(player: Player): Client? = playerToClients[player]?.let { getClient(it) }

    private fun getClient(id: Int) = clients.firstOrNull { it.id == id }

    private fun updateClient(newClient: Client) {
        clientLock.withLock {
            val old = getClient(newClient.id)!!
            clients = clients - old + newClient
        }
    }

    init {
        GlobalScope.launch {
            delay(1000 * 10)
            // wait 10 sek otherwise stuff might not be initialized at startup
            // kinda dirty, I know
            pokeClients()
            delay(1000 * 60) // 1 min
        }
    }

    fun getClient(secret: String): Client? = clients.firstOrNull { it.secret == secret }

    /**
     * @return secret of the client
     */
    fun registerClient(isAliveChallenge: () -> Boolean): Client {
        clientLock.withLock {
            val client = Client(getRandomString(10))
            clients = clients + client
            idsToAliveChallenges = idsToAliveChallenges + (client.id to isAliveChallenge)
            return client
        }
    }

    fun removeClient(id: Int) {
        clientLock.withLock {
            getClient(id)?.let { client ->
                idsToAliveChallenges = idsToAliveChallenges - id
                clients = clients - client
                playerToClients = playerToClients.mapNotNull { if (it.value == id) null else it.toPair() }.toMap()
            }
        }
    }

    fun registerPlayer(player: Player, client: Client): Boolean {
        clientLock.withLock {
            playerToClients[player]?.let { oldOwnerId ->
                if (idsToAliveChallenges[oldOwnerId]?.invoke()
                        ?: throw error("Client with id ${oldOwnerId} owns player ${player.name} but doesnt have an alive challenge")
                )
                    return false //old owner is alive
                else
                    removeClient(oldOwnerId) //guess they are dead, time to move on
            }

            playerToClients = playerToClients.plus(player to client.id)
            updateClient(client.copy(players = client.players.plus(player)))

            return true
        }
    }

    fun removePlayer(player: Player) {
        clientLock.withLock {
            getOwner(player)?.let { client ->
                updateClient(client.copy(players = client.players.minus(player)))
                playerToClients = playerToClients.minus(player)
            }
        }
    }

    fun getConnectionStatus(player: Player): EnumConnectionStatus =
        getOwner(player)?.let { EnumConnectionStatus.CONNECTION_CONNECTED }
            ?: EnumConnectionStatus.CONNECTION_DISCONNECTED

    private fun pokeClients() {
        idsToAliveChallenges.forEach {
            if (!it.value()) {
                removeClient(it.key)
            }
        }
    }
}
