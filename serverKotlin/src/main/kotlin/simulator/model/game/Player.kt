package simulator.model.game

import de.flunkyteam.endpoints.projects.simulator.Player

data class Player(val name: String,
                  val abgegeben: Boolean = false,
                  val team : Team = Team.Spectator
) {

    fun toGRPC() = Player.newBuilder()
        .setName(name)
        .setAbgegeben(abgegeben)
        .build()
}