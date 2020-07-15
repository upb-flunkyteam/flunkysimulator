package simulator

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptors
import simulator.control.*
import simulator.view.*
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
    internal fun start(port: Int) {

        val videoListUrl = System.getenv("VIDEO_LIST_URL")

        if (videoListUrl == null)
            logger.log(Level.WARNING, "VIDEO_LIST_URL environment variable is missing.")


        val messageController = MessageController()
        val videoController = VideoController(videoListUrl)
        val playerController = PlayerController()
        val clientManager = ClientsManager(playerController)
        val gameController = GameController(videoController, messageController,playerController)

        playerController.init {p ->
            gameController.handleRemovalOfPlayerFromTeamAndUpdate(p)
            clientManager.removePlayer(p)
        }

        // debug print
        gameController.addEventHandler { logger.info(it.toString()) }
        messageController.addEventHandler { logger.info(it.toString()) }
        playerController.addEventHandler { logger.info(it.toString()) }


        val flunkyService = FlunkyService(gameController, messageController)
        val playerService = PlayerService(playerController,messageController,clientManager)
        val videoService = VideoService(videoController)
        val clientServiceUnAuth = ClientServiceUnAuth(clientManager)
        val clientServiceAuth = ClientServiceAuth(clientManager,playerController,messageController)
        val messageService = MessageService(messageController)

        val authInterceptor = ClientAuthenticationInterceptor(clientManager)

        server = ServerBuilder.forPort(port)
            .addService(flunkyService)
            .addService(ServerInterceptors.intercept(playerService,authInterceptor))
            .addService(videoService)
            .addService(clientServiceUnAuth)
            .addService(ServerInterceptors.intercept(clientServiceAuth,authInterceptor))
            .addService(messageService)
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
    internal fun blockUntilShutdown() {
        server?.awaitTermination()
    }


}

private val logger = Logger.getLogger(ServerStarter::class.java.name)

/**
 * Main launches the server from the command line.
 */
fun main(args: Array<String>) {
    val port = if (args.size == 1) args[0].toInt() else 11049

    val server = ServerStarter()
    server.start(port)
    server.blockUntilShutdown()
}