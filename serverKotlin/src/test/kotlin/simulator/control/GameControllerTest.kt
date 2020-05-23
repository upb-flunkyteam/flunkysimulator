package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumAbgegebenRespStatus
import de.flunkyteam.endpoints.projects.simulator.EnumRoundPhase
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.Assert
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import simulator.model.game.GameState
import simulator.model.game.Player
import simulator.model.game.Team


internal class GameControllerTest {

    @Test
    fun `detect winning team on abgabe`() {

        val hans = Player("hans", team = Team.B, abgegeben = false)
        val state = GameState(
            players = listOf(
                Player("peter0", team = Team.A, abgegeben = true),
                Player("peter1", team = Team.A, abgegeben = false),
                Player("peter2", team = Team.B, abgegeben = true),
                Player("peter3", team = Team.B, abgegeben = true),
                hans
            ),
            throwingPlayer = hans.name,
            roundPhase = EnumRoundPhase.TEAM_A_THROWING_PHASE
        )


        val messageController = mockk<MessageController>()
        val videoController = mockk<VideoController>()

        val gameController = GameController(videoController, messageController, state)

        val abgegebenResult = gameController.setAbgegeben("peter0", hans.name, true)

        assertEquals(EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_SUCCESS, abgegebenResult)
        assertEquals(EnumRoundPhase.TEAM_B_WON_PHASE, gameController.gameState.roundPhase)
    }

    @Test
    fun `regular abgabe`() {

        val hans = Player("hans", team = Team.B, abgegeben = false)
        val state = GameState(
            players = listOf(
                Player("peter0", team = Team.A, abgegeben = true),
                Player("peter1", team = Team.A, abgegeben = false),
                Player("peter2", team = Team.B, abgegeben = false),
                Player("peter3", team = Team.B, abgegeben = true),
                hans
            ),
            throwingPlayer = hans.name,
            roundPhase = EnumRoundPhase.TEAM_A_THROWING_PHASE
        )


        val messageController = mockk<MessageController>()
        val videoController = mockk<VideoController>()

        val gameController = GameController(videoController, messageController, state)

        assertEquals(
            EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_SUCCESS,
            gameController.setAbgegeben("peter0", hans.name, true)
        )

        assertEquals(
            state.copy(players = state.players.map {
                if (it == hans) {
                    hans.copy(abgegeben = true)
                } else {
                    it
                }
            }),
            gameController.gameState
        )
    }
}