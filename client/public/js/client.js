/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

console.log("Starte Flunkyball-Simulator");
const {EnumThrowStrength, EnumTeams, GameState, 
    ThrowReq, ThrowResp, RegisterPlayerReq, RegisterPlayerResp, 
    StreamStateReq, StreamStateResp, LogReq, LogResp, 
    SendMessageReq, SendMessageResp, KickPlayerReq, KickPlayerResp,
    ResetGameReq, ResetGameResp, SwitchTeamReq, SwitchTeamResp,
    ModifyStrafbierCountReq, ModifyStrafbierCountResp, AbgegebenReq, 
    AbgegebenResp, SelectThrowingPlayerReq, SelectThrowingPlayerResp
} = require('./flunkyprotocol_pb');
const {SimulatorClient} = require('./flunkyprotocol_grpc_web_pb');
var simulatorClient = null;
var playerName = "";
var currentTeam = EnumTeams.UNKNOWN_TEAMS;
var actionButtonsEnabled = true;
var currentGameState = null;

jQuery(window).load(function () {
    $('#softthrowbutton').click(function () {
        if (actionButtonsEnabled) {
            throwing(EnumThrowStrength.SOFT_THROW_STRENGTH);
        }
    });
    $('#mediumthrowbutton').click(function () {
        if (actionButtonsEnabled) {
            throwing(EnumThrowStrength.MEDIUM_THROW_STRENGTH);
        }
    });
    $('#hardthrowbutton').click(function () {
        if (actionButtonsEnabled) {
            throwing(EnumThrowStrength.HARD_THROW_STRENGTH);
        }
    });
    $('#abgebenbutton').click(function () {
        abgeben(playerName);
    });
    $('#playernamebutton').click(function () {
        changePlayername();
    });
    $('#chatinput').bind("enterKey",function(e){
        sendMessage();
    });
    $('#chatinput').keyup(function(e){
        if(e.keyCode === 13){
            $(this).trigger("enterKey");
        }
    });
    $('#resetbutton').click(function () {
        resetGame();
    });
    $('#teamadisplay, #teambdisplay').click(function () {
        if (actionButtonsEnabled) {
            switchTeams();
        }
    });
    $('.video').on('ended', function () {
        stopvideos();
    });
    $('.video').hide();
    $('#logbox').scrollTop($('#logbox')[0].scrollHeight);
    updateActionButtonDisplay();
    simulatorClient = new SimulatorClient('http://viings.de:8080');
    subscribeStreams();
    changePlayername();
});

function subscribeStreams(){
    var stateRequest = new StreamStateReq();
    var stateStream = simulatorClient.streamState(stateRequest, {});
    stateStream.on('data', (response) => {
        processNewState(response.getState());
    });
    var logRequest = new LogReq();
    var logStream = simulatorClient.streamLog(logRequest, {});
    logStream.on('data', (response) => {
        processNewLog(response.getContent());
    });
    logStream.on('error', (response) => {
        console.log(response);
    });
}

function startAction() {
    actionButtonsEnabled = false;
    updateActionButtonDisplay();
}

function endAction() {
    actionButtonsEnabled = true;
    updateActionButtonDisplay();
}

function changePlayername(){
    var request = new RegisterPlayerReq();
    var desiredPlayername = $('#playername').val();
    if(desiredPlayername === ''){
        return;
    }
    request.setPlayername(desiredPlayername);
    console.log(request.toObject());
    simulatorClient.registerPlayer(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        } else {
            playerName = desiredPlayername;
        }
    });
}

function throwing(strength) {
    var request = new ThrowReq();
    request.setPlayername(playerName);
    request.setStrength(strength);
    console.log(request.toObject());
    simulatorClient.throw(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function sendMessage(){
    var request = new SendMessageReq();
    request.setPlayername(playerName);
    request.setContent($('#chatinput').val());
    console.log(request.toObject());
    simulatorClient.sendMessage(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
    $('#chatinput').val('');
}

function switchTeam(targetTeam, targetName) {
    var request = new SwitchTeamReq();
    request.setPlayername(playerName);
    request.setTargetteam(targetTeam);
    request.setTargetname(targetName);
    console.log(request.toObject());
    simulatorClient.switchTeam(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function kickPlayer(targetName) {
    var request = new KickPlayerReq();
    request.setPlayername(playerName);
    request.setTargetname(targetName);
    console.log(request.toObject());
    simulatorClient.switchTeam(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function modifyStrafbierCount(team, increment) {
    var request = new ModifyStrafbierCountReq();
    request.setPlayername(playerName);
    request.setTargetteam(team);
    request.setIncrement(increment);
    console.log(request.toObject());
    simulatorClient.modifyStrafbierCount(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function selectThrowingPlayer(targetName) {
    var request = new SelectThrowingPlayerReq();
    request.setPlayername(playerName);
    request.setTargetname(targetName);
    console.log(request.toObject());
    simulatorClient.selectThrowingPlayer(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function abgeben(targetName) {
    var request = new AbgegebenReq();
    request.setPlayername(playerName);
    request.setTargetname(targetName);
    players = currentGameState.playerteamaList.concat(currentGameState.playerteambList);
    players.forEach(function(player, index) { 
        if(player.name === targetName){
            request.setSetto(!player.abgegeben);
        }
    });
    console.log(request.toObject());
    simulatorClient.abgeben(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function resetGame(){
    var request = new ResetGameReq();
    request.setPlayername(playerName);
    console.log(request.toObject());
    simulatorClient.resetGame(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function processNewState(state){
    currentGameState = state.toObject();
    console.log(currentGameState);
    if(currentGameState.throwingplayer === playerName){
        actionButtonsEnabled = true;
    }else{
        actionButtonsEnabled = false;
    }
    currentTeam = EnumTeams.UNKNOWN_TEAMS;
    $('#teamaarea, #teambarea, #spectatorarea').empty();
    currentGameState.playerteamaList.forEach(function(player, index) {    
        $('#teamaarea').append(generatePlayerHTML(player, currentGameState.throwingplayer));
        if(player.name === currentGameState.throwingplayer){
            currentTeam = EnumTeams.TEAM_A_TEAMS;
        }
    });
    currentGameState.playerteambList.forEach(function(player, index) {    
        $('#teambarea').append(generatePlayerHTML(player, currentGameState.throwingplayer));
        if(player.name === currentGameState.throwingplayer){
            currentTeam = EnumTeams.TEAM_B_TEAMS;
        }
    });
    currentGameState.spectatorsList.forEach(function(player, index) {    
        $('#spectatorarea').append(generatePlayerHTML(player, currentGameState.throwingplayer));
    });
    $('#teamaarea').append(generateStrafbierHTML(currentGameState.strafbierteama, EnumTeams.TEAM_A_TEAMS));
    $('#teambarea').append(generateStrafbierHTML(currentGameState.strafbierteamb, EnumTeams.TEAM_B_TEAMS));
    registerStateButtonCallbacks();
    updateActionButtonDisplay();
}

function processNewLog(content){
    console.log("New log message: " + content);
    $('#logbox').val(function(i, text) {
        return text + '\n' + content;
    });
    $('#logbox').scrollTop($('#logbox')[0].scrollHeight);
}

function playvideo(videofolder) {
    // Abort all previously playing videos
    stopvideos();

    switch (videofolder) {
        case 'preparation':
            videos = $('.preparation');
            break;
        case 'hit':
            videos = $('.hit');
            break;
        case 'nohit':
            videos = $('.nohit');
            break;
        case 'closenohit':
            videos = $('.closenohit');
            break;
        case 'stop':
            videos = $('.stop');
            break;
        default:
            return null;
    }
    const video = $(videos[Math.floor(Math.random() * videos.length)]);
    video.show().trigger('play');
    return video;
}

function stopvideos() {
    $('.video').each(function(key, value){
        value.pause();
        value.currentTime = 0;
        $(value).hide();
    });
}

function updateActionButtonDisplay() {
    actionButtons = $('#mediumthrowbutton, #preparebutton, #softthrowbutton, #hardthrowbutton');
    actionButtons.prop('disabled', !actionButtonsEnabled);
}

function registerStateButtonCallbacks(){
    $('.switchteamabutton').click(function () {
        switchTeam(EnumTeams.TEAM_A_TEAMS, $(this).parents('.playerbuttongroup').children('.namebutton').text());
    });
    $('.switchteambbutton').click(function () {
        switchTeam(EnumTeams.TEAM_B_TEAMS, $(this).parents('.playerbuttongroup').children('.namebutton').text());
    });
    $('.switchspectatorbutton').click(function () {
        switchTeam(EnumTeams.SPECTATOR_TEAMS, $(this).parents('.playerbuttongroup').children('.namebutton').text());
    });
    $('.kickbutton').click(function () {
        kickPlayer($(this).parents('.playerbuttongroup').children('.namebutton').text());
    });
    $('.abgebenbutton').click(function () {
        abgeben($(this).parents('.playerbuttongroup').children('.namebutton').text());
    });
    $('.namebutton').click(function () {
        selectThrowingPlayer($(this).text());
    });
    $('.strafbierteamabutton.reducebutton').click(function () {
        modifyStrafbierCount(EnumTeams.TEAM_A_TEAMS, false);
    });
    $('.strafbierteamabutton.increasebutton').click(function () {
        modifyStrafbierCount(EnumTeams.TEAM_A_TEAMS, true);
    });
    $('.strafbierteambbutton.reducebutton').click(function () {
        modifyStrafbierCount(EnumTeams.TEAM_B_TEAMS, false);
    });
    $('.strafbierteambbutton.increasebutton').click(function () {
        modifyStrafbierCount(EnumTeams.TEAM_B_TEAMS, true);
    });
}

function generatePlayerHTML(player, throwingPlayer) {
    disabled = '';
    if(player.abgegeben){
        disabled = ' disabled="disabled"';
    }
    classes = ' btn-default';
    if(player.name === throwingPlayer){
        classes = ' btn-primary';
    }
    html = 
        '<div class="btn-group btn-group-justified vspace playerbuttongroup" role="group">\n\
            <div class="btn namebutton' + classes + '"' + disabled + '>'+player.name+'</div>\n\
            <div class="btn-group" role="group">\n\
                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">\n\
                    <span class="glyphicon glyphicon-transfer"></span>\n\
                </button>\n\
                <ul class="dropdown-menu">\n\
                    <li><a href="#" class="switchteamabutton">Linkes Team</a></li>\n\
                    <li><a href="#" class="switchteambbutton">Rechtes Team</a></li>\n\
                    <li><a href="#" class="switchspectatorbutton">Zuschauer</a></li>\n\
                </ul>\n\
            </div>\n\
            \n\<div class="btn btn-default abgebenbutton"><span class="glyphicon glyphicon-ok-circle"></span></div>\n\
            <div class="btn btn-default kickbutton"><span class="glyphicon glyphicon-ban-circle"></span></div>\n\
        </div>';
    return html;
}

function generateStrafbierHTML(number, team){
    teamclass = '';
    if(team === EnumTeams.TEAM_A_TEAMS){
        teamclass = ' strafbierteamabutton';
    }else if(team === EnumTeams.TEAM_B_TEAMS){
        teamclass = ' strafbierteambbutton';
    }
    html = '<div class="btn-group vspace" role="group">';
    for(var i=0; i < number; i++){
        html += '<div class="btn btn-default reducebutton'+teamclass+'"><span class="glyphicon glyphicon-glass"></span></div>';
    }
    html += '<div class="btn btn-default increasebutton'+teamclass+'"><span class="glyphicon glyphicon-plus"></span><span class="glyphicon glyphicon-glass"></span></div>';
    html += '</div>';
    return html;
}