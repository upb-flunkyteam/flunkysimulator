package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumTeams
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import simulator.model.Data
import simulator.model.game.Player
import simulator.model.game.Team

internal class PlayerControllerTest{

    @Test
    fun dryRun(){
        val playerController = PlayerController(Data())

        val res1 =  playerController.createOrFindPlayer("hans")
        assertEquals(
            Player("hans") to true,
            res1)

        val res2 = playerController.createOrFindPlayer("hans")
        assertEquals(
            Player("hans") to false,
            res2)

        playerController.createOrFindPlayer("lola")
        playerController.createOrFindPlayer("peter")
        playerController.createOrFindPlayer("anna")

        val hans = playerController.getPlayer("hans")
        assertNotNull(hans)
        val lola = playerController.getPlayer("lola")
        assertNotNull(lola)
        val peter = playerController.getPlayer("peter")
        assertNotNull(peter)
        val anna = playerController.getPlayer("anna")
        assertNotNull(anna)

        assertEquals(4,playerController.Spectators.size)

        playerController.setPlayerTeam("hans", EnumTeams.TEAM_A_TEAMS)
        playerController.setPlayerTeam("lola", EnumTeams.TEAM_A_TEAMS)
        playerController.setPlayerTeam("peter", EnumTeams.TEAM_B_TEAMS)
        playerController.setPlayerTeam("anna", EnumTeams.TEAM_B_TEAMS)

        assertEquals(2,playerController.TeamA.size)
        assertEquals(2,playerController.TeamB.size)

        playerController.removePlayer("anna")
        assertEquals(1,playerController.TeamB.size)

        playerController.registerTeamWin(Team.A)
        assertEquals(1, playerController.getPlayer("hans")!!.wonGames)
        assertEquals(1, playerController.getPlayer("lola")!!.wonGames)
        assertEquals(0, playerController.getPlayer("peter")!!.wonGames)
    }
}