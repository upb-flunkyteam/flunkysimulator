package simulator.control

import kotlinx.event.event
import simulator.model.GameState
import simulator.model.Player
import simulator.model.Team


// todo decide: error = gamestate does not change?


class GameController {

    data class GameStateEvent(val state: GameState)

    val newGameStateEvent = event<GameStateEvent>()

    var state = GameState()
        private set(value) {
            newGameStateEvent(GameStateEvent(value))
            field = value
        }

    fun resetGameAndShuffleTeams() {
        val (newPlayersA,newPlayersB) = (state.TeamA.players + state.TeamB.players).shuffleSplitList()
        state = GameState(TeamA = Team(newPlayersA), TeamB = Team(newPlayersB),Spectators = state.Spectators)
    }

    fun registerPlayer(name: String) {
        if (name.isEmpty())
            return
        // TODO return error

        if (state.nameTaken(name))
            return
        // todo error message, regular negative resp?

        val player = Player(name)

        state = state.addOrMoveToSpectator(player)
    }

    fun removePlayer(target: String) {
        state.getPlayer(target)?.let { player -> state = state.removePlayer(player) }
    }

    private fun <E> List<E>.shuffleSplitList(): Pair<List<E>, List<E>> {
        val shuffled = this.shuffled()

        return shuffled.filterIndexed(predicate = { index, _ -> index % 2 == 1 }).toList() to
                shuffled.filterIndexed(predicate = { index, _ -> index % 2 == 0 }).toList()
    }
}

