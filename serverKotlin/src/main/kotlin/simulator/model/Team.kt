package simulator.model

data class Team(val players: List<Player> = emptyList(),
                val strafbiere: Int = 0) : List<Player> by players {

    fun playerCount(): Int = players.count()

    fun removePlayer(player: Player) = this.copy(players = players.filter { p -> p != player})

    fun addPlayer(player: Player) = this.copy(players = players + player)

    fun getNextThrowingPlayer(previousThrower: Player?): Player? {
        val inGamePlayersWithIndex = players
            .mapIndexed { index, player -> player to index }
            .filter { p -> !p.first.abgegeben }

        if (!players.contains(previousThrower)){
            return inGamePlayersWithIndex.firstOrNull()?.first
        }

        val indexOfLast = players.indexOf(previousThrower)

        if (inGamePlayersWithIndex.isEmpty())
            return null

        return (inGamePlayersWithIndex.firstOrNull { (_,i) -> i > indexOfLast }
            ?: inGamePlayersWithIndex.first())
            .first
    }

}