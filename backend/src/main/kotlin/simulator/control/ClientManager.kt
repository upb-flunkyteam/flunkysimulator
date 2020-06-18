package simulator.control

import simulator.model.Client
import simulator.model.Player

class ClientManager {


    fun getOwner(player: Player): Client{
        //TODO
        return Client()
    }

    fun registerPlayer(player: Player, client: Client){
        //TODO

    }

    fun removePlayer(player: Player){
        //TODO

    }

    fun getConnectionStatus(player: Player): ConnectionStatus{
        //TODO
        return ConnectionStatus.Connected
    }

    enum class ConnectionStatus{Connected, NotConnected, Unknown}

}