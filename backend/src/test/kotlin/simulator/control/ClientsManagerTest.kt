package simulator.control

import io.mockk.mockk
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
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
}