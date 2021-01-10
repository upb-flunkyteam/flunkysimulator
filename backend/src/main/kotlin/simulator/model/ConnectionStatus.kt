package simulator.model

import de.flunkyteam.endpoints.projects.simulator.EnumConnectionStatus

enum class ConnectionStatus {
    Connected, Disconnected
}

fun ConnectionStatus.toGrpc() = when(this){
    ConnectionStatus.Connected -> EnumConnectionStatus.CONNECTION_CONNECTED
    ConnectionStatus.Disconnected -> EnumConnectionStatus.CONNECTION_DISCONNECTED
}

fun EnumConnectionStatus.toKotlin() = when(this){
    EnumConnectionStatus.CONNECTION_CONNECTED -> ConnectionStatus.Connected
    EnumConnectionStatus.CONNECTION_DISCONNECTED -> ConnectionStatus.Disconnected
    else -> ConnectionStatus.Disconnected
}
