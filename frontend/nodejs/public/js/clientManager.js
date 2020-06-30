var env = {};
$.get({
    url: "env",
    async: false,
    success: function (data) {
        env = data;
    }
});

const {AliveChallenge,ClientRegisterd,ClientStreamReq,ClientStreamResp} = require('./client_service_pb');
const EventOneofCase = ClientStreamResp.EventOneofCase
const {ClientServiceClient} = require('./client_service_grpc_web_pb');

var clientService = null;
export const ClientManager = {};

jQuery(window).load(function () {
    clientService = new ClientServiceClient(env['BACKEND_URL']);
    subscribeClientStream()
});

var secret = null

ClientManager.metadata = function() {
    if (secret){
        return {'client_secret_key': secret}
    }else {
        return null
    }}

function subscribeClientStream() {
    const req = new ClientStreamReq()
    var clientStream = clientService.clientStream(req,{});
    clientStream.on('data', (response) => {
        let tmp =response.getEventOneofCase();
        if (tmp === EventOneofCase.CLIENTREGISTERD){
            let event = response.getClientregisterd().toObject()
            secret = event.secret;

        } else if (response.getEventOneofCase() === EventOneofCase.ALIVECHALLENGE) {
            //alive challenge - do nothing
        } else {
            console.log("Unknown client stream event");
        }
    })
    clientStream.on('error', (response) => {
        console.log('Error in clientStream:');
        console.log(response);
    });
}
