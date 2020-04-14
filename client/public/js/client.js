/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

console.log("Starte Flunkyball-Simulator");
const {EnumThrowStrength, GameState, ThrowReq, ThrowResp, RegisterPlayerReq, RegisterPlayerResp, StreamStateReq, StreamStateResp, LogReq, LogResp} = require('./flunkyprotocol_pb');
const {SimulatorClient} = require('./flunkyprotocol_grpc_web_pb');
var simulatorClient = null;
var playerName = "";
var currentTeamA = true;
var actionButtonsEnabled = false;
var currentGameState = null;

jQuery(window).load(function () {
    $('#softthrowbutton').click(function () {
        if (actionButtonsEnabled) {
            throwing(EnumThrowStrength.SOFT);
        }
    });
    $('#mediumthrowbutton').click(function () {
        if (actionButtonsEnabled) {
            throwing(EnumThrowStrength.MEDIUM);
        }
    });
    $('#hardthrowbutton').click(function () {
        if (actionButtonsEnabled) {
            throwing(EnumThrowStrength.HARD);
        }
    });
    $('#playernamebutton').click(function () {
        changePlayername();
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
    updateTeamDisplay();
    updateActionButtonDisplay();
    simulatorClient = new SimulatorClient('http://viings.de:8080');
    subscribeStreams();
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
    var desiredPlayername = $('#username').text();
    request.setPlayername(desiredPlayername);
    simulatorClient.registerPlayer(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        } else {
            //TODO(jdrees): Implement resetting the playerName if request was 
            //not successful
            playerName = desiredPlayername;
        }
    });
}

function throwing(strength) {
    var request = new ThrowReq();
    request.setPlayername(playerName);
    request.setStrength(strength);
    simulatorClient.throw(request, {}, function(err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function processNewState(state){
    currentGameState = state.toObject();
    console.log(currentGameState);
    updateActionButtonDisplay();
}

function processNewLog(content){
    $('#logbox').val(function(i, text) {
        return text + '\n' + content;
    });
}

function switchTeams() {
    currentTeamA = !currentTeamA;
    updateTeamDisplay();
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

function updateTeamDisplay() {
    if (currentTeamA) {
        activeteam = $('#teamadisplay');
        inactiveteam = $('#teambdisplay');
    } else {
        activeteam = $('#teambdisplay');
        inactiveteam = $('#teamadisplay');
    }
    activeteam.addClass('btn-info').removeClass('btn-default').prop('disabled', true);
    inactiveteam.removeClass('btn-info').addClass('btn-default').prop('disabled', false);
    if (currentTeamA) {
        $('.video').addClass('flippedvideo');
    }else{
        $('.video').removeClass('flippedvideo');
    }
}