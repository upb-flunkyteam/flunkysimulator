package simulator.control

import de.flunkyteam.endpoints.projects.simulator.EnumTeams
import de.flunkyteam.endpoints.projects.simulator.EnumThrowStrength
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import simulator.shuffleSplitList
import simulator.model.game.*
import simulator.model.video.VideoInstructions
import simulator.model.video.VideoType
import kotlin.concurrent.withLock
import kotlin.random.Random
import kotlinx.coroutines.launch


class GameController(
    private val videoController: VideoController,
    private val messageController: MessageController
) :
    EventControllerBase<GameController.GameStateEvent>() {

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
            if (state.roundState.throwingPlayer == null || name != state.roundState.throwingPlayer)
                return false

            val player = gameState.getPlayer(state.roundState.throwingPlayer) ?: return false

            val throwingTeam = player.team
            val teamAThrows = throwingTeam == Team.A

            val throwingTime = 2.5
            val closeMissProbability = 0.15

            val videosToPlay = mutableListOf<VideoInstructions>()

            val probability: Double
            val minimumDrinkingTime: Double
            val maximumDrinkingTime: Double

            when (strength) {
                EnumThrowStrength.SOFT_THROW_STRENGTH -> {
                    probability = 0.666
                    minimumDrinkingTime = 3.0
                    maximumDrinkingTime = 3.0
                }
                EnumThrowStrength.MEDIUM_THROW_STRENGTH -> {
                    probability = 0.5
                    minimumDrinkingTime = 3.0
                    maximumDrinkingTime = 5.0
                }
                EnumThrowStrength.HARD_THROW_STRENGTH -> {
                    probability = 0.3
                    minimumDrinkingTime = 5.0
                    maximumDrinkingTime = 8.333
                }
                else -> return false
            }
            val hit = if (Math.random() < probability) {
                videosToPlay += VideoInstructions(
                    VideoType.Hit,
                    mirrored = teamAThrows
                )
                val runningTime = (throwingTime + minimumDrinkingTime +
                        Math.random() * (maximumDrinkingTime - minimumDrinkingTime)) * 1000
                videosToPlay += VideoInstructions(VideoType.Stop, runningTime.toLong())
                true
            } else {
                videosToPlay += if (Math.random() < closeMissProbability) {
                    VideoInstructions(
                        VideoType.CloseMiss,
                        mirrored = teamAThrows
                    )
                } else {
                    VideoInstructions(VideoType.Miss, mirrored = teamAThrows)
                }
                false
            }
            videoController.playVideos(videosToPlay)

            lastThrowingPlayer[throwingTeam] = player

            val otherTeam = throwingTeam.getOtherTeam()
            val nextThrowingPlayer = gameState.getNextThrowingPlayer(otherTeam)

            updateThrowingPlayer(nextThrowingPlayer)

            GlobalScope.launch {
                delay(10 * 1000)
                if (hit) {
                    messageController.sendMessage(player.name, "hat für Team ${throwingTeam.name} getroffen.")
                } else {
                    messageController.sendMessage(player.name, "hat nicht für Team ${throwingTeam.name} getroffen.")
                }
            }

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
                    val newCount = gameState.strafbiereA + diff
                    if (newCount < 0)
                        false
                    else {
                        gameState = gameState.copy(strafbiereA = newCount)
                        true
                    }
                }
                EnumTeams.TEAM_B_TEAMS -> {
                    val newCount = gameState.strafbiereB + diff
                    if (newCount < 0)
                        false
                    else {
                        gameState = gameState.copy(strafbiereB = newCount)
                        true
                    }
                }
                else -> false
            }
        }
    }

    fun resetGameAndShuffleTeams(): Boolean {
        gameStateLock.withLock {
            val (newPlayers1, newPlayers2) = gameState.players
                .map { p -> p.copy(abgegeben = false) }
                .shuffleSplitList()

            // without this random bool one team would always be the larger one
            val randBool = Random.nextBoolean()
            val teamA = if (randBool) newPlayers1 else newPlayers2
            val teamB = if (!randBool) newPlayers1 else newPlayers2

            // determine starting team
            val startingTeam = when {
                teamA.count() > newPlayers2.count() -> teamB
                teamB.count() < newPlayers2.count() -> teamA
                Random.nextBoolean() -> teamA
                else -> teamB
            }

            lastThrowingPlayer.clear()

            gameState = GameState(
                roundState = RoundState(
                    throwingPlayer = startingTeam.firstOrNull()?.name
                ),
                players = teamA.map { p -> p.copy(team = Team.A) }
                        + teamB.map { p -> p.copy(team = Team.B) }
            )

            videoController.playVideos(
                listOf(
                    VideoInstructions(VideoType.Setup),
                    VideoInstructions(
                        VideoType.Setup,
                        delay = 5 * 1000,
                        mirrored = true
                    )
                )
            )

            return true
        }
    }

    fun registerPlayer(name: String): Boolean {
        if (name.isEmpty())
            return false

        GlobalScope.launch { videoController.refreshVideos() }

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
            val newGameState = gameState.removePlayer(player)
            if (newGameState.roundState.throwingPlayer == player.name)
                newGameState.copy(roundState = RoundState())
            gameState = newGameState
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

    fun setAbgegeben(name: String, abgegeben: Boolean): Boolean {
        gameStateLock.withLock {
            val player = gameState.getPlayer(name) ?: return false
            gameState = gameState.updatePlayer(player.copy(abgegeben = abgegeben))
            return true
        }
    }

    private fun updateThrowingPlayer(player: Player?) {
        gameState = gameState.copy(roundState = gameState.roundState.copy(throwingPlayer = player?.name ?: ""))
    }

    private fun GameState.getNextThrowingPlayer(team: Team): Player? {
        val previousThrower = if (lastThrowingPlayer.containsKey(team))
            lastThrowingPlayer[team]
        else
            return this.getTeam(team).firstOrNull()

        val inGamePlayersWithIndex = players
            .mapIndexed { index, player -> player to index }
            .filter { p -> p.first.team == team && !p.first.abgegeben }

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

