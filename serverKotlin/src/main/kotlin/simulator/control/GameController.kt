package simulator.control

import simulator.model.GameState
import simulator.model.Player

// todo decide: error = gamestate does not change?

class GameController {

    var state = GameState()

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

