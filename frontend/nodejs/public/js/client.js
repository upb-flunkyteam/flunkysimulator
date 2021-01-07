/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var env = {};
$.get({
    url: "env",
    async: false,
    success: function (data) {
        env = data;
    }
});

console.log("Starte Flunkyball-Simulator");
const {
    EnumThrowStrength, EnumRoundPhase, EnumTeams, EnumVideoType, EnumLoginStatus,
    GameState, ThrowReq, ThrowResp, RegisterPlayerReq, RegisterPlayerResp,
    StreamStateReq, StreamStateResp, LogReq, LogResp,
    SendMessageReq, SendMessageResp, KickPlayerReq, KickPlayerResp,
    ResetGameReq, ResetGameResp, SwitchTeamReq, SwitchTeamResp,
    ModifyStrafbierCountReq, ModifyStrafbierCountResp, AbgegebenReq,
    AbgegebenResp, SelectThrowingPlayerReq, SelectThrowingPlayerResp,
    StreamVideoEventsReq, StreamVideoEventsResp,RestingPeriodReq
} = require('./generated/flunky_service_pb');
const {FlunkyServiceClient} = require('./generated/flunky_service_grpc_web_pb');

const {VideoManager} = require('./videoManager');
const {ClientManager} = require('./clientManager')
const {PlayerManager} = require('./playerManager');
const {MessageManager} = require('./messageManager');

PlayerManager.external.toggleAbgabe = toggleAbgabe;
PlayerManager.external.reduceStrafbierCount = reduceStrafbierCount;
PlayerManager.external.selectThrowingPlayer = selectThrowingPlayer;
PlayerManager.external.getThrowingPlayerName = () => {return currentGameState.throwingplayer};
PlayerManager.external.getStrafbierteamA = () => {return currentGameState.strafbierteama};
PlayerManager.external.getStrafbierteamB = () => {return currentGameState.strafbierteamb};
PlayerManager.external.hasAbgegeben = (name) => {return currentGameState.abgegebenList.includes(name)};

PlayerManager.external.ClientManager = ClientManager;
PlayerManager.external.MessageManager = MessageManager;

ClientManager.external.PlayerManager = PlayerManager;
ClientManager.external.MessageManager = MessageManager;
ClientManager.external.processNewState = function(){processNewState(currentGameState, true)};
ClientManager.external.triggerReconnect = reconnectAll;


MessageManager.external.playerManager = PlayerManager;


var flunkyService = null;
var stateStream = null;
var currentTeam = EnumTeams.UNKNOWN_TEAMS;
var actionButtonsEnabled = true;
var currentGameState = new GameState().toObject();
const title = document.title;

jQuery(window).load(function () {
    flunkyService = new FlunkyServiceClient(env['BACKEND_URL']);
    subscribeStreams();

    $('#softthrowbutton').click(function () {
        throwing(EnumThrowStrength.SOFT_THROW_STRENGTH);
    });
    $('#mediumthrowbutton').click(function () {
        throwing(EnumThrowStrength.MEDIUM_THROW_STRENGTH);
    });
    $('#hardthrowbutton').click(function () {
        throwing(EnumThrowStrength.HARD_THROW_STRENGTH);
    });

    $('#resetbutton').click(function () {
        if (confirm('Möchtest du wirklich das Spiel für alle Teilnehmenden neu starten?')) {
            resetGame();
        }
    });
    // secondary data-toogle to enable tooltips and dropdown at the same time
    $('[data-toggle-second="tooltip"]').tooltip();

    initDebugFunction()
});

//all besides client manager
function reconnectAll(){
    subscribeStreams();
    VideoManager.reconnect();
    MessageManager.reconnect();
    PlayerManager.reconnect();
}

function subscribeStreams() {
    var stateRequest = new StreamStateReq();
    stateStream = flunkyService.streamState(stateRequest, {});
    stateStream.on('data', (response) => {
        processNewState(response.getState().toObject());
    });
    stateStream.on('error', (response) => {
        console.log('Error in state stream:');
        console.log(response);
    });
}

function throwing(strength) {
    // disable the buttons so they cannot be used twice
    $('.throwbutton').prop('disabled', true);
    // Remove annoying flashing
    $('.actionbox').removeClass('flashingbackground');
    const request = new ThrowReq();
    request.setPlayername(PlayerManager.ownPlayerName);
    request.setStrength(strength);
    console.log(request.toObject());
    flunkyService.throw(request, {}, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function increaseStrafbierCount(team) {
    modifyStrafbierCount(team, true);
}

function reduceStrafbierCount(team) {
    modifyStrafbierCount(team, false);
}

function modifyStrafbierCount(team, increment) {
    var request = new ModifyStrafbierCountReq();
    request.setPlayername(PlayerManager.ownPlayerName);
    request.setTargetteam(team);
    request.setIncrement(increment);
    console.log(request.toObject());
    flunkyService.modifyStrafbierCount(request, {}, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function selectThrowingPlayer(targetName) {
    var request = new SelectThrowingPlayerReq();
    request.setPlayername(PlayerManager.ownPlayerName);
    request.setTargetname(targetName);
    console.log(request.toObject());
    flunkyService.selectThrowingPlayer(request, {}, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function toggleAbgabe(targetName) {
    var request = new AbgegebenReq();
    request.setPlayername(PlayerManager.ownPlayerName);
    request.setTargetname(targetName);
    let hasAbgegeben = currentGameState.abgegebenList.includes(targetName)
    request.setSetto(!hasAbgegeben);
    console.log(request.toObject());
    flunkyService.abgegeben(request, {}, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function resetGame() {
    var request = new ResetGameReq();
    request.setPlayername(PlayerManager.ownPlayerName);
    console.log(request.toObject());
    flunkyService.resetGame(request, {}, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function processNewState(state, stale = false) {
    currentGameState = state;
    console.log(currentGameState);
    if (currentGameState.roundphase === EnumRoundPhase.RESTING_PHASE) {
        return;
    }
    currentTeam = EnumTeams.UNKNOWN_TEAMS;
    if (currentGameState.roundphase === EnumRoundPhase.TEAM_A_THROWING_PHASE) {
        currentTeam = EnumTeams.TEAM_A_TEAMS;
    } else if (currentGameState.roundphase === EnumRoundPhase.TEAM_B_THROWING_PHASE) {
        currentTeam = EnumTeams.TEAM_B_TEAMS;
    }

    // TODO wont work because of  #82
    //  umlaut playernames are never correctly assigned to their team and will be removed nevertheless
    /*if (!stale && playerName && playerTeam === EnumTeams.UNKNOWN_TEAMS) {
        // player must have been kicked since he is not part of any team or lobby
        playerName = null;
        console.log('player appears to be kicked -> Playername reset to null');
    }*/

    // display strafbier
    let teamastrafbierarea = $('#teamastrafbierarea');
    teamastrafbierarea.empty();
    let teambstrafbierarea = $('#teambstrafbierarea');
    teambstrafbierarea.empty();
    teamastrafbierarea.append(generateStrafbierHTML(currentGameState.strafbierteama, EnumTeams.TEAM_A_TEAMS));
    teambstrafbierarea.append(generateStrafbierHTML(currentGameState.strafbierteamb, EnumTeams.TEAM_B_TEAMS));

    // Throwing Team related highlighting
    PlayerManager.ownTeam === currentTeam
        ? $('.video').addClass('highlight')
        : $('.video').removeClass('highlight');


    // Throwing player related highlighting
    if (currentGameState.throwingplayer === PlayerManager.ownPlayerName) {
        // It's my turn, display the throwing buttons!
        $('#throwactionbuttons').show();
        $('.throwbutton').prop('disabled', false);
        $('#throwerdisplayarea').hide();
        // Make sure user notices
        $('.actionbox').addClass('flashingbackground');
        document.title = `Wirf ${PlayerManager.ownPlayerName}!`;
    } else {
        document.title = title;
        // Remove annoying flashing
        $('.actionbox').removeClass('flashingbackground');
        // Update the box displaying who is currently throwing
        throwingText = '<b>' + currentGameState.throwingplayer + '</b> wirft';
        if (currentTeam === EnumTeams.TEAM_A_TEAMS) {
            $('#throwingplayer').addClass('text-left').removeClass('text-right');
            throwingText = '<span class="glyphicon glyphicon-chevron-left"></span>' + throwingText;
        } else {
            $('#throwingplayer').addClass('text-right').removeClass('text-left');
            throwingText = throwingText + '<span class="glyphicon glyphicon-chevron-right"></span>';
        }
        $('#throwingplayer').html(throwingText);
        $('#throwactionbuttons').hide();
        $('#throwerdisplayarea').show();
    }

    //update settings view
    const settingsView = $('#settingscontent')
    settingsView.text("Rest time: "+state.ruleconfig.restingperiodlength)

    PlayerManager.refreshPlayers();
}

function generateStrafbierHTML(number, team) {
    teamclass = team === EnumTeams.TEAM_A_TEAMS ? ' strafbierteamabutton' :
        team === EnumTeams.TEAM_B_TEAMS ? ' strafbierteambbutton' : '';

    html = $('<div role="group">').addClass("btn-group vspace");
    for (var i = 0; i < number; i++) {
        html.append($('<div>').addClass("btn btn-default reducebutton" + teamclass)
            .append($("<span>").addClass("glyphicon glyphicon-steinie"))
            .click(((team) => () => reduceStrafbierCount(team))(team))
            .attr({
                "data-toggle": "tooltip",
                "title": "Strafbier entfernen"
            })
        );
    }
    html.append($("<div>").addClass("btn btn-default increasebutton" + teamclass)
        .append($("<span>").addClass("glyphicon glyphicon-plus"))
        .append($("<span>").addClass("glyphicon glyphicon-steinie"))
        .click(((team) => () => increaseStrafbierCount(team))(team))
        .attr({
            "data-toggle": "tooltip",
            "title": "Strafbier hinzufügen"
        })
    );
    return html;
}

function initDebugFunction(){
    // debug functions which can be called from the browser console
    window.debug = {};

    // ---- settings modal ----
    // Get the modal
    const modal = $("#settings-modal")[0];
    // Get the <span> element that closes the modal
    const span = $("#close-settings")[0];

    window.debug.settings = function(){
        modal.style.display = "block";
    };
    // When the user clicks on <span> (x), close the modal
    span.onclick = function() {
        modal.style.display = "none";
    };
    // When the user clicks anywhere outside of the modal, close it
    window.onclick = function(event) {
        if (event.target == modal) {
            modal.style.display = "none";
        }
    };

    // ---- set resting period ----
    window.debug.setRestingPeriod = function(milliseconds){
        const request = new RestingPeriodReq()
        request.setMilliseconds(milliseconds)
        flunkyService.setRestingPeriod(request,{}, () => {})
    };
}

