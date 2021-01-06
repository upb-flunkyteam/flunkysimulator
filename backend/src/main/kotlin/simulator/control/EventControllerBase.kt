package simulator.control

import kotlinx.event.event
import simulator.DeactiveableHandler
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

open class EventControllerBase<Event> {
    protected val handlerLock = ReentrantLock()
    protected val onEvent = event<Event>()

    fun addEventHandler(handler: (Event) -> Unit) {
        handlerLock.withLock { onEvent += handler }
    }

    fun addEventHandler(handler:DeactiveableHandler<Event>){
        handlerLock.withLock { onEvent += handler::doAction }
    }

    fun removeEventHandler(handler: (Event) -> Unit) {
        handlerLock.withLock { onEvent -= handler }
    }

    fun triggerWithLock(e: Event){
        handlerLock.withLock { onEvent(e) }
    }
}