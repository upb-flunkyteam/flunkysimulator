package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumTeams
import simulator.model.Data
import simulator.model.Player
import simulator.model.game.Team
import simulator.model.game.toKotlin
import simulator.shuffleSplitList
import kotlin.concurrent.withLock
import kotlin.random.Random

class PlayerController(
    private val data: Data
) : EventControllerBase<PlayerController.PlayersEvent>() {

    data class PlayersEvent(val players: List<Player>)

    private val playerListLock = handlerLock

    var players: List<Player>
    get() = data.playerList.value
    set(value) {data.playerList.value=value}

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

    init {
        data.playerList.addChangeListener { changeEvent ->
            playerListLock.withLock {
                this.onEvent(PlayersEvent(changeEvent.newValue))
            }
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
                players = players + player
                player to true
            }
        }

    fun removePlayer(name: String?): Boolean {
        playerListLock.withLock {
            val player = getPlayer(name) ?: return false
            players = players - player
            return true
        }
    }

    internal fun setPlayerTeam(name: String, team: EnumTeams): Boolean {
        playerListLock.withLock {
            val player = getPlayer(name) ?: return false
            val newPlayer = player.copy(team = team.toKotlin())

            players = players - player + newPlayer


            return true
        }
    }

    internal fun registerTeamWin(team: Team) {
        players = players.map {
            if (it.team == team)
                it.copy(wonGames = it.wonGames+1)
            else
                it
        }
    }


    internal fun shuffleTeams() {
        playerListLock.withLock {
            val (newPlayers1, newPlayers2) = players.shuffleSplitList()

            // without this random bool one team would always be the larger one
            val randBool = Random.nextBoolean()
            val teamA = if (randBool) newPlayers1 else newPlayers2

            players = players.map { it.copy(team = if (teamA.contains(it)) Team.A else Team.B) }
        }
    }

    internal fun reset() {
        players = listOf()
    }
}