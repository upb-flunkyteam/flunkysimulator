package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumTeams
import de.flunkyteam.endpoints.projects.simulator.EnumThrowStrength
import kotlinx.event.event
import simulator.model.GameState
import simulator.model.Player
import simulator.model.RoundState
import simulator.model.Team
import kotlin.random.Random


// todo decide: error = gamestate does not change?


class GameController {

    data class GameStateEvent(val state: GameState)

    val onNewGameState = event<GameStateEvent>()

    var gameState = GameState()
        private set(value) {
            onNewGameState(GameStateEvent(value))
            field = value
        }

    private val lastThrown = mutableMapOf<Team,Player>()

    fun throwBall(name: String, strength: EnumThrowStrength): Boolean {
        val state = gameState
        if (state.roundState.throwingPlayer == null || name != state.roundState.throwingPlayer.name)
            return false

        val player = state.roundState.throwingPlayer

        val throwingTeam = state.getTeamOfPlayer(player) ?: return false

        //TODO actual throw with video, calculations and shit

        lastThrown[throwingTeam] = player

        val otherTeam = state.getOtherTeam(throwingTeam)
        val otherTeamPreviousThrower = if (lastThrown.containsKey(otherTeam)) lastThrown[otherTeam] else null
        val nextThrowingPlayer = otherTeam.getNextThrowingPlayer(otherTeamPreviousThrower)

        updateThrowingPlayer(nextThrowingPlayer)

        return true
    }

    fun forceThrowingPlayer(name: String): Boolean {
        val player = gameState.getPlayer(name)?: return false

        updateThrowingPlayer(player)

        return true
    }

    fun resetGameAndShuffleTeams(): Boolean {

        val (newPlayers1, newPlayers2) = (gameState.TeamA.players + gameState.TeamB.players)
            .map { p -> p.copy(abgegeben = false) }
            .shuffleSplitList()

        // without this random bool one team would always be the larger one
        val randBool = Random.nextBoolean()
        val teamA = Team(if (randBool) newPlayers1 else newPlayers2)
        val teamB = Team(if (!randBool) newPlayers1 else newPlayers2)

        //determine starting team
        val startingTeam = when {
            teamA.playerCount() > newPlayers2.count() -> teamB
            teamB.playerCount() < newPlayers2.count() -> teamA
            Random.nextBoolean() -> teamA
            else -> teamB
        }

        lastThrown.clear()

        gameState = GameState(
            TeamA = teamA,
            TeamB = teamB,
            Spectators = gameState.Spectators,
            roundState = RoundState(
                throwingPlayer = startingTeam.getNextThrowingPlayer(null)
            )
        )

        return true
    }

    fun registerPlayer(name: String): Boolean {
        if (name.isEmpty())
            return false
        // TODO return error

        if (gameState.nameTaken(name))
            return false
        // todo error message, regular negative resp?

        val player = Player(name)

        gameState = gameState.addOrMoveToSpectator(player)

        return true
    }

    fun removePlayer(target: String): Boolean {
        return gameState.getPlayer(target)?.let { player -> gameState = gameState.removePlayer(player)
            return true}?: false

    }

    fun switchTeam(name: String, team: EnumTeams): Boolean {
        val player = gameState.getPlayer(name) ?: return false

        return when(team){
            EnumTeams.SPECTATOR_TEAMS -> {
                gameState = gameState.addOrMoveToSpectator(player)
                true
            }
            EnumTeams.TEAM_A_TEAMS -> {
                gameState = gameState.addOrMoveToTeamA(player)
                true
            }
            EnumTeams.TEAM_B_TEAMS -> {
                gameState = gameState.addOrMoveToTeamB(player)
                true
            }
            else -> false
        }

    }

    private fun updateThrowingPlayer(player: Player?){
        gameState = gameState.copy(roundState = gameState.roundState.copy(throwingPlayer = player))
    }

    private fun <E> List<E>.shuffleSplitList(): Pair<List<E>, List<E>> {
        val shuffled = this.shuffled()

        return shuffled.filterIndexed(predicate = { index, _ -> index % 2 == 1 }).toList() to
                shuffled.filterIndexed(predicate = { index, _ -> index % 2 == 0 }).toList()
    }
}

