/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

console.log("Starte Flunkyball-Simulator");
jQuery(window).load(function () {
    $('#preparebutton').click(function () {
        if (actionButtonsEnabled) {
            preparing();
        }
    });
    $('#softthrowbutton').click(function () {
        if (actionButtonsEnabled) {
            throwing('soft');
        }
    });
    $('#mediumthrowbutton').click(function () {
        if (actionButtonsEnabled) {
            throwing('medium');
        }
    });
    $('#hardthrowbutton').click(function () {
        if (actionButtonsEnabled) {
            throwing('hard');
        }
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
});

var currentTeamA = true;
var actionButtonsEnabled = true;
const throwingtime = 2.5;
const closenohitprobability = 0.15;
const videoloadtime = 300; 

function startAction() {
    actionButtonsEnabled = false;
    updateActionButtonDisplay();
}

function endAction() {
    actionButtonsEnabled = true;
    updateActionButtonDisplay();
}

function preparing() {
    startAction();
    video = playvideo('preparation');
    setTimeout(() => {
        endAction();
        switchTeams();
    }, video[0].duration * 1000 + videoloadtime);
}

function throwing(hardness) {
    startAction();
    switch (hardness) {
        case 'soft':
            probability = 0.666;
            minimumDrinkingTime = 3;
            maximumDrinkingTime = 3;
            break;
        case 'medium':
            probability = 0.5;
            minimumDrinkingTime = 3;
            maximumDrinkingTime = 5;
            break;
        case 'hard':
            probability = 0.3;
            minimumDrinkingTime = 5;
            maximumDrinkingTime = 8.333;
            break;
    }
    if (Math.random() < probability) {
        playvideo('hit');
        runningtime = throwingtime + minimumDrinkingTime +
                Math.random() * (maximumDrinkingTime - minimumDrinkingTime);
        setTimeout(() => {
            video = playvideo('stop');
            setTimeout(() => {
                endAction();
                switchTeams();
            }, video[0].duration * 1000 + videoloadtime);
        }, runningtime * 1000);
    } else {
        if (Math.random() < closenohitprobability) {
            video = playvideo('closenohit');
        } else {
            video = playvideo('nohit');
        }
        setTimeout(() => {
            endAction();
            switchTeams();
        }, video[0].duration * 1000 + videoloadtime);
    }
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
    $('.video').trigger('pause');
    $('.video').attr('currentTime', 0);
    $('.video').hide();
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