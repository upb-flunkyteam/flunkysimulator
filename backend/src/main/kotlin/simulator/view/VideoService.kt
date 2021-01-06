package simulator.view

import de.flunkyteam.endpoints.projects.simulator.StreamVideoEventsReq
import de.flunkyteam.endpoints.projects.simulator.StreamVideoEventsResp
import de.flunkyteam.endpoints.projects.simulator.VideoServiceGrpc
import io.grpc.stub.StreamObserver
import simulator.buildRegisterHandler
import simulator.control.VideoController
import simulator.control.VideoEvent

class VideoService(private val videoController: VideoController) : VideoServiceGrpc.VideoServiceImplBase(){

    override fun streamVideoEvents(
        request: StreamVideoEventsReq?,
        responseObserver: StreamObserver<StreamVideoEventsResp>?
    ) {

        videoController.getAllPreparedVideoEvents().forEach {
            responseObserver?.onNext(
                StreamVideoEventsResp.newBuilder()
                    .setEvent(it.toGrpc())
                    .build()
            )
        }

        val handler =
            buildRegisterHandler { event: VideoEvent ->
                responseObserver?.onNext(
                    StreamVideoEventsResp.newBuilder()
                        .setEvent(event.toGrpc())
                        .build()
                )
            }

        videoController.addEventHandler(handler::doAction)
    }
}