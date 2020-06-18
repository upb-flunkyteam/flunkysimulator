package simulator.model.game

import de.flunkyteam.endpoints.projects.simulator.EnumRoundPhase
import simulator.control.PlayerController
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

    fun toGRPC(playerController: PlayerController) =
        de.flunkyteam.endpoints.projects.simulator.GameState.newBuilder()
            .setThrowingPlayer(throwingPlayer ?: "")
            .setStrafbierTeamA(strafbiereA.toLong())
            .setStrafbierTeamB(strafbiereB.toLong())
            .setRoundPhase(roundPhase)
            .setRuleConfig(ruleConfig.toGrpc())
            .build()

    private fun Iterable<Player>.toGRPC() =
        this.map { player -> player.toGRPC(getAbgegeben(player)) }
}


