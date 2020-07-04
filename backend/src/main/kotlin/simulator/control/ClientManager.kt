package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumConnectionStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import simulator.getRandomString
import simulator.model.Client
import simulator.model.Player
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Logger
import kotlin.concurrent.withLock

/*
This is not a controller as the other classes in this package because our controllers are
responsible for one event handler. This class on the other hand handles multiple client events/streams
and to distinguish that it is not called a controller.
 */

class ClientManager (private val pokeInterval: Int = 10){

    private val clientLock = ReentrantLock()

    private var clients: Set<Client> = emptySet()
    private var playerToClients: Map<Player, Int> = emptyMap()
    private var idsToAliveChallenges: Map<Int, () -> Boolean> = emptyMap()
    private lateinit var triggerPlayerUpdate: () -> Unit

    private fun getOwner(player: Player): Client? = playerToClients[player]?.let { getClient(it) }

    private fun getClient(id: Int) = clients.firstOrNull { it.id == id }

    private fun updateClient(newClient: Client) {
        clientLock.withLock {
            val old = getClient(newClient.id)!!
            clients = clients - old + newClient
        }
    }

    fun init(triggerPlayerUpdate: () -> Unit){
        this.triggerPlayerUpdate = triggerPlayerUpdate
    }

    init {
        GlobalScope.launch {
            delay(1000 * 10)
            // wait 10 sek otherwise stuff might not be initialized at startup
            // kinda dirty, I know
            while (true) {
                pokeClients()
                delay(1000L * pokeInterval)
            }
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

            playerToClients = playerToClients.minus(client.players).plus(player to client.id)
            //TODO allow more than one player?
            updateClient(client.copy(players = listOf(player)))

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
        logger.info("Poking clients")
        var allAlive = true
        idsToAliveChallenges.forEach {
            if (!it.value()) {
                removeClient(it.key)
                allAlive = false
            }
        }
        // connection status changed, update the players
        if (!allAlive){
            triggerPlayerUpdate()
        }
    }
    private val logger = Logger.getLogger(this::class.java.name)
}
