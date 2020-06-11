package simulator.model.video

import de.flunkyteam.endpoints.projects.simulator.TimedVideo

data class VideoInstructions(val type: VideoType, val delay: Long = 0, val mirrored: Boolean = false){

    fun toGrpc(): TimedVideo = TimedVideo.newBuilder()
        .setDelay(delay)
        .setMirrored(mirrored)
        .setVideoType(type.toGrpc())
        .build()
}