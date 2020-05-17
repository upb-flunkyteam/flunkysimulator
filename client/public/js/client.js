/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

console.log("Starte Flunkyball-Simulator");
const {
    EnumThrowStrength, EnumTeams, EnumVideoType, EnumLoginStatus, GameState,
    ThrowReq, ThrowResp, RegisterPlayerReq, RegisterPlayerResp,
    StreamStateReq, StreamStateResp, LogReq, LogResp,
    SendMessageReq, SendMessageResp, KickPlayerReq, KickPlayerResp,
    ResetGameReq, ResetGameResp, SwitchTeamReq, SwitchTeamResp,
    ModifyStrafbierCountReq, ModifyStrafbierCountResp, AbgegebenReq,
    AbgegebenResp, SelectThrowingPlayerReq, SelectThrowingPlayerResp,
    StreamVideoEventsReq, StreamVideoEventsResp
} = require('./flunkyprotocol_pb');
const {SimulatorClient} = require('./flunkyprotocol_grpc_web_pb');
var simulatorClient = null;
var playerName = null;
var playerTeam = null
var currentTeam = EnumTeams.UNKNOWN_TEAMS;
var actionButtonsEnabled = true;
var currentGameState = null;
var lowBandwidth = false;
var preparedVideos = {};
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
        changePlayername($('#playername').val());
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
    lowBandwidth = !$('#lowbandwidthbutton').prop('checked');
    $('#lowbandwidthbutton').change(function () {
        lowBandwidth = !$(this).prop('checked');
        changeLowBandwidthMode();
    });
    $('.video').on('ended', function () {
        $(this).hide();
        $('.logoposter').show();
    });
    $('.video').hide();
    $('.poster').hide();
    $('#logbox').scrollTop($('#logbox')[0].scrollHeight);
    simulatorClient = new SimulatorClient('https://flunky.viings.de:8443');
    subscribeStreams();
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
    var videoEventsRequest = new StreamVideoEventsReq();
    var videoEventStream = simulatorClient.streamVideoEvents(videoEventsRequest, {});
    videoEventStream.on('data', (response) => {
        processNewVideoEvent(response.getEvent().toObject());
    });
    videoEventStream.on('error', (response) => {
        console.log('Error in video event stream:');
        console.log(response);
    });
}

function changePlayername(desiredPlayername) {
    if (desiredPlayername === '') {
        console.log("Warning: Cannot register empty player name");
        return;
    }

    // Discourage false flag attacks, the player was already registered
    playerName ? sendMessage('hat sich zu ' + desiredPlayername + ' umbenannt') : '';

    const request = new RegisterPlayerReq();
    request.setPlayername(desiredPlayername);
    console.log(request.toObject());
    simulatorClient.registerPlayer(request, {}, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        } else {
            response = response.toObject();
            console.log(response);
            switch (response.status) {
                case EnumLoginStatus.LOGIN_STATUS_SUCCESS:
                case EnumLoginStatus.LOGIN_STATUS_NAME_TAKEN:
                    playerName = response.registeredname;
                    if (!playerName) {
                        playerName = desiredPlayername;
                    }
                    $('#playername').text(playerName);
                    $('#registerform').hide();
                    // Force re-evaluation of game state, e.g. do I need to throw
                    processNewState(currentGameState);
                    break;
                case EnumLoginStatus.LOGIN_STATUS_EMPTY:
                    window.alert('Registrierung fehlgeschlagen! Dein Benutzername ist leer.');
                    break;
                case EnumLoginStatus.LOGIN_STATUS_SECRET_MISMATCH:
                    window.alert('Registrierung fehlgeschlagen! Passwort falsch.');
                    break;
                case EnumLoginStatus.LOGIN_STATUS_UNKNOWN:
                    window.alert('Registrierung fehlgeschlagen!');
                    break;
            }
        }
    });
}

function throwing(strength) {
    // disable the buttons so they cannot be used twice
    $('.throwbutton').prop('disabled', true);
    // Remove annoying flashing
    $('.actionbox').removeClass('flashingbackground');
    const request = new ThrowReq();
    request.setPlayername(playerName);
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
    request.setPlayername(playerName);
    request.setContent(content);
    console.log(request.toObject());
    simulatorClient.sendMessage(request, {}, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function switchTeam(targetTeam, targetName) {
    const request = new SwitchTeamReq();
    request.setPlayername(playerName);
    request.setTargetteam(targetTeam);
    request.setTargetname(targetName);
    console.log(request.toObject());
    simulatorClient.switchTeam(request, {}, function (err, response) {
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
    simulatorClient.kickPlayer(request, {}, function (err, response) {
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
    simulatorClient.modifyStrafbierCount(request, {}, function (err, response) {
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
    simulatorClient.selectThrowingPlayer(request, {}, function (err, response) {
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
    request.setPlayername(playerName);
    console.log(request.toObject());
    simulatorClient.resetGame(request, {}, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

function processNewState(state) {
    currentGameState = state;
    console.log(currentGameState);
    currentTeam = EnumTeams.UNKNOWN_TEAMS;
    playerTeam =
        currentGameState.playerteamaList.map(a => a.name).includes(playerName) ? EnumTeams.TEAM_A_TEAMS :
            currentGameState.playerteambList.map(a => a.name).includes(playerName) ? EnumTeams.TEAM_B_TEAMS :
                EnumTeams.SPECTATOR_TEAMS;

    // Create players
    $('#teamaarea, #teambarea, #spectatorarea').empty();
    currentGameState.playerteamaList.forEach(function (player, index) {
        player.team = EnumTeams.TEAM_A_TEAMS
        $('#teamaarea').append(generatePlayerHTML(player, currentGameState.throwingplayer, player.team === playerTeam));
        if (player.name === currentGameState.throwingplayer) {
            currentTeam = EnumTeams.TEAM_A_TEAMS;
        }
    });
    currentGameState.playerteambList.forEach(function (player, index) {
        player.team = EnumTeams.TEAM_B_TEAMS
        $('#teambarea').append(generatePlayerHTML(player, currentGameState.throwingplayer, player.team === playerTeam));
        if (player.name === currentGameState.throwingplayer) {
            currentTeam = EnumTeams.TEAM_B_TEAMS;
        }
    });
    currentGameState.spectatorsList.forEach(function (player, index) {
        player.team = EnumTeams.SPECTATOR_TEAMS
        $('#spectatorarea').append(generatePlayerHTML(player, currentGameState.throwingplayer, false));
    });
    $('#teamaarea').append(generateStrafbierHTML(currentGameState.strafbierteama, EnumTeams.TEAM_A_TEAMS));
    $('#teambarea').append(generateStrafbierHTML(currentGameState.strafbierteamb, EnumTeams.TEAM_B_TEAMS));

    // Throwing Team related highlighting
    console.log(playerTeam)
    console.log(currentTeam)

    playerTeam === currentTeam
        ? $('.video').addClass('highlight')
        : $('.video').removeClass('highlight');


    // Throwing player related highlighting
    if (currentGameState.throwingplayer === playerName) {
        // It's my turn, display the throwing buttons!
        $('#throwactionbuttons').show();
        $('.throwbutton').prop('disabled', false);
        $('#throwerdisplayarea').hide();
        // Make sure user notices
        $('.actionbox').addClass('flashingbackground')
        document.title = `!! ${playerName} !!`;
    } else {
        document.title = title
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

    registerStateButtonCallbacks();
}

function processNewLog(sender, content) {
    console.log("New log message: " + content);
    $('#logbox').val(function (i, text) {
        return text + '\n' + sender + ' ' + content;
    });
    $('#logbox').scrollTop($('#logbox')[0].scrollHeight);
}

function processNewVideoEvent(videoEvent) {
    if (typeof videoEvent.preparevideo !== 'undefined') {
        console.log('Got prepare video event');
        console.log(videoEvent.preparevideo);
        preparedVideos[videoEvent.preparevideo.videotype] = videoEvent.preparevideo.url;
        if (!lowBandwidth) {
            prepareVideo(videoEvent.preparevideo.url, videoEvent.preparevideo.videotype);
        }
    }
    if (typeof videoEvent.playvideos !== 'undefined') {
        console.log('Got play video event');
        console.log(videoEvent.playvideos);
        if (lowBandwidth) {
            videoEvent.playvideos.videosList.forEach(function (video, index) {
                type = video.videotype;
                if (type === EnumVideoType.HIT_VIDEOTYPE || type === EnumVideoType.MISS_VIDEOTYPE || type === EnumVideoType.NEAR_MISS_VIDEOTYPE) {
                    // Do not spoil the result just yet
                    console.log('Spoiler alert!');
                    setTimeout(() => {
                        playPoster('throw', video.mirrored);
                    }, video.delay);
                    setTimeout(() => {
                        playPoster(video.videotype, video.mirrored);
                    }, video.delay + 2500);
                } else {
                    setTimeout(() => {
                        playPoster(video.videotype, video.mirrored);
                    }, video.delay);
                }
            });
        } else {
            videolist = videoEvent.playvideos.videosList;
            first = videolist[0];
            if (videolist.length === 1) {
                setTimeout(() => {
                    playVideo(first.videotype, first.mirrored);
                }, first.delay);
            }
            if (videolist.length === 2) {
                setTimeout(() => {
                    playVideo(first.videotype, first.mirrored);
                }, first.delay);
                second = videolist[1];
                scheduleSecondVideo(first, second);
            }
        }
    }
}

function prepareVideo(url, videotype) {
    video = getVideoByType(videotype);
    video.attr('src', url);
    video[0].load();
    // Force loading of the video by starting to play it muted and hidden
    video.prop('muted', true).trigger('play');
}

function playVideo(videotype, mirrored) {
    // Abort all previously playing videos
    stopVideos();
    $('.logoposter').hide();
    video = getVideoByType(videotype);
    if (mirrored) {
        video.addClass('mirroredvideo');
    } else {
        video.removeClass('mirroredvideo');
    }
    video.show().prop('muted', false).trigger('play');
    return video;
}

function scheduleSecondVideo(first, second) {
    firstVideo = getVideoByType(first.videotype)[0];
    if (firstVideo.currentTime >= 2.5) {
        // Ready to play, we have played the first 2.5 seconds
        setTimeout(() => {
            playVideo(second.videotype, second.mirrored);
        }, second.delay - first.delay - 2500);
    } else {
        // Try again in 100ms
        setTimeout(() => {
            scheduleSecondVideo(first, second);
        }, 100);
    }
}

function playPoster(videotype, mirrored) {
    // Hide all previous posters
    $('.poster').hide();
    $('.logoposter').hide();
    poster = getPosterByType(videotype);
    poster.show();
    return poster;
}

function getPosterByType(videotype, mirrored) {
    switch (videotype) {
        case EnumVideoType.HIT_VIDEOTYPE:
            return $('.poster.hit');
        case EnumVideoType.MISS_VIDEOTYPE:
            return $('.poster.miss');
        case EnumVideoType.NEAR_MISS_VIDEOTYPE:
            return $('.poster.nearmiss');
        case EnumVideoType.SETUP_VIDEOTYPE:
            return $('.poster.setup');
        case EnumVideoType.STOP_VIDEOTYPE:
            return $('.poster.stop');
        case 'throw':
            return $('.poster.throw');
        default:
            return null;
    }
}

function getVideoByType(videotype) {
    switch (videotype) {
        case EnumVideoType.HIT_VIDEOTYPE:
            return $('.video.hit');
        case EnumVideoType.MISS_VIDEOTYPE:
            return $('.video.miss');
        case EnumVideoType.NEAR_MISS_VIDEOTYPE:
            return $('.video.nearmiss');
        case EnumVideoType.SETUP_VIDEOTYPE:
            return $('.video.setup');
        case EnumVideoType.STOP_VIDEOTYPE:
            return $('.video.stop');
        default:
            return null;
    }
}

function stopVideos() {
    $('.video').each(function (key, value) {
        value.pause();
        value.currentTime = 0;
        $(value).hide();
    });
}

function changeLowBandwidthMode() {
    if (lowBandwidth) {
        // Hide all the videos
        stopVideos();
    } else {
        // Hide all the posters
        $('.poster').hide();
        $('.logoposter').show();
        // Preload everything we ignored
        for (var videotype in preparedVideos) {
            var url = preparedVideos[videotype];
            prepareVideo(url, videotype);
        }

    }
}

function registerStateButtonCallbacks() {
    $('.switchteamabutton').click(function () {
        switchTeam(EnumTeams.TEAM_A_TEAMS, $(this).parents('.playerbuttongroup').children('.namebutton').html());
    });
    $('.switchteambbutton').click(function () {
        switchTeam(EnumTeams.TEAM_B_TEAMS, $(this).parents('.playerbuttongroup').children('.namebutton').html());
    });
    $('.switchspectatorbutton').click(function () {
        switchTeam(EnumTeams.SPECTATOR_TEAMS, $(this).parents('.playerbuttongroup').children('.namebutton').html());
    });
    $('.kickbutton').click(function () {
        kickPlayer($(this).parents('.playerbuttongroup').children('.namebutton').html());
    });
    $('.abgebenbutton').click(function () {
        abgeben($(this).parents('.playerbuttongroup').children('.namebutton').html());
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

function generatePlayerHTML(player, throwingPlayer, isOwnTeam) {
    name = player.name;
    spacing = ' vspace-small';
    isSelectPlayerDisabled = player.abgegeben ? ' disabled="disabled"' : '';
    isAbgabeDisabled = isOwnTeam ? ' disabled="disabled"' : '';
    currPlayerClass = name === throwingPlayer ? ' btn-primary' : ' btn-default';
    pName = name === playerName ? `<span class="glyphicon glyphicon-chevron-right" style="font-size: smaller"></span><span class="egoplayer">${name}</span><span class="glyphicon glyphicon-chevron-left" style="font-size: smaller"></span>` : name;


    html =
        '<div class="btn-group btn-group-justified playerbuttongroup' + spacing + '" role="group">\n\
            <div class="btn btn-default namebutton' + currPlayerClass + '"' + isSelectPlayerDisabled + '>' + pName + '</div>\n\
            <div class="btn-group" role="group">\n\
                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" \
                 data-toggle-second="tooltip" aria-haspopup="true" aria-expanded="false"  title="Spieler verschieben">\n\
                    <span class="glyphicon glyphicon-transfer"></span>\n\
                </button>\n\
                <ul class="dropdown-menu">\n\
                    <li><a href="#" class="switchteamabutton">Linkes Team</a></li>\n\
                    <li><a href="#" class="switchteambbutton">Rechtes Team</a></li>\n\
                    <li><a href="#" class="switchspectatorbutton">Zuschauer</a></li>\n\
                </ul>\n\
            </div>\n\
            <div class="btn btn-default abgebenbutton"' + isAbgabeDisabled + ' data-toggle="tooltip" title="Abgabe abnehmen"">\
               <span class="glyphicon glyphicon-ok-circle"></span></div>\n\
            <div class="btn btn-default kickbutton" data-toggle="tooltip" title="Spieler kicken">\
                <span class="glyphicon glyphicon-ban-circle"></span></div>\n\
        </div>';
    return html;
}

function generateStrafbierHTML(number, team) {
    teamclass = '';
    if (team === EnumTeams.TEAM_A_TEAMS) {
        teamclass = ' strafbierteamabutton';
    } else if (team === EnumTeams.TEAM_B_TEAMS) {
        teamclass = ' strafbierteambbutton';
    }
    html = '<div class="btn-group vspace" role="group">';
    for (var i = 0; i < number; i++) {
        html += '<div class="btn btn-default reducebutton' + teamclass + '"><span class="glyphicon glyphicon-steinie"></span></div>';
    }
    html += '<div class="btn btn-default increasebutton' + teamclass + '"><span class="glyphicon glyphicon-plus"></span><span class="glyphicon glyphicon-steinie"></span></div>';
    html += '</div>';
    return html;
}
