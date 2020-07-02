package simulator.model

private var ids = 1

/***
 * Represents a client eg. an opened tab in browser.
 */
data class Client(
    val secret: String,
    val players: List<Player> = listOf(),
    val id: Int = ids++
) {

}
