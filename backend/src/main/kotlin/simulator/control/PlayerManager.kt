package simulator.control

import org.springframework.web.util.HtmlUtils
import de.flunkyteam.endpoints.projects.simulator.*
import simulator.model.Player
import simulator.model.game.Team
import simulator.model.game.toKotlin
import simulator.shuffleSplitList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.random.Random


class PlayerManager(
    private val playersLock: ReentrantLock,
    private val triggerGamestateUpdate: () -> Unit,
    private val handleRemovalOfPlayerFromTeamAndUpdate:(Player) -> Unit,
    private val players: MutableList<Player> = mutableListOf()
) {


    val allPlayers: List<Player>
            get() = players.toList()
    val activePlayers: List<Player>
        get() = players.filter { p -> p.team == Team.A || p.team == Team.B }
    val TeamA: List<Player>
        get() = players.filter { p -> p.team == Team.A }
    val TeamB: List<Player>
        get() = players.filter { p -> p.team == Team.B }
    val Spectators: List<Player>
        get() = players.filter { p -> p.team == Team.Spectator }

    fun getPlayer(name: String?) = this.players.firstOrNull { player -> player.name == name }

    fun getTeam(team: Team) = when (team) {
        Team.A -> TeamA
        Team.B -> TeamB
        else -> Spectators
    }

    data class LoginResp(val status: EnumLoginStatus, val registeredName: String = "")

    private fun prepareNewName(name: String): String? {
        if (name.isEmpty())
            return null

        val escapedAndTrimmedName = HtmlUtils.htmlEscape(name.trim())
        return escapedAndTrimmedName
    }

    fun registerPlayer(name: String): LoginResp {
        val newName = prepareNewName(name)
            ?: return LoginResp(EnumLoginStatus.LOGIN_STATUS_EMPTY)

        playersLock.withLock {
            val player = Player(newName)

            if (players.any { it.name == newName })
                return LoginResp(EnumLoginStatus.LOGIN_STATUS_NAME_TAKEN, newName)
            else
                players.add(player)

            triggerGamestateUpdate()
            return LoginResp(EnumLoginStatus.LOGIN_STATUS_SUCCESS, newName)
        }
    }

    fun removePlayer(name: String?): Boolean {
        playersLock.withLock {
            val player = getPlayer(name) ?: return false
            players.remove(player)
            handleRemovalOfPlayerFromTeamAndUpdate(player)
            return true
        }
    }
    internal fun setPlayerTeam(name: String, team: EnumTeams): Boolean {
        playersLock.withLock {
            val player = getPlayer(name) ?: return false
            player.team = team.toKotlin()

            handleRemovalOfPlayerFromTeamAndUpdate(player)

            return true
        }
    }

    internal fun registerTeamWin(team: Team) = players.forEach {
        if (it.team == team)
            it.wonGames += 1

        this.triggerUpdateIfRequired()
    }

    private fun triggerUpdateIfRequired() {
        // the call to player manager was direct if there was no previous locking
        // therefore we need to make the changes known by ourself and can't trust the calling function
        // to do that for us
        if (playersLock.holdCount == 1) {
            this.triggerGamestateUpdate()
        }
    }


    internal fun shuffleTeams(): Pair<List<Player>, List<Player>> {
        val (newPlayers1, newPlayers2) = players.shuffleSplitList()

        // without this random bool one team would always be the larger one
        val randBool = Random.nextBoolean()
        val teamA = if (randBool) newPlayers1 else newPlayers2

        players.forEach { it.team = if(teamA.contains(it)) Team.A else Team.B }

        return (this.TeamA to this.TeamB)
    }

    internal fun reset() {
        players.clear()
    }

}