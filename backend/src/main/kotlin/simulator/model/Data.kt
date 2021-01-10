package simulator.model

import simulator.model.game.Player
import java.util.concurrent.locks.ReentrantLock


class Data (playerList: List<Player> = listOf()){
    val writeLock = ReentrantLock()

    var playerList = ObserveableData<List<Player>>(playerList)
}