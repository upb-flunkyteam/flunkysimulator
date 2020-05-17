package simulator.model.game

import de.flunkyteam.endpoints.projects.simulator.EnumTeams

enum class Team {
    Spectator, A, B
}

fun Team.positionalName() = when(this){
    Team.A -> "Links"
    Team.B -> "Rechts"
    Team.Spectator -> "Zuschauer"
}

fun Team.otherTeam() = when (this) {
    Team.A -> Team.B
    Team.B -> Team.A
    else -> throw IllegalArgumentException("There is no other Team for $this.")
}

fun EnumTeams.toKotlin() = when(this){
    EnumTeams.TEAM_A_TEAMS -> Team.A
    EnumTeams.TEAM_B_TEAMS -> Team.B
    else -> Team.Spectator
}

fun EnumTeams.positionalName() = when(this){
    EnumTeams.TEAM_A_TEAMS -> "Links"
    EnumTeams.TEAM_B_TEAMS -> "Rechts"
    else -> "Zuschauer"
}