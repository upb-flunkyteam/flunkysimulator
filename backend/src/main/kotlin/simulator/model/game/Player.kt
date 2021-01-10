package simulator.model.game

import de.flunkyteam.endpoints.projects.simulator.Player
import simulator.control.ClientsManager
import simulator.model.ConnectionStatus
import simulator.model.toGrpc

data class Player(val name: String,
                  val team : Team = Team.Spectator,
                  val wonGames: Int = 0,
                  val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected
) {

    fun toGRPC(clientsManager: ClientsManager): Player = Player.newBuilder()
        .setName(name)
        .setWonGames(wonGames.toLong())
        .setConnectionStatus(connectionStatus.toGrpc())
        .setTeam(team.toGrpc())
        .build()
}

fun List<simulator.model.game.Player>.update(name:String, modification: (simulator.model.game.Player) -> simulator.model.game.Player) =
    this.map {
        if (it.name != name)
            it
        else
            modification(it)
    }

fun List<simulator.model.game.Player>.update(name: String, with:simulator.model.game.Player) = this.update(name) { with }