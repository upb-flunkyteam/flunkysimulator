package simulator.control

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

    val newGameStateEvent = event<GameStateEvent>()

    var gameState = GameState()
        private set(value) {
            newGameStateEvent(GameStateEvent(value))
            field = value
        }

    private val lastThrown = mutableMapOf<Team,Player>()

    fun throwBall(name: String, strength: EnumThrowStrength) {
        val state = gameState
        if (state.roundState.throwingPlayer == null || name != state.roundState.throwingPlayer.name)
            return

        val player = state.roundState.throwingPlayer

        val throwingTeam = state.getTeamOfPlayer(player) ?: return

        //TODO actual throw with video, calculations and shit

        lastThrown[throwingTeam] = player

        val otherTeam = state.getOtherTeam(throwingTeam)
        val otherTeamPreviousThrower = if (lastThrown.containsKey(otherTeam)) lastThrown[otherTeam] else null
        val nextThrowingPlayer = otherTeam.getNextThrowingPlayer(otherTeamPreviousThrower)

        updateThrowingPlayer(nextThrowingPlayer)

    }

    fun forceThrowingPlayer(name: String){
        val player = gameState.getPlayer(name)?: return

        updateThrowingPlayer(player)
    }

    private fun updateThrowingPlayer(player: Player?){
        gameState = gameState.copy(roundState = gameState.roundState.copy(throwingPlayer = player))
    }

    fun resetGameAndShuffleTeams() {

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
    }

    fun registerPlayer(name: String) {
        if (name.isEmpty())
            return
        // TODO return error

        if (gameState.nameTaken(name))
            return
        // todo error message, regular negative resp?

        val player = Player(name)

        gameState = gameState.addOrMoveToSpectator(player)
    }

    fun removePlayer(target: String) {
        gameState.getPlayer(target)?.let { player -> gameState = gameState.removePlayer(player) }
    }

    private fun <E> List<E>.shuffleSplitList(): Pair<List<E>, List<E>> {
        val shuffled = this.shuffled()

        return shuffled.filterIndexed(predicate = { index, _ -> index % 2 == 1 }).toList() to
                shuffled.filterIndexed(predicate = { index, _ -> index % 2 == 0 }).toList()
    }
}

