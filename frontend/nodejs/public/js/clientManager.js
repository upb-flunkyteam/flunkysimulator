var env = {};
$.get({
    url: "env",
    async: false,
    success: function (data) {
        env = data;
    }
});

const {AliveChallenge,ClientRegisterd,ClientStreamReq,ClientStreamResp,
    EventOneofCase} = require('./client_service_pb');
const {ClientServiceClient} = require('./client_service_grpc_web_pb');

var clientService = null;
export const ClientManager = {};

jQuery(window).load(function () {
    clientService = new ClientServiceClient(env['BACKEND_URL']);
});

ClientManager.secret = null

ClientManager.metadata = function() {
    if (secret){
        return {'client_secret_key': ClientManager.secret}
    }else {
        return null
    }}

ClientManager.subscribeClientStream = function(){
    const req = new ClientStreamReq()
    const clientStream = clientService.clientStream(req,{});
    clientStream.on('data', (response) => {
        if (typeof response.secret !== 'undefined'){
            //ClientRegistered
            ClientManager.secret = response.secret
        } else {
            //alive challenge - do nothing
        }
    })
    clientStream.on('error', (response) => {
        console.log('Error in clientStream:');
        console.log(response);
    });
}
