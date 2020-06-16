package simulator.model.game

import de.flunkyteam.endpoints.projects.simulator.Player

data class Player(val name: String,
                  val abgegeben: Boolean = false,
                  val team : Team = Team.Spectator,
                  val wonGames: Int = 0
) {

    fun toGRPC() = Player.newBuilder()
        .setName(name)
        .setAbgegeben(abgegeben)
        .setWonGames(wonGames.toLong())
        .build()
}