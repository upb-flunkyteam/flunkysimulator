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
var clientStream = null;
var ownPlayers = [];
var lastAlivePoke = new Date();//Date('2000-01-01T01:00:00');
var lastReconnect = new Date();
var secret = null;

export const ClientManager = {};
ClientManager.external = {};
ClientManager.external.PlayerManager = {};
ClientManager.external.MessageManager = null;
ClientManager.external.processNewState = null;
ClientManager.external.triggerReconnect = null;

jQuery(window).load(function () {
    clientService = new ClientServiceClient(env['BACKEND_URL']);
    clientAuthService = new ClientServiceAuthClient(env['BACKEND_URL']);

    // Get the <span> element that closes the modal
    // When the user clicks on <span> (x), close the modal
    $("#close-login").click( function() {
        closeNewPlayerModal();
    });

    $('#player-name-button-modal').click(function () {
        $('#player-name-modal').trigger("submission");
    });
    $('#player-name-modal').bind("submission", function (e) {
        registerPlayer($('#player-name-modal').val());
    });

    $('#player-name-modal').keyup(function (e) {
        if (e.keyCode === 13) {
            $(this).trigger("submission");
        }
    });

    $('#switchplayerbutton').click(function () {
        $('#new-player-modal').show();
    });

    //connection watchdog
    let whatchdog = function (){
        let timeSinceLastPoke = new Date().getTime()- lastAlivePoke.getTime();

        let debugArea = $('#debuginformationarea');
        debugArea.empty();
        // debugArea
        //     .append($("<span>"))
        //     .append("Last Poked: "+timeSinceLastPoke/1000);

        if (timeSinceLastPoke > 15 * 1000) {
            // steams seem dead, reconnect
            const timeSinceLastReconnect = new Date().getTime() - lastReconnect.getTime();
            if (timeSinceLastReconnect > 5 * 1000) {
                lastReconnect = new Date();
                reconnect()
                ClientManager.external.triggerReconnect();
                ClientManager.external.MessageManager
                    .sendMessage("(debug) trys to reconnect because last poke was " + timeSinceLastPoke / 1000 + " sec ago.", true);
            }
        }
    };
    window.setInterval( whatchdog, 2000);

    reconnect();
});


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

    clientStream = clientService.clientStream(req,{});
    clientStream.on('data', (response) => {
        if (response.getEventOneofCase() === EventOneofCase.CLIENTREGISTERED){
            let event = response.getClientregistered().toObject();
            secret = event.secret;
        } else if (response.getEventOneofCase() === EventOneofCase.ALIVECHALLENGE) {
            //alive challenge - do nothing
            lastAlivePoke = new Date();
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

function reconnect(){
    //clean old connection
    secret = null;

    let prevOwnPlayers = ownPlayers; //copy it to be save

    subscribeClientStream();
    //reregister players
    prevOwnPlayers.forEach(player => registerPlayer(player));
}

function setOwnPlayer(newName) {
    ClientManager.external.PlayerManager.ownPlayerName = newName;
    closeNewPlayerModal()
    // Force re-evaluation of game state, e.g. do I need to throw
    ClientManager.external.processNewState();
    updateOwnPlayers()
}

function closeNewPlayerModal(){
    $("#new-player-modal").hide();
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

//https://stackoverflow.com/questions/7307983/while-variable-is-not-defined-wait
function __delay__(timer) {
    return new Promise(resolve => {
        timer = timer || 1000;
        setTimeout(function () {
            resolve();
        }, timer);
    });
}
