package simulator.model.game

import de.flunkyteam.endpoints.projects.simulator.EnumRoundPhase
import de.flunkyteam.endpoints.projects.simulator.EnumTeams


data class GameState(
    val players: List<Player> = emptyList(),
    val throwingPlayer: String? = null,
    val roundPhase: EnumRoundPhase = EnumRoundPhase.NO_ACTIVE_GAME_PHASE,
    val strafbiereA: Int = 0,
    val strafbiereB: Int = 0,
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

    fun getPlayer(name: String) = this.players.firstOrNull { player -> player.name == name }

    fun getTeamOfPlayer(player: Player) = when{
        TeamA.contains(player) -> Team.A
        TeamB.contains(player) -> Team.B
        Spectators.contains(player) -> Team.Spectator
        else -> null
    }

    fun addPlayer(player: Player) = this.copy(players = players + player)

    fun updatePlayer(player: Player) = this.copy(players = players.map {
        if (it.name == player.name) player else it
    })

    fun removePlayer(player: Player): GameState = this.copy(players = players.filter { p -> p != player })

    fun nameTaken(name: String) = this.players.any { player -> player.name == name }

    fun setRoundPhase(value: EnumRoundPhase): GameState = this.copy(roundPhase = value)

    fun setThrowingPlayer(name: String?): GameState = this .copy(throwingPlayer = name)

    fun toGRPC() = de.flunkyteam.endpoints.projects.simulator.GameState.newBuilder()
        .setThrowingPlayer(throwingPlayer?: "")
        .addAllPlayerTeamA(TeamA.toGRPC())
        .setStrafbierTeamA(strafbiereA.toLong())
        .addAllPlayerTeamB(TeamB.toGRPC())
        .setStrafbierTeamB(strafbiereB.toLong())
        .addAllSpectators(Spectators.toGRPC())
        .setRoundPhase(roundPhase)
        .setRuleConfig(ruleConfig.toGrpc())
        .build()

    private fun Iterable<Player>.toGRPC() = this.map { player -> player.toGRPC() }
}


