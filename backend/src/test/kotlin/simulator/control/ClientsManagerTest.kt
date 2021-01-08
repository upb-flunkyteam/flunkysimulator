package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumTeams
import io.mockk.mockk
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import simulator.model.Client
import simulator.model.Player

internal class ClientsManagerTest {


    @Test
    fun registerAndGetClient(){
        val playerController = mockk<PlayerController>()
        val clientManager = ClientsManager(playerController)

        val client = clientManager.registerClient { true }

        val client2 = clientManager.getClient(client.secret)

        assertEquals(client,client2)
    }

    @Test
    fun registerPlayerRejection() {
        val playerController = mockk<PlayerController>()
        val clientManager = ClientsManager(playerController)
        val client = clientManager.registerClient { true }
        val client2 = clientManager.registerClient { true }

        val player = Player("peter")

        assertTrue(clientManager.registerPlayerWithClient(player.name,client))

        assertFalse(clientManager.registerPlayerWithClient(player.name,client2))
    }


    @Test
    fun registerPlayerDeadClientKick() {
        val playerController = mockk<PlayerController>()
        val clientManager = ClientsManager(playerController)
        val client = clientManager.registerClient { false }
        val client2 = clientManager.registerClient { true }

        val player = Player("peter")

        assertTrue(clientManager.registerPlayerWithClient(player.name,client))

        assertTrue(clientManager.registerPlayerWithClient(player.name,client2))

        val deadClient = clientManager.getClient(client.secret)

        assertNull(deadClient)
    }

    @Test
    fun switchTeamWithTwoPlayers(){
        val player1 = Player("p1");
        val player2 = Player("p2");

        val playerController = PlayerController(mutableListOf(player1,player2))
        val clientsManager = ClientsManager(playerController)
        playerController.init {p ->
            clientsManager.removePlayer(p)
        }

        val client = clientsManager.registerClient { true };
        clientsManager.registerPlayerWithClient(player1.name,client)
        clientsManager.registerPlayerWithClient(player2.name,client)

        playerController.setPlayerTeam(player1.name,EnumTeams.TEAM_A_TEAMS);

        assertTrue(client.players.contains(player1.name))
        assertTrue(client.players.contains(player2.name))
    }
}