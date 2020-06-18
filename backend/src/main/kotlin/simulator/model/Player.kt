package simulator.model

import de.flunkyteam.endpoints.projects.simulator.Player
import simulator.model.game.Team

data class Player(val name: String,
                  var team : Team = Team.Spectator,
                  var wonGames: Int = 0
) {

    fun toGRPC() = Player.newBuilder()
        .setName(name)
        .setWonGames(wonGames.toLong())
        .build()
}