package simulator.view;

import de.flunkyteam.endpoints.projects.simulator.*
import io.grpc.stub.StreamObserver
import simulator.buildRegisterHandler
import simulator.control.MessageController;

class MessageService(private val messageController:MessageController)
    : MessageServiceGrpc.MessageServiceImplBase(){

    override fun sendMessage(request: SendMessageReq?, responseObserver: StreamObserver<SendMessageResp>?) {
        messageController.sendMessage(request!!.message)

        responseObserver?.onNext(SendMessageResp.getDefaultInstance())
        responseObserver?.onCompleted()
    }

    override fun streamMessages(request: StreamMessageReq, responseObserver: StreamObserver<StreamMessageResp>) {
        val handler =
            buildRegisterHandler { event: MessageController.MessageEvent ->
                responseObserver.onNext(
                    StreamMessageResp.newBuilder()
                        .setMessage(event.message)
                        .build()
                )
            }

        responseObserver.onNext(
            StreamMessageResp.newBuilder()
                .setMessage(Message.newBuilder()
                    .setSender("Server:")
                    .setContent(patchNotes)
                    .setMessageType(EnumMessageType.MESSAGE_TYPE_LOG)
                    .build())
                .build())

        messageController.addEventHandler(handler::doAction)


    }


    private val patchNotes = """Version 2.5:
    - Hinzufügen von Siegesfeiervideos (Danke Basti)
    - Stoppuhrbefehl ".stoppuhr ${"$"}Sekunden${"$"}"
    """
}
