package simulator.model

import de.flunkyteam.endpoints.projects.simulator.EnumConnectionStatus
import de.flunkyteam.endpoints.projects.simulator.Player
import simulator.control.ClientManager
import simulator.model.game.Team
import simulator.model.game.toGrpc

data class Player(val name: String,
                  var team : Team = Team.Spectator,
                  var wonGames: Int = 0
) {

    fun toGRPC(clientManager: ClientManager) = Player.newBuilder()
        .setName(name)
        .setWonGames(wonGames.toLong())
        .setConnectionStatus(clientManager.getConnectionStatus(this))
        .setTeam(team.toGrpc())
        .build()
}