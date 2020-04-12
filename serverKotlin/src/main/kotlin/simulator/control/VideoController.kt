package simulator.control

class VideoController(private val messageController: MessageController) {
    fun playVideos(instructions: List<VideoInstructions>){
        //TODO video service
        instructions.forEach{ messageController.sendMessage("video plays: ",it.toString())}
    }
}

data class VideoInstructions(val type:VideoType, val delay: Double = 0.0, val mirrored: Boolean = false)

enum class VideoType{
    Setup, Hit, CloseMiss, Miss, Stop
}