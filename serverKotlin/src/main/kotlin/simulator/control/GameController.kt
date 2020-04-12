package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumTeams
import de.flunkyteam.endpoints.projects.simulator.EnumThrowStrength
import simulator.model.*
import kotlin.concurrent.withLock
import kotlin.random.Random


class GameController : EventController<GameController.GameStateEvent>() {

    data class GameStateEvent(val state: GameState)

    private val gameStateLock = handlerLock

    var gameState = GameState()
        private set(value) {
            handlerLock.withLock {
                onEvent(GameStateEvent(value))
            }
            field = value
        }

    private val lastThrowingPlayer: MutableMap<Team, Player> = mutableMapOf()

    fun throwBall(name: String, strength: EnumThrowStrength): Boolean {
        gameStateLock.withLock {
            val state = gameState
            if (state.roundState.throwingPlayer == null || name != state.roundState.throwingPlayer.name)
                return false

            val player = state.roundState.throwingPlayer

            val throwingTeam = player.team

            //TODO actual throw with video, calculations and shit

            lastThrowingPlayer[throwingTeam] = player

            val otherTeam = throwingTeam.getOtherTeam()
            val nextThrowingPlayer = gameState.getNextThrowingPlayer(otherTeam)

            updateThrowingPlayer(nextThrowingPlayer)

            return true
        }
    }

    fun forceThrowingPlayer(name: String): Boolean {
        gameStateLock.withLock {
            val player = gameState.getPlayer(name) ?: return false
            updateThrowingPlayer(player)
            return true
        }
    }

    fun modifyStrafbierCount(team: EnumTeams, increment: Boolean): Boolean {
        gameStateLock.withLock {
            val diff = if (increment) 1 else -1

            return when (team) {
                EnumTeams.TEAM_A_TEAMS -> {
                    gameState = gameState.copy(strafbiereA = gameState.strafbiereA + diff)
                    true
                }
                EnumTeams.TEAM_B_TEAMS -> {
                    gameState = gameState.copy(strafbiereB = gameState.strafbiereB + diff)
                    true
                }
                else -> false
            }
        }
    }

    fun resetGameAndShuffleTeams(): Boolean {
        gameStateLock.withLock {
            val (newPlayers1, newPlayers2) = gameState.activePlayers
                .map { p -> p.copy(abgegeben = false) }
                .shuffleSplitList()

            // without this random bool one team would always be the larger one
            val randBool = Random.nextBoolean()
            val teamA = if (randBool) newPlayers1 else newPlayers2
            val teamB = if (!randBool) newPlayers1 else newPlayers2

            //determine starting team
            val startingTeam = when {
                teamA.count() > newPlayers2.count() -> teamB
                teamB.count() < newPlayers2.count() -> teamA
                Random.nextBoolean() -> teamA
                else -> teamB
            }

            lastThrowingPlayer.clear()

            gameState = GameState(
                roundState = RoundState(
                    throwingPlayer = startingTeam.firstOrNull()
                ),
                players = gameState.Spectators
                        + teamA.map { p -> p.copy(team = Team.A) }
                        + teamB.map { p -> p.copy(team = Team.B) }
            )

            return true
        }
    }

    fun registerPlayer(name: String): Boolean {
        if (name.isEmpty())
            return false

        gameStateLock.withLock {
            if (gameState.nameTaken(name))
                return false

            val player = Player(name)

            gameState = gameState.addPlayer(player)

            return true
        }
    }

    fun removePlayer(target: String): Boolean {
        gameStateLock.withLock {
            val player = gameState.getPlayer(target) ?: return false
            gameState = gameState.removePlayer(player)
            return true
        }
    }

    fun switchTeam(name: String, team: EnumTeams): Boolean {
        gameStateLock.withLock {
            val player = gameState.getPlayer(name) ?: return false
            gameState = gameState.updatePlayer(player.copy(team = team.toKotlin()))
            return true
        }
    }


    private fun updateThrowingPlayer(player: Player?) {
        gameState = gameState.copy(roundState = gameState.roundState.copy(throwingPlayer = player))
    }

    private fun <E> List<E>.shuffleSplitList(): Pair<List<E>, List<E>> {
        val shuffled = this.shuffled()

        return shuffled.filterIndexed(predicate = { index, _ -> index % 2 == 1 }).toList() to
                shuffled.filterIndexed(predicate = { index, _ -> index % 2 == 0 }).toList()
    }

    private fun GameState.getNextThrowingPlayer(team: Team): Player? {
        val previousThrower = if (lastThrowingPlayer.containsKey(team))
            lastThrowingPlayer[team]
        else
            return this.getTeam(team).firstOrNull()

        val inGamePlayersWithIndex = players
            .mapIndexed { index, player -> player to index }
            .filter { p -> !p.first.abgegeben }

        if (!players.contains(previousThrower)) {
            return inGamePlayersWithIndex.firstOrNull()?.first
        }

        val indexOfLast = players.indexOf(previousThrower)

        if (inGamePlayersWithIndex.isEmpty())
            return null

        return (inGamePlayersWithIndex.firstOrNull { (_, i) -> i > indexOfLast }
            ?: inGamePlayersWithIndex.first())
            .first
    }
}

