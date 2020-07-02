package simulator.control

import de.flunkyteam.endpoints.projects.simulator.Client
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import simulator.model.Player

internal class ClientManagerTest {


    @Test
    fun registerAndGetClient(){
        val clientManager = ClientManager()

        val client = clientManager.registerClient { true }

        val client2 = clientManager.getClient(client.secret)

        assertEquals(client,client2)
    }

    @Test
    fun registerPlayerRejection() {

        val clientManager = ClientManager()
        val client = clientManager.registerClient { true }
        val client2 = clientManager.registerClient { true }

        val player = Player("peter")

        assertTrue(clientManager.registerPlayer(player,client))

        assertFalse(clientManager.registerPlayer(player,client2))
    }


    @Test
    fun registerPlayerDeadClientKick() {

        val clientManager = ClientManager()
        val client = clientManager.registerClient { false }
        val client2 = clientManager.registerClient { true }

        val player = Player("peter")

        assertTrue(clientManager.registerPlayer(player,client))

        assertTrue(clientManager.registerPlayer(player,client2))

        val deadClient = clientManager.getClient(client.secret)

        assertNull(deadClient)
    }
}