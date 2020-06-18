package simulator

import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlin.random.Random

fun <E> List<E>.shuffleSplitList(): Pair<List<E>, List<E>> {
    val shuffled = this.shuffled()

    return shuffled.filterIndexed(predicate = { index, _ -> index % 2 == 1 }).toList() to
            shuffled.filterIndexed(predicate = { index, _ -> index % 2 == 0 }).toList()
}

fun <E> List<E>.getRandomElement(): E? = if (this.isEmpty())
    null
else
    this[Random.nextInt(this.size)]

fun String.removeFirstAndLast(): String = if (this.length <= 1)
    this
else
    this.subSequence(1, this.length-1).toString()

/***
 * action should put something in a responseObserver
 */
fun <Event> buildRegisterHandler(action: (Event) -> Unit): DeactiveableHandler<Event> =
    DeactiveableHandler({ event: Event,
                          deactiveableHandler: DeactiveableHandler<Event> ->
        try {
            //fails if stream is closed
            action(event)
        } catch (e: StatusRuntimeException) {
            if (e.status.code == Status.Code.CANCELLED) {
                println("Another stream bites the dust. Message: \n ${e.message}")
                deactiveableHandler.enabled = false
                /*TODO delete handlers when connection gone but not while iterating
                     through handlers like in this position, because this would casue
                     concurrency modification errors because of the underlying HashSet
                     in the Event plugin.
                     handler?.let { gameController.removeEventHandler(it) }
                     */
            } else
                throw e
        }
    })