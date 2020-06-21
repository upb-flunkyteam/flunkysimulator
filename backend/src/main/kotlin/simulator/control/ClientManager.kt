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
            delay(1000*10)
            // wait 10 sek otherwise stuff might not be initialized at startup
            // kinda dirty, I know
            pokeClients()
            delay(1000*60) // 1 min
        }
    }

    fun registerClient(isAliveChallenge: () -> Boolean): String {
        clientLock.withLock {
            val client = Client(getRandomString(10))
            clients = clients + client
            idsToAliveChallenges = idsToAliveChallenges + (client.id to isAliveChallenge)
            return client.secret
        }
    }

    fun removeClient(id: Int) {
        clientLock.withLock {
            getClient(id)?.let {
                idsToAliveChallenges = idsToAliveChallenges - id
                clients = clients - it
            }
        }
    }

    fun registerPlayer(player: Player, client: Client): Boolean {
        clientLock.withLock {
            return if (!playerToClients.containsKey(player)) {
                playerToClients = playerToClients.plus(player to client.id)
                updateClient(client.copy(players = client.players.plus(player)))
                true
            } else
                false
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

    private fun pokeClients(){
        idsToAliveChallenges.forEach {
            if (!it.value()){
                removeClient(it.key)
            }
        }
    }

}
