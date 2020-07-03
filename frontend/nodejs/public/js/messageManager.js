var env = {};
$.get({
    url: "env",
    async: false,
    success: function (data) {
        env = data;
    }
});

const {
    EnumMessageType, Message, SendMessageReq, SendMessageResp,
    StreamMessageReq, StreamMessageResp,
} = require('./generated/message_service_pb');
const {MessageServiceClient} = require('./generated/message_service_grpc_web_pb');

var messageService = null;
export const MessageManager = {};

MessageManager.external = {}
MessageManager.external.playerManager = null

jQuery(window).load(function () {
    messageService = new MessageServiceClient(env['BACKEND_URL']);

    // register chat gui interactions
    $('#chatinput').bind("enterKey", function (e) {
        MessageManager.sendMessage($('#chatinput').val(), false);
        $('#chatinput').val('');
    });
    $('#chatinput').keyup(function (e) {
        if (e.keyCode === 13) {
            $(this).trigger("enterKey");
        }
    });

    $('#logbox').scrollTop($('#logbox')[0].scrollHeight);

    // subscribe streams
    const streamMessageRequest = new StreamMessageReq();
    const messageStream = messageService.streamMessages(streamMessageRequest, {});
    messageStream.on('data', (response) => {
        processNewMessage(response.getMessage().toObject());
    });
    messageStream.on('error', (response) => {
        console.log('Error in log stream:');
        console.log(response);
    });
});

function processNewMessage(message) {
    console.log("New message: " + message);

    let toString = message.sender;
    if ((message.messagetype === EnumMessageType.MESSAGE_TYPE_CHAT))
        toString += ": "+message.content;
    else
        toString += " "+message.content;
    appendMessage(toString);
}

function appendMessage(message){

    $('#logbox').val(function (i, text) {
        return text + '\n' + message;
    });
    $('#logbox').scrollTop($('#logbox')[0].scrollHeight);
}

MessageManager.sendMessage = function (content, isLog = false) {
    if ( $.trim( content ) == '' )
        return

    let playerName = MessageManager.external.playerManager.ownPlayerName;
    if (!playerName) {
        appendMessage("Bitte registriere dich erst mit einem Namen.")
        return
    }
    const message = new Message()
    message.setSender(playerName);
    message.setContent(content)
    message.setMessagetype((isLog) ? EnumMessageType.MESSAGE_TYPE_LOG
        : EnumMessageType.MESSAGE_TYPE_CHAT)

    const request = new SendMessageReq();
    request.setMessage(message)
    console.log("Sending: " + message)
    messageService.sendMessage(request, {}, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}