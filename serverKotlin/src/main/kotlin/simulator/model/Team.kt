package simulator.model

data class Team(val players: List<Player> = emptyList(),
                val nextThrowingPlayer: Player? = null,
                val strafbiere: Int = 0) {

     fun playerCount(): Int = players.size

    fun removePlayer(name: String) = this.copy(players = players.filter { player -> player.name != name})

    fun addPlayer(player: Player) = this.copy(players = players + player)
}