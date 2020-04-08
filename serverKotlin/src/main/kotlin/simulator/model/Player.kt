package simulator.model

import de.flunkyteam.endpoints.projects.simulator.Player

data class Player(val name: String,
                  val abgegeben: Boolean = false) {

    fun toGRPC() = Player.newBuilder()
        .setName(name)
        .setAbgegeben(abgegeben)
        .build()
}