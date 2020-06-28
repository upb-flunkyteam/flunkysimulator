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
    StreamVideoEventsReq, StreamVideoEventsResp
} = require('./flunkyprotocol_pb');
const {SimulatorClient} = require('./flunkyprotocol_grpc_web_pb');
const {PlayerManager} = require('./playerManager');

PlayerManager.external.sendMessage = sendMessage;
PlayerManager.external.processNewState = function(){processNewState(currentGameState, true)};
PlayerManager.external.toggleAbgabe = toggleAbgabe;
PlayerManager.external.reduceStrafbierCount = reduceStrafbierCount;
PlayerManager.external.selectThrowingPlayer = selectThrowingPlayer;

const {VideoManager} = require('./videoManager');

var simulatorClient = null;
var playerTeam = null;
var currentTeam = EnumTeams.UNKNOWN_TEAMS;
var actionButtonsEnabled = true;
var currentGameState = null;
const title = document.title;

jQuery(window).load(function () {
    $('#softthrowbutton').click(function () {
        throwing(EnumThrowStrength.SOFT_THROW_STRENGTH);
    });
    $('#mediumthrowbutton').click(function () {
        throwing(EnumThrowStrength.MEDIUM_THROW_STRENGTH);
    });
    $('#hardthrowbutton').click(function () {
        throwing(EnumThrowStrength.HARD_THROW_STRENGTH);
    });
    $('#playername').keyup(function (e) {
        if (e.keyCode === 13) {
            $(this).trigger("submission");
        }
    });
    $('#playernamebutton').click(function () {
        $('#playername').trigger("submission");
    });
    $('#playername').bind("submission", function (e) {
        PlayerManager.changePlayername($('#playername').val());
    });
    $('#switchplayerbutton').click(function () {
        $('#registerform').show();
        $('#playernamebutton').text('Spielernamen ändern');
    });
    $('#chatinput').bind("enterKey", function (e) {
        sendMessage($('#chatinput').val());
        $('#chatinput').val('');
    });
    $('#chatinput').keyup(function (e) {
        if (e.keyCode === 13) {
            $(this).trigger("enterKey");
        }
    });
    $('#resetbutton').click(function () {
        if (confirm('Möchtest du wirklich das Spiel für alle Teilnehmenden neu starten?')) {
            resetGame();
        }
    });
    // secondary data-toogle to enable tooltips and dropdown at the same time
    $('[data-toggle-second="tooltip"]').tooltip();
    desktop = window.matchMedia("(min-width: 992px)").matches;
    if (desktop) {
        $('#lowbandwidthbutton').bootstrapToggle('on');
    }
    VideoManager.lowBandwidth = !$('#lowbandwidthbutton').prop('checked');
    $('#lowbandwidthbutton').change(function () {
        VideoManager.lowBandwidth = !$(this).prop('checked');
        VideoManager.changeLowBandwidthMode();
    });
    // do not autohide hit-videos, in order to hold the last frame of the video until the stop video is played (#4)
    $('.video:not(.hit)').on('ended', function () {
        $(this).hide();
        $('.logoposter').show();
    });
    $('.video').hide();
    $('.poster').hide();
    $('#logbox').scrollTop($('#logbox')[0].scrollHeight);
    simulatorClient = new SimulatorClient(env['BACKEND_URL']);
    subscribeStreams();
    // Try to re-register if the username field is not empty
    // This happens when the page is reloaded
    // Browsers will preserve the form input, thus the username remains set
    playerNameFormValue = $('#playername').val();
    if (playerNameFormValue) {
        if (confirm('Möchtest du mit dem Namen ' + playerNameFormValue + ' beitreten?')) {
            PlayerManager.changePlayername(playerNameFormValue);
        }
    }
});

function subscribeStreams() {
    var stateRequest = new StreamStateReq();
    var stateStream = simulatorClient.streamState(stateRequest, {});
    stateStream.on('data', (response) => {
        processNewState(response.getState().toObject());
    });
    stateStream.on('error', (response) => {
        console.log('Error in state stream:');
        console.log(response);
    });
    var logRequest = new LogReq();
    var logStream = simulatorClient.streamLog(logRequest, {});
    logStream.on('data', (response) => {
        processNewLog(response.getSender(), response.getContent());
    });
    logStream.on('error', (response) => {
        console.log('Error in log stream:');
        console.log(response);
    });

    VideoManager.subscribeVideoStream()
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
    simulatorClient.throw(request, {}, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function sendMessage(content) {
    const request = new SendMessageReq();
    request.setPlayername(PlayerManager.ownPlayerName);
    request.setContent(content);
    console.log(request.toObject());
    simulatorClient.sendMessage(request, {}, function (err, response) {
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
    simulatorClient.modifyStrafbierCount(request, {}, function (err, response) {
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
    simulatorClient.selectThrowingPlayer(request, {}, function (err, response) {
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
    players = currentGameState.playerteamaList.concat(currentGameState.playerteambList);
    players.forEach(function (player, index) {
        if (player.name === targetName) {
            request.setSetto(!player.abgegeben);
        }
    });
    console.log(request.toObject());
    simulatorClient.abgegeben(request, {}, function (err, response) {
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
    simulatorClient.resetGame(request, {}, function (err, response) {
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
    playerTeam =
        currentGameState.playerteamaList.map(a => a.name).includes(PlayerManager.ownPlayerName) ? EnumTeams.TEAM_A_TEAMS :
            currentGameState.playerteambList.map(a => a.name).includes(PlayerManager.ownPlayerName) ? EnumTeams.TEAM_B_TEAMS :
                currentGameState.spectatorsList.map(a => a.name).includes(PlayerManager.ownPlayerName) ? EnumTeams.SPECTATOR_TEAMS :
                    EnumTeams.UNKNOWN_TEAMS;

    // TODO wont work because of  #82
    //  umlaut playernames are never correctly assigned to their team and will be removed nevertheless
    /*if (!stale && playerName && playerTeam === EnumTeams.UNKNOWN_TEAMS) {
        // player must have been kicked since he is not part of any team or lobby
        playerName = null;
        console.log('player appears to be kicked -> Playername reset to null');
    }*/

    // Create players
    $('#teamaarea, #teambarea, #spectatorarea').empty();
    currentGameState.playerteamaList.forEach(function (player, index) {
        player.team = EnumTeams.TEAM_A_TEAMS
        $('#teamaarea').append(PlayerManager.generatePlayerHTML(player, currentGameState.throwingplayer, player.team === playerTeam, currentGameState.strafbierteama));
    });
    currentGameState.playerteambList.forEach(function (player, index) {
        player.team = EnumTeams.TEAM_B_TEAMS
        $('#teambarea').append(PlayerManager.generatePlayerHTML(player, currentGameState.throwingplayer, player.team === playerTeam, currentGameState.strafbierteamb));
    });
    currentGameState.spectatorsList.forEach(function (player, index) {
        player.team = EnumTeams.SPECTATOR_TEAMS
        $('#spectatorarea').append(PlayerManager.generateSpectatorHTML(player));
    });
    $('#teamaarea').append(generateStrafbierHTML(currentGameState.strafbierteama, EnumTeams.TEAM_A_TEAMS));
    $('#teambarea').append(generateStrafbierHTML(currentGameState.strafbierteamb, EnumTeams.TEAM_B_TEAMS));

    // Throwing Team related highlighting
    playerTeam === currentTeam
        ? $('.video').addClass('highlight')
        : $('.video').removeClass('highlight');


    // Throwing player related highlighting
    if (currentGameState.throwingplayer === PlayerManager.ownPlayerName) {
        // It's my turn, display the throwing buttons!
        $('#throwactionbuttons').show();
        $('.throwbutton').prop('disabled', false);
        $('#throwerdisplayarea').hide();
        // Make sure user notices
        // TODO rename after namechange isResting
        console.log(state.restingperiod)
        if (!state.restingperiod) {
            console.log('not resting')
            $('.actionbox').addClass('flashingbackground');
        } else {
            console.log('resting')
        }
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
}

function processNewLog(sender, content) {
    console.log("New log message: " + content);
    $('#logbox').val(function (i, text) {
        return text + '\n' + sender + ' ' + content;
    });
    $('#logbox').scrollTop($('#logbox')[0].scrollHeight);
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
