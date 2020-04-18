package simulator.control

import de.flunkyteam.endpoints.projects.simulator.PlayVideosEvent
import de.flunkyteam.endpoints.projects.simulator.PrepareVideoEvent
import simulator.model.video.VideoInstructions
import simulator.model.video.VideoType
import simulator.model.video.toGrpc

sealed class VideoEvent() {
    data class PlayVideos(val videos: List<VideoInstructions>) :
        VideoEvent() {
        override fun toGrpc(): de.flunkyteam.endpoints.projects.simulator.VideoEvent {
            return de.flunkyteam.endpoints.projects.simulator.VideoEvent
                .newBuilder()
                .setPlayVideos(
                    PlayVideosEvent.newBuilder()
                        .addAllVideos(
                            videos.map { it.toGrpc() }
                        )
                        .build()
                )
                .build()
        }
    }

    data class PrepareVideo(val type: VideoType, val url: String?) :
        VideoEvent() {
        override fun toGrpc(): de.flunkyteam.endpoints.projects.simulator.VideoEvent {
            return de.flunkyteam.endpoints.projects.simulator.VideoEvent
                .newBuilder()
                .setPrepareVideo(
                    PrepareVideoEvent.newBuilder()
                        .setVideoType(type.toGrpc())
                        .setUrl(url)
                        .build()
                )
                .build()
        }
    }

    abstract fun toGrpc(): de.flunkyteam.endpoints.projects.simulator.VideoEvent
}