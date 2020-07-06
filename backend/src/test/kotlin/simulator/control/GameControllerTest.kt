package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumAbgegebenRespStatus
import de.flunkyteam.endpoints.projects.simulator.EnumRoundPhase
import de.flunkyteam.endpoints.projects.simulator.EnumTeams
import io.mockk.mockk
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import simulator.model.game.GameState
import simulator.model.Player
import simulator.model.game.Team


internal class GameControllerTest {



    private fun gameController(
        state: GameState,
        players: List<Player>
    ): Pair<GameController, PlayerController> {
        val messageController = mockk<MessageController>()
        val videoController = mockk<VideoController>()
        val clientManager = mockk<ClientManager>()
        val playerController = PlayerController(clientManager,players.toMutableList())
        val gameController = GameController(videoController, messageController, playerController, state)
        playerController.init (gameController::handleRemovalOfPlayerFromTeamAndUpdate)
        return (gameController to playerController)
    }

    @Test
    fun `detect winning team on abgabe`() {

        val hans = Player("hans", team = Team.B)
        val player1 = Player("peter1", team = Team.A)
        val player2 = Player("peter2", team = Team.A)
        val player3 = Player("peter3", team = Team.B)

        val players = listOf(hans, player1, player2, player3)

        val state = GameState(
            abgegeben = listOf(player1.name, player3.name),
            throwingPlayer = hans.name,
            roundPhase = EnumRoundPhase.TEAM_A_THROWING_PHASE
        )


        val (gameController,_) = gameController(state, players)

        val abgegebenResult = gameController.setAbgegeben("peter1", hans.name, true)

        assertEquals(EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_SUCCESS, abgegebenResult)
        assert(gameController.gameState.getAbgegeben(hans.name))
        assertEquals(EnumRoundPhase.TEAM_B_WON_PHASE, gameController.gameState.roundPhase)
    }

    @Test
    fun `regular abgabe`() {

        val hans = Player("hans", team = Team.B)
        val player1 = Player("peter1", team = Team.A)
        val player2 = Player("peter2", team = Team.A)
        val player3 = Player("peter3", team = Team.B)

        val players = listOf(hans, player1, player2, player3)

        val state = GameState(
            abgegeben = listOf(player1.name),
            throwingPlayer = hans.name,
            roundPhase = EnumRoundPhase.TEAM_A_THROWING_PHASE
        )

        val (gameController,_) = gameController(state,players)

        assertEquals(
            EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_SUCCESS,
            gameController.setAbgegeben("peter1", hans.name, true)
        )

        assert(gameController.gameState.getAbgegeben(hans.name))
    }

    @Test
    suspend fun `move player to spectator`() {

        val hans = Player("hans", team = Team.B)
        val gameState = GameState(abgegeben = listOf(hans.name))

        val (gameController,playerController) = gameController( gameState, listOf(hans))

        assertEquals(
            true,
            playerController.setPlayerTeam("hans", EnumTeams.SPECTATOR_TEAMS)
        )
        assertEquals(Team.Spectator,playerController.getPlayer("hans")?.team)
        delay(1000) //wait for update coroutine to finish
        assertFalse(gameController.gameState.getAbgegeben(hans.name))
    }
}