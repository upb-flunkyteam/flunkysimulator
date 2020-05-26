package simulator

import io.grpc.Server
import io.grpc.ServerBuilder
import simulator.control.GameController
import simulator.control.MessageController
import simulator.control.VideoController
import simulator.view.FlunkyServer
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Server that manages startup/shutdown of a `Greeter` server.
 *
 * Note: this file was automatically converted from Java
 */
class ServerStarter {

    private var server: Server? = null

    @Throws(IOException::class)
    private fun start() {

        val videoListUrl = System.getenv("VIDEO_LIST_URL")

        if (videoListUrl == null)
            logger.log(Level.WARNING,"VideoListUrl environment variable is missing." )


        val messageController = MessageController()
        val videoController = VideoController(videoListUrl)
        val gameController = GameController(videoController,messageController)
        val flunkyServer = FlunkyServer(gameController, messageController,videoController)

        // debug print
        gameController.addEventHandler { println(it) }
        messageController.addEventHandler { println(it) }

        /* The port on which the server should run */
        val port = 11049
        server = ServerBuilder.forPort(port)
            .addService(flunkyServer)
            .build()
            .start()
        logger.log(Level.INFO, "Server started, listening on {0}", port)
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down")
                this@ServerStarter.stop()
                System.err.println("*** server shut down")
            }
        })
    }

    private fun stop() {
        server?.shutdown()
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    @Throws(InterruptedException::class)
    private fun blockUntilShutdown() {
        server?.awaitTermination()
    }


    companion object {
        private val logger = Logger.getLogger(ServerStarter::class.java.name)

        /**
         * Main launches the server from the command line.
         */
        @Throws(IOException::class, InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val server = ServerStarter()
            server.start()
            server.blockUntilShutdown()
        }
    }
}

