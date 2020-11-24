import {PlayerManager} from "./playerManager";

var env = {};
$.get({
    url: "env",
    async: false,
    success: function (data) {
        env = data;
    }
});

const {AliveChallenge,ClientRegisterd,ClientStreamReq,ClientStreamResp,RegisterPlayerReq,RegisterPlayerResp,EnumLoginStatus} = require('./generated/client_service_pb');
const EventOneofCase = ClientStreamResp.EventOneofCase;

const {ClientServiceClient,ClientServiceAuthClient} = require('./generated/client_service_grpc_web_pb');

var clientService = null;
var clientAuthService = null;
var ownPlayers = [];
var lastAlivePoke = new Date();
var debugUpdateRef = null;

export const ClientManager = {};
ClientManager.external = {};
ClientManager.external.PlayerManager = {};
ClientManager.external.MessageManager = null;
PlayerManager.external.processNewState = null;

jQuery(window).load(function () {
    clientService = new ClientServiceClient(env['BACKEND_URL']);
    clientAuthService = new ClientServiceAuthClient(env['BACKEND_URL']);
    subscribeClientStream()

    $('#playernamebutton').click(function () {
        $('#playername').trigger("submission");
    });
    $('#playername').bind("submission", function (e) {
        registerPlayer($('#playername').val());
    });
    $('#switchplayerbutton').click(function () {
        $('#registerform').show();
        $('#playernamebutton').text('Spielernamen ändern');
    });
});

var secret = null

ClientManager.metadata = function() {
    if (secret){
        return {'client_secret_key': secret}
    }else {
        return null
    }}

function updateOwnPlayers() {
    let area = $('#ownplayersarea');
    let listArea = $('#ownplayerslistarea');
    listArea.empty();
    if (ownPlayers.length > 1) {
        area.show()
        area.removeClass('disabled')
        ownPlayers.forEach(playerName => {
            listArea.append(generateOwnPlayerHTML(playerName))
        })
    } else {
        area.hide()
    }
}

function subscribeClientStream() {
    const req = new ClientStreamReq()
    var clientStream = clientService.clientStream(req,{});
    clientStream.on('data', (response) => {
        if (response.getEventOneofCase() === EventOneofCase.CLIENTREGISTERED){
            let event = response.getClientregistered().toObject()
            secret = event.secret;
        } else if (response.getEventOneofCase() === EventOneofCase.ALIVECHALLENGE) {
            //alive challenge - do nothing
            lastAlivePoke = new Date();
            if (debugUpdateRef === null){
                debugUpdateRef = window.setInterval( () => {
                    let debugArea = $('#debuginformationarea');
                    debugArea.empty();
                    debugArea
                        .append($("<span>"))
                        .append("Last Poked: "+(new Date().getTime()- lastAlivePoke.getTime())/1000);
                }, 100);
            }
        } else if (response.getEventOneofCase() === EventOneofCase.OWNEDPLAYERSUPDATED) {
            let event = response.getOwnedplayersupdated().toObject();
            ownPlayers = event.playersList
            updateOwnPlayers();
        } else {
            console.log("Unknown client stream event");
        }
    })
    clientStream.on('error', (response) => {
        console.log('Error in clientStream:');
        console.log(response);
    });
}

function setOwnPlayer(newName) {
    ClientManager.external.PlayerManager.ownPlayerName = newName;
    $('#playername').text(newName);
    $('#registerform').hide();
    // Force re-evaluation of game state, e.g. do I need to throw
    ClientManager.external.processNewState();
    updateOwnPlayers()
}

async function registerPlayer(desiredPlayername){
    if (desiredPlayername === '') {
        console.log("Warning: Cannot register empty player name");
        return;
    }

    while (!ClientManager.metadata())
        await __delay__(1000);

    // Discourage false flag attacks, the player was already registered
    ClientManager.external.PlayerManager.ownPlayerName ?
        ClientManager.external.MessageManager
            .sendMessage('hat sich zu ' + desiredPlayername + ' umbenannt', true) : '';

    const request = new RegisterPlayerReq();
    request.setPlayername(desiredPlayername);
    console.log(request.toObject());
    clientAuthService.registerPlayer(request, ClientManager.metadata(), function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        } else {
            response = response.toObject();
            console.log(response);
            switch (response.status) {
                case EnumLoginStatus.LOGIN_STATUS_SUCCESS:
                case EnumLoginStatus.LOGIN_STATUS_NAME_TAKEN:
                    const newName= response.registeredname;
                    setOwnPlayer(newName);
                    break;
                case EnumLoginStatus.LOGIN_STATUS_PLAYER_TAKEN:
                    window.alert('Registrierung fehlgeschlagen! Aktive Sitzung für diesen Namen bereits vorhanden.')
                    if (desiredPlayername === ClientManager.external.PlayerManager.ownPlayerName){
                        //this was probatly a missclick or reconnect attempt
                        ClientManager.external.processNewState();//make the textbox go away
                    }
                    break;
                case EnumLoginStatus.LOGIN_STATUS_EMPTY:
                    window.alert('Registrierung fehlgeschlagen! Dein Benutzername ist leer.');
                    break;
                case EnumLoginStatus.LOGIN_STATUS_UNKNOWN:
                    window.alert('Registrierung fehlgeschlagen!');
                    break;
            }
        }
    });
}


function generateOwnPlayerHTML(playerName){
    const isHimself = playerName === ClientManager.external.PlayerManager.ownPlayerName;
    const playerSpan = isHimself
        ? $('<span>')
            .append($('<span class="glyphicon glyphicon-chevron-right smaller-font">'))
            .append(playerName)
            .append($('<span class="glyphicon glyphicon-chevron-left smaller-font">'))
        : $('<span>')
            .append(playerName+' ')

    const playerButton = $("<a href='#'>").addClass("btn namebutton btn-default")
        .html(playerSpan)
        .click(((n) => () => setOwnPlayer(n))(playerName))
        .attr({
            "data-toggle": "tooltip",
            "title": "Spieler aktivieren"
        });



    return $('<div role="group">')
        .addClass("btn-group btn-group-justified vspace-small playerbuttongroup")
        .append(playerButton)


}
