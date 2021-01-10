package simulator.model

import de.flunkyteam.endpoints.projects.simulator.Player
import simulator.control.ClientsManager
import simulator.model.game.Team
import simulator.model.game.toGrpc

data class Player(val name: String,
                  val team : Team = Team.Spectator,
                  val wonGames: Int = 0
) {

    fun toGRPC(clientsManager: ClientsManager) = Player.newBuilder()
        .setName(name)
        .setWonGames(wonGames.toLong())
        .setConnectionStatus(clientsManager.getConnectionStatus(this))
        .setTeam(team.toGrpc())
        .build()
}