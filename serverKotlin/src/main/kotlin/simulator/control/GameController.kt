package simulator.control

import de.flunkyteam.endpoints.projects.simulator.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import simulator.shuffleSplitList
import simulator.model.game.*
import simulator.model.video.VideoInstructions
import simulator.model.video.VideoType
import kotlin.concurrent.withLock
import kotlin.random.Random
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils.escapeHtml4
import simulator.model.game.GameState
import simulator.model.game.Player


class GameController(
    private val videoController: VideoController,
    private val messageController: MessageController,
    initGamestate: GameState = GameState()
) :
    EventControllerBase<GameController.GameStateEvent>() {

    data class GameStateEvent(val state: GameState)

    private val gameStateLock = handlerLock

    var gameState = initGamestate
        private set(value) {
            handlerLock.withLock {
                onEvent(GameStateEvent(value))
            }
            field = value
        }

    private val lastThrowingPlayer: MutableMap<Team, Player> = mutableMapOf()

    fun throwBall(name: String, strength: EnumThrowStrength): EnumThrowRespStatus {
        gameStateLock.withLock {
            val state = gameState

            // -- determining whether we are allowed to throw --

            if (state.throwingPlayer == null || name != state.throwingPlayer)
                return EnumThrowRespStatus.THROW_STATUS_NOT_THROWING_PLAYER

            val player = gameState.getPlayer(state.throwingPlayer)
                ?: return EnumThrowRespStatus.THROW_STATUS_NOT_THROWING_PLAYER

            if (state.roundPhase == EnumRoundPhase.RESTING_PHASE) {
                return EnumThrowRespStatus.THROW_STATUS_RESTING_PERIOD
            }


            // -- calculating the throw --

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
                else -> return EnumThrowRespStatus.THROW_STATUS_UNKNOWN
            }

            val hit = Math.random() < probability

            val runningTime = (throwingTime + minimumDrinkingTime +
                    Math.random() * (maximumDrinkingTime - minimumDrinkingTime)) * 1000

            val restingTime = state.ruleConfig.restingPeriodMilliseconds + if (hit) runningTime.toInt() else 0


            // -- make the result known to the world via video--

            if (hit) {
                videosToPlay += VideoInstructions(
                    VideoType.Hit,
                    mirrored = teamAThrows
                )
                videosToPlay += VideoInstructions(VideoType.Stop, runningTime.toLong())
            } else {
                videosToPlay += if (Math.random() < closeMissProbability) {
                    VideoInstructions(
                        VideoType.Miss, // remove close miss until we have more "riechen" videos
                        mirrored = teamAThrows
                    )
                } else {
                    VideoInstructions(VideoType.Miss, mirrored = teamAThrows)
                }
            }
            videoController.playVideos(videosToPlay)


            // -- next player and round phase handling --

            lastThrowingPlayer[throwingTeam] = player

            val otherTeam = throwingTeam.otherTeam()
            val nextThrowingPlayer = gameState.getNextThrowingPlayer(otherTeam)
            val nextThrowingPhase = otherTeam.toThrowingPhase()

            // launch coroutine which disables the resting period, writes the result in the log and sets the next player
            GlobalScope.launch {

                delay(restingTime.toLong())

                if (hit)
                    messageController.sendMessage(
                        player.name,
                        "hat für ${throwingTeam.positionalName()} getroffen."
                    )
                else
                    messageController.sendMessage(
                        player.name,
                        "hat nicht für ${throwingTeam.positionalName()} getroffen."
                    )

                gameState = gameState
                    .setThrowingPlayer(nextThrowingPlayer?.name)
                    .setRoundPhase(nextThrowingPhase)

                messageController.sendMessage(nextThrowingPlayer?.name ?: "Niemand", "ist mit werfen dran.")
            }

            gameState = gameState.setRoundPhase(EnumRoundPhase.RESTING_PHASE)
            return EnumThrowRespStatus.THROW_STATUS_SUCCESS
        }
    }

    fun forceThrowingPlayer(name: String): Boolean {
        gameStateLock.withLock {
            val player = gameState.getPlayer(name) ?: return false
            val playerTeam = gameState.getTeamOfPlayer(player) ?: return false
            val throwingPhase = playerTeam.toThrowingPhase()
            if (throwingPhase == EnumRoundPhase.UNKNOWN_PHASE) return false
            gameState = gameState.copy(throwingPlayer = player.name, roundPhase = throwingPhase)
            return true
        }
    }

    fun modifyStrafbierCount(team: EnumTeams, increment: Boolean): Boolean {
        gameStateLock.withLock {
            val diff = if (increment) 1 else -1

            return when (team) {
                EnumTeams.TEAM_A_TEAMS -> {
                    val newCount = gameState.strafbiereA + diff
                    if (newCount in 0..10) {
                        gameState = gameState.copy(strafbiereA = newCount)
                        true
                    } else
                        false
                }
                EnumTeams.TEAM_B_TEAMS -> {
                    val newCount = gameState.strafbiereB + diff
                    if (newCount in 0..10) {
                        gameState = gameState.copy(strafbiereB = newCount)
                        true
                    } else
                        false
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
                throwingPlayer = startingTeam.firstOrNull()?.name,
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

    data class LoginResp(val status: EnumLoginStatus, val registeredName: String = "")

    fun registerPlayer(name: String): LoginResp {
        if (name.isEmpty())
            return LoginResp(EnumLoginStatus.LOGIN_STATUS_EMPTY)

        GlobalScope.launch { videoController.refreshVideos() }

        val escapedAndTrimmedName = escapeHtml4(name.trim())

        gameStateLock.withLock {
            val player = Player(escapedAndTrimmedName)

            gameState = gameState.addPlayer(player)

            if (gameState.nameTaken(escapedAndTrimmedName))
                return LoginResp(EnumLoginStatus.LOGIN_STATUS_NAME_TAKEN, escapedAndTrimmedName)

            return LoginResp(EnumLoginStatus.LOGIN_STATUS_SUCCESS, escapedAndTrimmedName)
        }
    }


    fun removePlayer(target: String): Boolean {
        gameStateLock.withLock {
            val player = gameState.getPlayer(target) ?: return false
            val newGameState = gameState.removePlayer(player)
            if (newGameState.throwingPlayer == player.name)
                newGameState.copy(throwingPlayer = null)
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

    fun setAbgegeben(judgeName: String, targetName: String, abgegeben: Boolean): EnumAbgegebenRespStatus {
        gameStateLock.withLock {
            val player =
                gameState.getPlayer(targetName) ?: return EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_UNKNOWN_TARGET
            val judge = gameState.getPlayer(judgeName) ?: return EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_UNKNOWN_JUDGE

            if (abgegeben && player.team == judge.team)
                return EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_OWN_TEAM

            // check if team has won
            var newState = gameState.updatePlayer(player.copy(abgegeben = abgegeben))
            if (newState.getTeam(player.team).all { it.abgegeben } && newState.getStrafbier(player.team) == 0) {
                newState = newState
                    .setRoundPhase(
                        when (player.team) {
                            Team.A -> EnumRoundPhase.TEAM_A_WON_PHASE
                            Team.B -> EnumRoundPhase.TEAM_B_WON_PHASE
                            else -> return EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_ERROR
                        }
                    ).registerTeamWin(player.team)
            }

            gameState = newState
            return EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_SUCCESS
        }
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

    private fun Team.toThrowingPhase(): EnumRoundPhase {
        return when (this) {
            Team.A -> EnumRoundPhase.TEAM_A_THROWING_PHASE
            Team.B -> EnumRoundPhase.TEAM_B_THROWING_PHASE
            else -> EnumRoundPhase.UNKNOWN_PHASE
        }
    }

    // -- Debug functions
    fun hardReset() {
        gameStateLock.withLock {
            gameState = GameState()
        }
    }

    fun setRestingPeriod(milliseconds: Long) {
        gameStateLock.withLock {
            gameState = gameState.copy(
                ruleConfig = gameState.ruleConfig.copy(restingPeriodMilliseconds = milliseconds.toInt())
            )
        }
    }
}

