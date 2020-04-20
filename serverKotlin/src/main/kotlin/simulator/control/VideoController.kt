package simulator.control

import simulator.getRandomElement
import simulator.model.video.VideoInstructions
import simulator.model.video.VideoType
import simulator.removeFirstAndLast
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.withLock


class VideoController(private val videoListUrl: String = "https://upbflunkyteamsimulator.herokuapp.com/videolist") :
    EventControllerBase<VideoEvent>() {

    private var lastVideoListRefresh: Long = 0

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

    fun refreshVideos() {
        if (lastVideoListRefresh + 5 * 60 * 1000 > System.currentTimeMillis())
            return

        lastVideoListRefresh = System.currentTimeMillis()

        loadVideoUrls()

        VideoType.values().forEach {
            val videos = videoUrls[it] ?: error("Video Type not found!")
            if (!videos.contains(preparedVideos[it])) {
                // video has been deleted or not set => need new one
                handlerLock.withLock {
                    setPreparedVideo(videos.getRandomElement(), it)
                }
            }
        }
    }

    /***
     * DOES NOT LOCK by itself! But requires it to notify clients!
     */
    private fun setPreparedVideo(url: String?, type: VideoType) {
        preparedVideos[type] = url
        if (handlerLock.isHeldByCurrentThread && url != null)
            onEvent(VideoEvent.PrepareVideo(type, url))
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

        val mURL = URL(videoListUrl)

        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "GET"

            println("Update VideoList by URL : $url")
            println("Update VideoList Response Code : $responseCode")

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
        val elements = withoutBraces.split(",").map { it.removeFirstAndLast() }

        return elements.map { path ->
            val split = path.split("/")

            val type = when (split[0]) {
                "hit" -> VideoType.Hit
                "closenohit" -> VideoType.CloseMiss
                "nohit" -> VideoType.Miss
                "closemiss" -> VideoType.CloseMiss
                "miss" -> VideoType.Miss
                "preparation" -> VideoType.Setup
                "stop" -> VideoType.Stop
                else -> error("Unknown Video path type $path")
            }
            type to path
        }.groupBy { it.first }
            .map { it.key to it.value.map { typeToUrl -> typeToUrl.second } }
            .toMap()
    }
}

fun main() {
    val vc = VideoController()
    val a = vc.parseJson(vc.sendGetRequest())
    a.forEach { println("${it.key}: ${it.value}") }
}

