package simulator.control

import kotlinx.event.event
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

open class EventControllerBase<Event> {
    protected val handlerLock = ReentrantLock()
    protected val onEvent = event<Event>()

    fun addEventHandler(handler: (Event) -> Unit) {
        handlerLock.withLock { onEvent += handler }
    }

    fun removeEventHandler(handler: (Event) -> Unit) {
        handlerLock.withLock { onEvent -= handler }
    }
}