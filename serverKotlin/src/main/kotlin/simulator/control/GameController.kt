package simulator.control

import kotlinx.event.event
import simulator.model.GameState
import simulator.model.Player


// todo decide: error = gamestate does not change?


class GameController {

    data class GameStateEvent(val state:GameState)

    val newGameStateEvent = event<GameStateEvent>()

    var state = GameState()
        private set(value) {
            newGameStateEvent(GameStateEvent(value))
            field = value
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
}

