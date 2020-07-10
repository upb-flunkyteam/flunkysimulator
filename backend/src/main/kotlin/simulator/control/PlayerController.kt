package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumTeams
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import simulator.model.Player
import simulator.model.game.Team
import simulator.model.game.toKotlin
import simulator.shuffleSplitList
import kotlin.concurrent.withLock
import kotlin.random.Random


class PlayerController(
    private val players: MutableList<Player> = mutableListOf()
) : EventControllerBase<PlayerController.PlayersEvent>() {

    data class PlayersEvent(val updateOf: Set<Team>)

    private val playerListLock = handlerLock

    private lateinit var handleRemovalOfPlayerCoroutine: (String) -> Unit

    /**
     * @param handleRemovalOfPlayer Function is called when a player is removed from a team. This might be relevant for the game
     * state. eg. if they are the trowing player or having the abgegeben status.
     */
    fun init(handleRemovalOfPlayer: (String) -> Unit) {
        handleRemovalOfPlayerCoroutine = { p: String ->
            GlobalScope.launch {
                handleRemovalOfPlayer(p)
            }
        }
    }

    val allPlayers: List<Player>
        get() = players.toList()
    val activePlayers: List<Player>
        get() {
            playerListLock.withLock { return players.filter { p -> p.team == Team.A || p.team == Team.B } }
        }
    val TeamA: List<Player>
        get() {
            playerListLock.withLock { return players.filter { p -> p.team == Team.A } }
        }
    val TeamB: List<Player>
        get() {
            playerListLock.withLock { return players.filter { p -> p.team == Team.B } }
        }
    val Spectators: List<Player>
        get() {
            playerListLock.withLock { return players.filter { p -> p.team == Team.Spectator } }
        }


    private val all = setOf(Team.A, Team.B, Team.Spectator)
    fun triggerUpdate(of: Set<Team> = all) {
        playerListLock.withLock {
            this.onEvent(PlayersEvent(of))
        }
    }

    fun getPlayer(name: String?) = this.players.firstOrNull { player -> player.name == name }

    fun getTeam(team: Team) = when (team) {
        Team.A -> TeamA
        Team.B -> TeamB
        else -> Spectators
    }

    /***
     * @return player and isNew
     */
    fun createOrFindPlayer(name: String): Pair<Player, Boolean> =
        getPlayer(name)?.let { it to false } ?: run {
            val player = Player(name)
            playerListLock.withLock {
                players.add(player)
                player to true
            }
        }

    fun removePlayer(name: String?): Boolean {
        playerListLock.withLock {
            val player = getPlayer(name) ?: return false
            players.remove(player)
            triggerUpdate(setOf(player.team))
            handleRemovalOfPlayerCoroutine(player.name)
            return true
        }
    }

    internal fun setPlayerTeam(name: String, team: EnumTeams): Boolean {
        playerListLock.withLock {
            val player = getPlayer(name) ?: return false
            player.team = team.toKotlin()

            triggerUpdate(setOf(player.team))
            handleRemovalOfPlayerCoroutine(player.name)

            return true
        }
    }

    internal fun registerTeamWin(team: Team) = players.forEach {
        if (it.team == team)
            it.wonGames += 1

        this.triggerUpdate()
    }


    internal fun shuffleTeams() {
        playerListLock.withLock {
            val (newPlayers1, newPlayers2) = players.shuffleSplitList()

            // without this random bool one team would always be the larger one
            val randBool = Random.nextBoolean()
            val teamA = if (randBool) newPlayers1 else newPlayers2

            players.forEach { it.team = if (teamA.contains(it)) Team.A else Team.B }
            triggerUpdate()
        }
    }

    internal fun reset() {
        players.clear()
    }
}