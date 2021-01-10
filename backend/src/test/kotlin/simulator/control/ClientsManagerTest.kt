package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumTeams
import io.mockk.mockk
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import simulator.model.Data
import simulator.model.game.Player

internal class ClientsManagerTest {


    @Test
    fun registerAndGetClient(){
        val playerController = mockk<PlayerController>()
        val clientManager = ClientsManager(Data(),playerController)

        val client = clientManager.registerClient { true }

        val client2 = clientManager.getClient(client.secret)

        assertEquals(client,client2)
    }

    @Test
    fun registerPlayerRejection() {
        val player = Player("peter")
        val playerController = mockk<PlayerController>()
        val clientManager = ClientsManager(Data(listOf(player)),playerController)
        val client = clientManager.registerClient { true }
        val client2 = clientManager.registerClient { true }


        assertTrue(clientManager.registerPlayerWithClient(player.name,clientManager.getClient(client.secret)!!))

        assertFalse(clientManager.registerPlayerWithClient(player.name,clientManager.getClient(client2.secret)!!))
    }


    @Test
    fun registerPlayerDeadClientKick() {
        val player = Player("peter")

        val playerController = mockk<PlayerController>()
        val clientManager = ClientsManager(Data(listOf(player)),playerController)
        val client = clientManager.registerClient { false }
        val client2 = clientManager.registerClient { true }


        assertTrue(clientManager.registerPlayerWithClient(player.name,client))

        assertTrue(clientManager.registerPlayerWithClient(player.name,client2))

        val deadClient = clientManager.getClient(client.secret)

        assertNull(deadClient)
    }

    @Test
    fun switchTeamWithTwoPlayers(){
        val player1 = Player("p1")
        val player2 = Player("p2")

        val data = Data(listOf(player1,player2))

        val playerController = PlayerController(data)
        val clientsManager = ClientsManager(data, playerController)

        val clientSecret = clientsManager.registerClient { true }.secret

        fun client() = clientsManager.getClient(clientSecret)!!

        assertTrue(clientsManager.registerPlayerWithClient(player1.name,client()))
        assertTrue(clientsManager.registerPlayerWithClient(player2.name,client()))

        assertTrue(client().players.contains(player1.name))
        assertTrue(client().players.contains(player2.name))

        playerController.setPlayerTeam(player1.name,EnumTeams.TEAM_A_TEAMS)

        Thread.sleep(1_000)

        assertTrue(client().players.contains(player1.name))
        assertTrue(client().players.contains(player2.name))
    }
}