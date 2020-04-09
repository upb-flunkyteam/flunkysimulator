package simulator.model


data class GameState(
    val TeamA: Team = Team(),
    val TeamB: Team = Team(),
    val Spectators: List<Player> = emptyList(),
    val roundState: RoundState = RoundState()
) {
    val players: Sequence<Player>
        get() = Spectators.asSequence() + TeamA.players.asSequence() + TeamB.players.asSequence()

    fun addOrMoveToSpectator(player: Player): GameState = this.copy(
        Spectators = Spectators + player,
        TeamA = TeamA.removePlayer(player.name),
        TeamB = TeamB.removePlayer(player.name)
    )

    fun addOrMoveToTeamA(player: Player): GameState = this.copy(
        Spectators = Spectators.filter { p -> p.name != p.name },
        TeamA = TeamA.addPlayer(player),
        TeamB = TeamB.removePlayer(player.name)
    )

    fun addOrMoveToTeamB(player: Player): GameState = this.copy(
        Spectators = Spectators.filter { p -> p.name != p.name },
        TeamA = TeamA.removePlayer(player.name),
        TeamB = TeamB.addPlayer(player)
    )

    fun removePlayer(player: Player) = this.copy(
        Spectators = Spectators.filter { p -> p.name != p.name },
        TeamA = TeamA.removePlayer(player.name),
        TeamB = TeamB.removePlayer(player.name)
    )

    fun nameTaken(name: String) = this.players.any { player -> player.name == name}

    fun getPlayer(name: String) = this.players.firstOrNull { player -> player.name == name}

    fun getTeamOfPlayer(player: Player) = when {
        TeamA.contains(player) -> TeamA
        TeamB.contains(player) -> TeamB
        else -> null
    }

    fun getOtherTeam(team: Team) = if (team == TeamA) TeamB else TeamA

    fun toGRPC() = de.flunkyteam.endpoints.projects.simulator.GameState.newBuilder()
        .setThrowingPlayer(roundState.throwingPlayer?.name?:"")
        .addAllPlayerTeamA(TeamA.players.toGRPC())
        .setStrafbierTeamA(TeamA.strafbiere.toLong())
        .addAllPlayerTeamB(TeamB.players.toGRPC())
        .setStrafbierTeamB(TeamB.strafbiere.toLong())
        .addAllSpectators(Spectators.toGRPC())
        .build()

    private fun Iterable<Player>.toGRPC() = this.map { player -> player.toGRPC() }
}

