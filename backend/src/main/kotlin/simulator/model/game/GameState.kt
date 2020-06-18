package simulator.model.game

import de.flunkyteam.endpoints.projects.simulator.EnumRoundPhase
import simulator.control.PlayerManager
import simulator.model.Player


data class GameState(
    val throwingPlayer: String? = null,
    val roundPhase: EnumRoundPhase = EnumRoundPhase.NO_ACTIVE_GAME_PHASE,
    val strafbiereA: Int = 0,
    val strafbiereB: Int = 0,
    val abgegeben: List<Player> = emptyList(),
    val ruleConfig: RuleConfig = RuleConfig()
) {


    fun getStrafbier(team: Team) = when (team) {
        Team.A -> strafbiereA
        Team.B -> strafbiereB
        else -> -1
    }

    fun getAbgegeben(player: Player) = this.abgegeben.contains(player)

    fun setAbgegeben(player: Player, value: Boolean) = if (value)
        this.copy(abgegeben = abgegeben.plus(player))
    else
        this.copy(abgegeben = abgegeben.minus(player))


    fun setRoundPhase(value: EnumRoundPhase): GameState = this.copy(roundPhase = value)

    fun setThrowingPlayer(name: String?): GameState = this.copy(throwingPlayer = name)

    fun toGRPC(playerManager: PlayerManager) =
        de.flunkyteam.endpoints.projects.simulator.GameState.newBuilder()
            .setThrowingPlayer(throwingPlayer ?: "")
            .addAllPlayerTeamA(playerManager.TeamA.toGRPC())
            .setStrafbierTeamA(strafbiereA.toLong())
            .addAllPlayerTeamB(playerManager.TeamB.toGRPC())
            .setStrafbierTeamB(strafbiereB.toLong())
            .addAllSpectators(playerManager.Spectators.toGRPC())
            .setRoundPhase(roundPhase)
            .setRuleConfig(ruleConfig.toGrpc())
            .build()

    private fun Iterable<Player>.toGRPC() =
        this.map { player -> player.toGRPC(getAbgegeben(player)) }
}


