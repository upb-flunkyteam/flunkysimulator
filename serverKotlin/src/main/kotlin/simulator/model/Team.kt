package simulator.model

import de.flunkyteam.endpoints.projects.simulator.EnumTeams

enum class Team {
    Spectator, A, B
}

fun Team.getOtherTeam() = when (this) {
    Team.A -> Team.B
    Team.B -> Team.A
    else -> throw IllegalArgumentException("There is no other Team for $this.")
}

fun EnumTeams.toKotlin() = when(this){
    EnumTeams.TEAM_A_TEAMS -> Team.A
    EnumTeams.TEAM_B_TEAMS -> Team.B
    else -> Team.Spectator
}