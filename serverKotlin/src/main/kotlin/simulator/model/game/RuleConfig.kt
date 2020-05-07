package simulator.model.game

import de.flunkyteam.endpoints.projects.simulator.RuleConfig

data class RuleConfig(val restingPeriodMilliseconds: Int = 4){

    fun toGrpc(): RuleConfig = RuleConfig.newBuilder()
        .setRestingPeriodLength(restingPeriodMilliseconds.toLong())
        .build()
}