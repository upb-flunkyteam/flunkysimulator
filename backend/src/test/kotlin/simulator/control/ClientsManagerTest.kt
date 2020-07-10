package simulator.control

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import simulator.model.Player

internal class ClientsManagerTest {


    @Test
    fun registerAndGetClient(){
        val clientManager = ClientsManager()

        val client = clientManager.registerClient { true }

        val client2 = clientManager.getClient(client.secret)

        assertEquals(client,client2)
    }

    @Test
    fun registerPlayerRejection() {

        val clientManager = ClientsManager()
        val client = clientManager.registerClient { true }
        val client2 = clientManager.registerClient { true }

        val player = Player("peter")

        assertTrue(clientManager.registerPlayerWithClient(player,client))

        assertFalse(clientManager.registerPlayerWithClient(player,client2))
    }


    @Test
    fun registerPlayerDeadClientKick() {

        val clientManager = ClientsManager()
        val client = clientManager.registerClient { false }
        val client2 = clientManager.registerClient { true }

        val player = Player("peter")

        assertTrue(clientManager.registerPlayerWithClient(player,client))

        assertTrue(clientManager.registerPlayerWithClient(player,client2))

        val deadClient = clientManager.getClient(client.secret)

        assertNull(deadClient)
    }
}