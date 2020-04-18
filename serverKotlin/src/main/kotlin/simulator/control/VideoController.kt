package simulator.control

import simulator.getRandomElement
import simulator.model.video.VideoInstructions
import simulator.model.video.VideoType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.withLock

class VideoController(private val videoListUrl: String = "https://upbflunkyteamsimulator.herokuapp.com/videolist") :
    EventControllerBase<VideoController.VideoEvent>() {

    sealed class VideoEvent() {
        data class PlayVideos(val videos: List<VideoInstructions>) :
            VideoEvent()

        data class PrepareVideo(val type: VideoType, val url: String?) :
            VideoEvent()
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

    fun refreshVideos() {
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
     * DOES NOT LOCK! But requires it to notify clients!
     */
    private fun setPreparedVideo(url: String?, type: VideoType) {
        preparedVideos[type] = url
        if (handlerLock.isHeldByCurrentThread)
            onEvent(VideoEvent.PrepareVideo(type,url))
    }

    /***
     * sendGetRequest, parse and save
     */
    private fun loadVideoUrls() {
        //TODO sendGetReqest, parse and save

        //dummy
        videoUrls = VideoType.values().map { it to listOf("$it Bewegtbild","$it Bewegtbild 2") }.toMap()

    }

    private fun sendGetRequest(): String {

        val mURL = URL(videoListUrl)

        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "GET"

            println("URL : $url")
            println("Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                println("Response : $response")

                return response.toString()
            }
        }
    }
}

fun main() {
    val vc = VideoController()
    //vc.sendGetRequest()
}

