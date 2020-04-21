package simulator

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