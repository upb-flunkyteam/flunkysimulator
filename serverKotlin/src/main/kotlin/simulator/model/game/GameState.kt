package simulator.model.game


data class GameState(
    val players: List<Player> = emptyList(),
    val roundState: RoundState = RoundState(),
    val strafbiereA: Int = 0,
    val strafbiereB: Int = 0,
    val isResting: Boolean = false,
    val ruleConfig: RuleConfig = RuleConfig()
) {

    val activePlayers: List<Player>
        get() = players.filter { p -> p.team == Team.A || p.team == Team.B }
    val TeamA: List<Player>
        get() = players.filter { p -> p.team == Team.A }
    val TeamB: List<Player>
        get() = players.filter { p -> p.team == Team.B }
    val Spectators: List<Player>
        get() = players.filter { p -> p.team == Team.Spectator }

    fun getTeam(team: Team) = when (team) {
        Team.A -> TeamA
        Team.B -> TeamB
        else -> Spectators
    }

    fun addPlayer(player: Player) = this.copy(players = players + player)

    fun updatePlayer(player: Player) = this.copy(players = players.map {
        if (it.name == player.name) player else it
    })

    fun removePlayer(player: Player): GameState = this.copy(players = players.filter { p -> p != player })

    fun nameTaken(name: String) = this.players.any { player -> player.name == name }

    fun getPlayer(name: String) = this.players.firstOrNull { player -> player.name == name }

    fun setResting(value: Boolean): GameState = this.copy(isResting = value)

    fun toGRPC() = de.flunkyteam.endpoints.projects.simulator.GameState.newBuilder()
        .setThrowingPlayer(roundState.throwingPlayer?: "")
        .addAllPlayerTeamA(TeamA.toGRPC())
        .setStrafbierTeamA(strafbiereA.toLong())
        .addAllPlayerTeamB(TeamB.toGRPC())
        .setStrafbierTeamB(strafbiereB.toLong())
        .addAllSpectators(Spectators.toGRPC())
        .setIsResting(isResting)
        .setRuleConfig(ruleConfig.toGrpc())
        .build()

    private fun Iterable<Player>.toGRPC() = this.map { player -> player.toGRPC() }
}


