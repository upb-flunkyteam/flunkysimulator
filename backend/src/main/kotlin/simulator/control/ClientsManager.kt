package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumConnectionStatus
import de.flunkyteam.endpoints.projects.simulator.EnumLoginStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.web.util.HtmlUtils
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

class ClientsManager(private val playerController: PlayerController, private val pokeInterval: Int = 10) {

    private val clientLock = ReentrantLock()

    private var clients: Set<Client> = emptySet()
    private var playerToClients: Map<String, Int> = emptyMap()

    private fun getOwner(player: String): Client? = playerToClients[player]?.let { getClient(it) }

    private fun getClient(id: Int) = clients.firstOrNull { it.id == id }

    private fun updateClient(newClient: Client) {
        clientLock.withLock {
            val old = getClient(newClient.id)!!
            clients = clients - old + newClient
            newClient.clientEvent.triggerWithLock(ClientEvent.OwnedPlayersUpdate(newClient.players))
        }
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

    sealed class ClientEvent {
        class OwnedPlayersUpdate(val players: List<String>) : ClientEvent()
    }

    fun getClient(secret: String): Client? = clients.firstOrNull { it.secret == secret }

    /**
     * @return secret of the client
     */
    fun registerClient(isAliveChallenge: () -> Boolean): Client {
        clientLock.withLock {
            val client = Client(getRandomString(10), isAliveChallenge)
            clients = clients + client
            return client
        }
    }

    fun removeClient(id: Int) {
        clientLock.withLock {
            getClient(id)?.let { client ->
                clients = clients - client
                playerToClients = playerToClients.mapNotNull { if (it.value == id) null else it.toPair() }.toMap()
            }
        }
    }

    internal fun registerPlayerWithClient(player: String, client: Client): Boolean {
        if (client.players.contains(player))
            return true

        clientLock.withLock {
            //check for old owner
            playerToClients[player]?.let { oldOwnerId ->
                if (clients.firstOrNull { it.id == oldOwnerId }
                        ?.let { c -> c.aliveChallenge() } == true)
                    return false //old owner is alive
                else
                    removeClient(oldOwnerId) //guess they are dead, time to move on
            }

            playerToClients = playerToClients.plus(player to client.id)
            updateClient(client.copy(players = client.players.plus(player)))

            return true
        }
    }

    fun registerPlayer(name: String, client: Client): LoginResp {

        if (name.isEmpty())
            return LoginResp(EnumLoginStatus.LOGIN_STATUS_EMPTY)

        val newName = HtmlUtils.htmlEscape(name.trim())

        val (player, isNew) = playerController.createOrFindPlayer(newName)
        val successfulRegistration = registerPlayerWithClient(player.name, client)

        return when {
            isNew -> {
                if (!successfulRegistration)
                    throw error("Could not register new player ${player.name} with client ${client.id}")
                playerController.triggerUpdate(setOf(player.team))
                LoginResp(
                    EnumLoginStatus.LOGIN_STATUS_NAME_TAKEN,
                    newName
                )

            }
            !isNew && successfulRegistration -> {
                playerController.triggerUpdate(setOf(player.team))
                LoginResp(
                    EnumLoginStatus.LOGIN_STATUS_SUCCESS,
                    newName
                )
            }
            else -> {
                LoginResp(
                    EnumLoginStatus.LOGIN_STATUS_PLAYER_TAKEN,
                    newName
                )
            }
        }
    }

    fun removePlayer(name: String) {
        clientLock.withLock {
            getOwner(name)?.let { client ->
                removePlayer(client, name)
            }
        }
    }

    fun removePlayer(client: Client, player: String) {
        clientLock.withLock {
            updateClient(client.copy(players = client.players.minus(player)))
            playerToClients = playerToClients.minus(player)
        }
    }

    fun getConnectionStatus(player: Player): EnumConnectionStatus =
        getOwner(player.name)?.let { EnumConnectionStatus.CONNECTION_CONNECTED }
            ?: EnumConnectionStatus.CONNECTION_DISCONNECTED

    private fun pokeClients() {
        logger.info("Poking clients")
        var allAlive = true
        clients.forEach {
            if (!it.aliveChallenge()) {
                removeClient(it.id)
                allAlive = false
            }
        }
        // connection status changed, update the players
        if (!allAlive) {
            playerController.triggerUpdate()
        }
    }

    private val logger = Logger.getLogger(this::class.java.name)

    data class LoginResp(val status: EnumLoginStatus, val registeredName: String = "")
}
