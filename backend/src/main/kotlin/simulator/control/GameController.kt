package simulator.control

import de.flunkyteam.endpoints.projects.simulator.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import simulator.model.game.*
import simulator.model.video.VideoInstructions
import simulator.model.video.VideoType
import kotlin.concurrent.withLock
import kotlin.random.Random
import kotlinx.coroutines.launch
import simulator.model.game.GameState
import simulator.model.Player


class GameController(
    private val videoController: VideoController,
    private val messageController: MessageController,
    private val playerController: PlayerController,
    initGamestate: GameState = GameState()
) :
    EventControllerBase<GameController.GameStateEvent>() {

    data class GameStateEvent(val state: GameState)

    private val gameStateLock = handlerLock

    var gameState = initGamestate
        private set(value) {
            field = value
            publishGamestateUpdate()
        }

    private val lastThrowingPlayer: MutableMap<Team, Player> = mutableMapOf()

    /**
     * Only relevant when changes do not directly result in a gameState eg. player updates
     */
    internal fun publishGamestateUpdate() {
        handlerLock.withLock {
            onEvent(GameStateEvent(gameState))
        }
    }

    fun throwBall(name: String, strength: EnumThrowStrength): EnumThrowRespStatus {
        gameStateLock.withLock {
            val state = gameState

            // -- determining whether we are allowed to throw --

            if (state.throwingPlayer == null || name != state.throwingPlayer)
                return EnumThrowRespStatus.THROW_STATUS_NOT_THROWING_PLAYER

            val player = playerController.getPlayer(state.throwingPlayer)
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
            val nextThrowingPlayer = getNextThrowingPlayer(otherTeam)
            val nextThrowingPhase = otherTeam.toThrowingPhase()

            // launch coroutine which disables the resting period, writes the result in the log and sets the next player
            GlobalScope.launch {

                delay(restingTime.toLong())

                if (hit)
                    messageController.sendLogMessage(
                        player.name,
                        "hat für ${throwingTeam.positionalName()} getroffen."
                    )
                else
                    messageController.sendLogMessage(
                        player.name,
                        "hat nicht für ${throwingTeam.positionalName()} getroffen."
                    )

                gameState = gameState
                    .setThrowingPlayer(nextThrowingPlayer?.name)
                    .setRoundPhase(nextThrowingPhase)

                messageController.sendLogMessage(nextThrowingPlayer?.name ?: "Niemand", "ist mit werfen dran.")
            }

            gameState = gameState.setRoundPhase(EnumRoundPhase.RESTING_PHASE)
            return EnumThrowRespStatus.THROW_STATUS_SUCCESS
        }
    }

    fun forceThrowingPlayer(name: String): Boolean {
        gameStateLock.withLock {
            val player = playerController.getPlayer(name) ?: return false
            val playerTeam = player.team
            val throwingPhase = playerTeam.toThrowingPhase()
            if (throwingPhase == EnumRoundPhase.UNKNOWN_PHASE) return false
            gameState = gameState.copy(throwingPlayer = player.name, roundPhase = throwingPhase)
            return true
        }
    }

    fun modifyStrafbierCount(team: EnumTeams, increment: Boolean): Boolean {
        gameStateLock.withLock {
            val diff = if (increment) 1 else -1

            val success = when (team) {
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

            if (success && increment) {
                videoController.playVideos(
                    listOf(
                        VideoInstructions(
                            VideoType.Strafbier,
                            mirrored = EnumTeams.TEAM_A_TEAMS == team
                        )
                    )
                )
            }

            return success
        }
    }


    fun resetGame(): Boolean {
        gameStateLock.withLock {

            val teamA = playerController.TeamA
            val teamB = playerController.TeamB

            // determine starting team
            val startingTeam = when {
                teamA.count() > teamB.count() -> teamB
                teamB.count() < teamA.count() -> teamA
                Random.nextBoolean() -> teamA
                else -> teamB
            }

            lastThrowingPlayer.clear()

            gameState = GameState(
                throwingPlayer = startingTeam.firstOrNull()?.name,
                abgegeben = emptyList()
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

    fun setAbgegeben(judgeName: String, targetName: String, abgegeben: Boolean): EnumAbgegebenRespStatus {
        gameStateLock.withLock {
            val player =
                playerController.getPlayer(targetName) ?: return EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_UNKNOWN_TARGET
            val judge =
                playerController.getPlayer(judgeName) ?: return EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_UNKNOWN_JUDGE

            if (abgegeben && player.team == judge.team)
                return EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_OWN_TEAM

            // check if team has won
            var newState = gameState.setAbgegeben(player.name, abgegeben)
            if (playerController.getTeam(player.team).all { newState.getAbgegeben(it.name) }
                && newState.getStrafbier(player.team) == 0) {
                newState = newState
                    .setRoundPhase(
                        when (player.team) {
                            Team.A -> EnumRoundPhase.TEAM_A_WON_PHASE
                            Team.B -> EnumRoundPhase.TEAM_B_WON_PHASE
                            else -> return EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_ERROR
                        }
                    )
                playerController.registerTeamWin(player.team)
            }

            gameState = newState
            return EnumAbgegebenRespStatus.ABGEGEBEN_STATUS_SUCCESS
        }
    }

    private fun getNextThrowingPlayer(team: Team): Player? {
        //if there is no previous thrower for a team or they have left start with first
        val previousThrower = if (lastThrowingPlayer.containsKey(team)
            || !playerController.allPlayers.contains(lastThrowingPlayer[team])
        )
            lastThrowingPlayer[team]
        else
            return playerController.getTeam(team).firstOrNull()

        val activeTeamWithIndex = playerController.allPlayers
            .mapIndexed { index, player -> player to index }
            .filter { p -> p.first.team == team && !gameState.getAbgegeben(p.first.name) }
        // by indexing over all players instead of just one team,
        // we can handle player switching teams

        val indexOfLast = playerController.allPlayers.indexOf(previousThrower)

        if (activeTeamWithIndex.isEmpty())
            return null

        return (activeTeamWithIndex.firstOrNull { (_, i) -> i > indexOfLast }
            ?: activeTeamWithIndex.first())
            .first
    }

    private fun Team.toThrowingPhase(): EnumRoundPhase {
        return when (this) {
            Team.A -> EnumRoundPhase.TEAM_A_THROWING_PHASE
            Team.B -> EnumRoundPhase.TEAM_B_THROWING_PHASE
            else -> EnumRoundPhase.UNKNOWN_PHASE
        }
    }

    /**
     * Used by PlayerCotnroller
     */
    fun handleRemovalOfPlayerFromTeamAndUpdate(player: String) {

        var newGameState = gameState.setAbgegeben(player, false)

        if (gameState.throwingPlayer == player) {
            val nextThrowingPlayer = when (gameState.roundPhase){
                EnumRoundPhase.TEAM_A_THROWING_PHASE -> getNextThrowingPlayer(Team.A)
                EnumRoundPhase.TEAM_B_THROWING_PHASE -> getNextThrowingPlayer(Team.B)
                else -> null
            }
            newGameState = newGameState.setThrowingPlayer(nextThrowingPlayer?.name)
        }

        gameState = newGameState
    }


    // -- Debug functions --
    fun hardReset() {
        gameStateLock.withLock {
            playerController.reset()
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

