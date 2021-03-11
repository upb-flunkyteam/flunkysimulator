package simulator.model

import kotlinx.event.event
import simulator.DeactiveableHandler
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

/**
 * Allows to listen on data changes
 */
open class ObserveableData<T>(initalValue: T) {
    private val handlerLock = ReentrantLock()

    private val onEvent = event<ChangeEvent<T>>()

    var value: T by Delegates.observable(initalValue)
    { property, oldValue, newValue ->
        handlerLock.withLock {
            onEvent(ChangeEvent(property, oldValue, newValue))
        }
    }

    fun addChangeListener(handler: (ChangeEvent<T>) -> Unit) {
        handlerLock.withLock { onEvent += handler }
    }

    fun removeChangeListener(handler: (ChangeEvent<T>) -> Unit) {
        handlerLock.withLock { onEvent -= handler }
    }
}

data class ChangeEvent<T>(val property: KProperty<*>, val oldValue: T, val newValue: T)