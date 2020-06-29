var env = {};
$.get({
    url: "env",
    async: false,
    success: function (data) {
        env = data;
    }
});

const {
    EnumTeams, EnumConnectionStatus, EnumLoginStatus, KickPlayerReq, KickPlayerResp, Player,
    PlayerListResp, RegisterPlayerReq, RegisterPlayerResp, ShuffleTeamsReq,
    ShuffleTeamsResp, SwitchTeamReq, SwitchTeamResp
} = require('./player_service_pb');
const {PlayerServiceClient} = require('./player_service_grpc_web_pb');

var playerService = null;
export const PlayerManager = {};

jQuery(window).load(function () {
    playerService = new PlayerServiceClient(env['BACKEND_URL']);
});

PlayerManager.ownPlayerName = null;

//external functions
PlayerManager.external = {}
PlayerManager.external.sendMessage = null;
PlayerManager.external.processNewState = null; // TODO DECIDE: Might not be needed anymore
PlayerManager.external.toggleAbgabe = null;
PlayerManager.external.reduceStrafbierCount = null;
PlayerManager.external.selectThrowingPlayer = null;

PlayerManager.external.ClientManager = null

var metadata = {}

//https://stackoverflow.com/questions/7307983/while-variable-is-not-defined-wait
function __delay__(timer) {
    return new Promise(resolve => {
        timer = timer || 1000;
        setTimeout(function () {
            resolve();
        }, timer);
    });
};

async function subscribeTeamStreams() {

    while (!PlayerManager.external.ClientManager.metadata())
        await __delay__(1000);

    metadata = PlayerManager.external.ClientManager.metadata()

    const teamAStream = playerService.streamTeamAPlayers({}, metadata);
    teamAStream.on('data', (response) => {
        $('#teamaarea').empty();
        response.players.forEach(function (player, index) {
            player.team = EnumTeams.TEAM_A_TEAMS
            $('#teamaarea').append(
                PlayerManager.generatePlayerHTML(player,
                    false,
                    player.team === playerTeam,
                    true));
            // TODO: fix trowing player & rejoin button
        });
    })
    teamAStream.on('error', (response) => {
        console.log('Error in teamAStream:');
        console.log(response);
    });

    const teamBStream = playerService.streamTeamBPlayers({}, metadata);
    teamBStream.on('data', (response) => {
        $('#teambarea').empty();
        response.players.forEach(function (player, index) {
            player.team = EnumTeams.TEAM_B_TEAMS
            $('#teamaarea').append(
                PlayerManager.generatePlayerHTML(player,
                    false,
                    player.team === playerTeam,
                    true));
            // TODO: fix trowing player & rejoin button
        });
    })
    teamBStream.on('error', (response) => {
        console.log('Error in teamBStream:');
        console.log(response);
    });

    const spectatorStream = playerService.streamSpectator({},metadata);
    spectatorStream.on('data', (response) => {
        $('#spectatorarea').empty();
        response.players.forEach(function (player, index) {
            player.team = EnumTeams.SPECTATOR_TEAMS
            $('#spectatorarea').append(PlayerManager.generateSpectatorHTML(player));
        });
    })
    spectatorStream.on('error', (response) => {
        console.log('Error in spectatorStream:');
        console.log(response);
    });
}

PlayerManager.switchTeam = function (targetTeam, targetName) {
    const request = new SwitchTeamReq();
    request.setPlayername(PlayerManager.ownPlayerName);
    request.setTargetteam(targetTeam)
    request.setTargetname(targetName);
    console.log(request.toObject());
    playerService.switchTeam(request, metadata, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

PlayerManager.kickPlayer = function (targetName) {
    var request = new KickPlayerReq();
    request.setPlayername(PlayerManager.ownPlayerName);
    request.setTargetname(targetName);
    console.log(request.toObject());
    playerService.kickPlayer(request, metadata, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        }
    });
}

PlayerManager.changePlayername = function (desiredPlayername) {
    if (desiredPlayername === '') {
        console.log("Warning: Cannot register empty player name");
        return;
    }

    // Discourage false flag attacks, the player was already registered
    PlayerManager.ownPlayerName ? PlayerManager.external.sendMessage('hat sich zu ' + desiredPlayername + ' umbenannt') : '';

    const request = new RegisterPlayerReq();
    request.setPlayername(desiredPlayername);
    console.log(request.toObject());
    playerService.registerPlayer(request, metadata, function (err, response) {
        if (err) {
            console.log(err.code);
            console.log(err.message);
        } else {
            response = response.toObject();
            console.log(response);
            switch (response.status) {
                case EnumLoginStatus.LOGIN_STATUS_SUCCESS:
                case EnumLoginStatus.LOGIN_STATUS_NAME_TAKEN:
                    if (response.registeredname) {
                        PlayerManager.ownPlayerName = response.registeredname
                    } else {
                        // TODO: This is a bug in the server
                        console.log('Got empty registered name in response.');
                        PlayerManager.ownPlayerName = $('<div>').text(desiredPlayername).html();
                        console.log('Player name: ' + playerName);
                    }
                    $('#playername').text(PlayerManager.ownPlayerName);
                    $('#registerform').hide();
                    // Force re-evaluation of game state, e.g. do I need to throw
                    PlayerManager.external.processNewState();
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

PlayerManager.generateSpectatorHTML = function (player) {
    return PlayerManager.generatePlayerHTML(player, false, false, false, true);
}

PlayerManager.generatePlayerHTML = function (player,
                                             throwingPlayer = false,
                                             isOwnTeam = false,
                                             hasStrafbier = false,
                                             isSpectator = false) {
    let name = player.name;
    let isHimself = name === PlayerManager.ownPlayerName;
    let turnClass = name === throwingPlayer ? ' btn-primary' : ' btn-default';
    let egoClass = isHimself ? ' egoplayer' : '';
    let hasAbgegebenClass = player.abgegeben ? ' disabled' : '';

    // disabled for own team and not abgegeben
    let mayValidateAbgabeClass = !isOwnTeam ? "" : "disabled";
    let mayRejoinClass = hasStrafbier && isHimself ? "" : "disabled";

    let playerSpan = name === PlayerManager.ownPlayerName
        ? $('<span>')
            .append($('<span class="glyphicon glyphicon-chevron-right smaller-font">'))
            .append(name)
            .append($('<span class="glyphicon glyphicon-chevron-left smaller-font">'))
        : name;
    let playerbutton = $("<a href='#'>").addClass("btn namebutton" + turnClass + egoClass + hasAbgegebenClass).html(playerSpan)
    if (!isSpectator) {
        playerbutton
            .click(((n) => () => PlayerManager.external.selectThrowingPlayer(n))(name))
            .attr({
                "data-toggle": "tooltip",
                "title": "Werfer machen"
            });
    }

    let html = $('<div role="group">').addClass("btn-group btn-group-justified vspace-small playerbuttongroup")
        .append(playerbutton)
        .append($('<div role="group">').addClass("btn-group")
            .append($("<a href='#'>").addClass("btn btn-default dropdown-toggle").attr({
                "type": "button",
                "data-toggle": "dropdown",
                "data-toggle-second": "tooltip",
                "aria-haspopup": "true",
                "aria-expanded": "false",
                "title": "Spieler verschieben",
            }).append($("<span>").addClass("glyphicon glyphicon-transfer")))
            .append($("<ul>").addClass("dropdown-menu")
                .append($("<li>")
                    .append($("<a href='#'>").addClass("switchteamabutton").text("Linkes Team"))
                    .click(((n) => () => PlayerManager.switchTeam(EnumTeams.TEAM_A_TEAMS, n))(name)))
                .append($("<li>")
                    .append($("<a href='#'>").addClass("switchteambbutton").text("Rechtes Team"))
                    .click(((n) => () => PlayerManager.switchTeam(EnumTeams.TEAM_B_TEAMS, n))(name)))
                .append($("<li>")
                    .append($("<a href='#'>").addClass("switchspectatorbutton").text("Zuschauer"))
                    .click(((n) => () => PlayerManager.switchTeam(EnumTeams.SPECTATOR_TEAMS, n))(name)))));
    // Abgabe / TakeStrafbier button
    if (!isSpectator) {
        if (player.abgegeben) {
            html.append($("<a href='#' data-toggle='tooltip' title='Strafbier übernehmen'>")
                .addClass("btn btn-default abgebenbutton " + mayRejoinClass)
                .click(((n) => function () {
                    PlayerManager.external.toggleAbgabe(n);
                    PlayerManager.external.reduceStrafbierCount(player.team);
                })(name, player.abgegeben))
                .append($("<span>").addClass("glyphicon glyphicon-refresh"))
            );
        } else {
            html.append($("<a href='#' data-toggle='tooltip' title='Abgabe abnehmen'>")
                .addClass("btn btn-default abgebenbutton " + mayValidateAbgabeClass)
                .click(((n) => () => PlayerManager.external.toggleAbgabe(n))(name))
                .append($("<span>").addClass("glyphicon glyphicon-ok-circle"))
            );
        }
    }

    html.append($("<a href='#' data-toggle='tooltip' title='Spieler kicken'>")
        .addClass("btn btn-default kickbutton")
        .click(((n) => function () {
            if (confirm(`Möchtest du "${n}" wirklich kicken?`)) {
                PlayerManager.kickPlayer(n);
            }
        })(name))
        .append($("<span>").addClass("glyphicon glyphicon-ban-circle"))
    );

    return html;
}