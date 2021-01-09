package simulator.model

import java.util.concurrent.locks.ReentrantLock


class Data (playerList: List<Player> = listOf()){
    val writeLock = ReentrantLock()

    var playerList = ObserveableData<List<Player>>(playerList)
}