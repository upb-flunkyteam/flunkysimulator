package simulator.control

import kotlinx.coroutines.Delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import simulator.ServerStarter
import simulator.getRandomElement
import simulator.model.video.VideoInstructions
import simulator.model.video.VideoType
import simulator.removeFirstAndLast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.withLock


class VideoController(private val videoListUrl: String?) :
    EventControllerBase<VideoEvent>() {

    init {
        GlobalScope.launch {
            delay(1000*10)
            // wait 10 sek otherwise stuff might not be initialized at startup
            // kinda dirty, I know
            refreshVideoList()
            delay(1000*60*10) // 10min
        }
    }

    private var videoUrls: Map<VideoType, List<String>> = VideoType.values()
        .map { it to listOf<String>() }
        .toMap()

    private val preparedVideos: MutableMap<VideoType, String?> = VideoType.values()
        .map { it to null }
        .toMap()
        .toMutableMap()

    fun playVideos(instructions: List<VideoInstructions>) {
        val typesWhichNeedNewVideo = instructions
            .map { it.type }
            .toSet()

        handlerLock.withLock {
            onEvent(VideoEvent.PlayVideos(instructions))

            typesWhichNeedNewVideo.forEach {
                val newUrl = videoUrls[it]?.getRandomElement()
                setPreparedVideo(newUrl, it)
            }
        }
    }

    fun refreshVideoList() {

        loadVideoUrls()

        VideoType.values().forEach {
            val videos = videoUrls[it] ?: error("Video Type \"${it}\" not found!")
            if (!videos.contains(preparedVideos[it])) {
                // video has been deleted or not set => need new one
                setPreparedVideo(videos.getRandomElement(), it)

            }
        }
    }

    fun getAllPreparedVideoEvents() = preparedVideos.mapNotNull { pair -> pair.value?.let { VideoEvent.PrepareVideo(pair.key,
        it)}}

    private fun setPreparedVideo(url: String?, type: VideoType) {
        if (url == null)
            return

        preparedVideos[type] = url
        handlerLock.withLock {
                onEvent(VideoEvent.PrepareVideo(type, url))
        }
    }

    /***
     * sendGetRequest, parse and save
     */
    private fun loadVideoUrls() {
        try {
            val videoListResp = sendGetRequest()
            videoUrls = parseJson(videoListResp)
        } catch (t: Throwable){
            println("Error getting new video list: \n $t")
        }
    }

    internal fun sendGetRequest(): String {
        if (videoListUrl == null)
            error("Video list url missing.")

        val mURL = URL(videoListUrl)

        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "GET"

            logger.log(Level.INFO, "Update VideoList by URL : {0}",url)
            logger.log(Level.INFO, "Update VideoList Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                //println("Response : $response")

                return response.toString()
            }
        }
    }

    internal fun parseJson(jsonString: String): Map<VideoType, List<String>> {
        val withoutBraces = jsonString.removeFirstAndLast()
        val elements = withoutBraces.split(",").map { it.trim().removeFirstAndLast() }

        return elements.mapNotNull { path ->
            val typeString = path.substringAfter("path=%2F").substringBefore("&files")

            val type = when (typeString) {
                "hit" -> VideoType.Hit
                "closenohit" -> VideoType.CloseMiss
                "nohit" -> VideoType.Miss
                "closemiss" -> VideoType.CloseMiss
                "miss" -> VideoType.Miss
                "preparation" -> VideoType.Setup
                "stop" -> VideoType.Stop
                "strafbier" -> VideoType.Strafbier
                else -> {
                    logger.log(Level.WARNING, "Unknown Video path type $path")
                    return@mapNotNull  null
                }
            }
            type to path
        }.groupBy { it.first }
            .map { it.key to it.value.map { typeToUrl -> typeToUrl.second } }
            .toMap()
    }
}

fun main() {
    val envVar = System.getenv("VIDEO_LIST_URL") ?: throw error("VideoListUrl parameter is missing")
    val vc = VideoController(envVar)
    val a = vc.parseJson(vc.sendGetRequest())
    a.forEach { println("${it.key}: ${it.value}") }
}


private val logger = Logger.getLogger(ServerStarter::class.java.name)