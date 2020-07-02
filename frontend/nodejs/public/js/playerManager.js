var env = {};
$.get({
    url: "env",
    async: false,
    success: function (data) {
        env = data;
    }
});

var {Empty} = require('google-protobuf/google/protobuf/empty_pb.js')

const {
    EnumTeams, EnumConnectionStatus, EnumLoginStatus, KickPlayerReq, KickPlayerResp, Player,
    PlayerListResp, RegisterPlayerReq, RegisterPlayerResp, ShuffleTeamsReq,
    ShuffleTeamsResp, SwitchTeamReq, SwitchTeamResp
} = require('./player_service_pb');
const {PlayerServiceClient} = require('./player_service_grpc_web_pb');

var playerService = null;
export const PlayerManager = {};

PlayerManager.ownPlayerName = null;
PlayerManager.ownTeam = EnumTeams.SPECTATOR_TEAMS;

//external functions
PlayerManager.external = {}
PlayerManager.external.sendMessage = null;
PlayerManager.external.processNewState = null;
PlayerManager.external.toggleAbgabe = null;
PlayerManager.external.reduceStrafbierCount = null;
PlayerManager.external.selectThrowingPlayer = null;
PlayerManager.external.getThrowingPlayerName = () => "";
PlayerManager.external.getStrafbierteamA = () => 0;
PlayerManager.external.getStrafbierteamB = () => 0;
PlayerManager.external.hasAbgegeben = (name) => false;

PlayerManager.external.ClientManager = null

var metadata = {}
var players = []
var teamA = []
var teamB = []
var spectators = []


jQuery(window).load(function () {
    playerService = new PlayerServiceClient(env['BACKEND_URL']);
    subscribeTeamStreams()

    // Try to re-register if the username field is not empty
    // This happens when the page is reloaded
    // Browsers will preserve the form input, thus the username remains set
    let playerNameFormValue = $('#playername').val();
    if (playerNameFormValue) {
        if (confirm('Möchtest du mit dem Namen ' + playerNameFormValue + ' beitreten?')) {
            changePlayername(playerNameFormValue);
        }
    }

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

    $('#shufflebutton').click(function () {
        if (confirm('Möchtest du wirklich die Teams neu mischen?')) {
            PlayerManager.shuffleTeams();
        }
    });

});

//https://stackoverflow.com/questions/7307983/while-variable-is-not-defined-wait
function __delay__(timer) {
    return new Promise(resolve => {
        timer = timer || 1000;
        setTimeout(function () {
            resolve();
        }, timer);
    });
}

PlayerManager.refreshPlayers = function () {
    // figure out own team
    players.forEach(player => {
        if (player.name === PlayerManager.ownPlayerName) {
            PlayerManager.ownTeam = player.team;
        }
    })

    $('#teamaarea').empty();
    $('#teambarea').empty();
    $('#spectatorarea').empty();

    let strafbierA = PlayerManager.external.getStrafbierteamA();
    let strafbierB = PlayerManager.external.getStrafbierteamB();
    let throwingPlayer = PlayerManager.external.getThrowingPlayerName();

    teamA.forEach(player => {
        $('#teamaarea').append(
            generatePlayerHTML(player,
                player.name === throwingPlayer,
                player.team === PlayerManager.ownTeam,
                strafbierA > 0));
    });
    teamB.forEach(player => {
        $('#teambarea').append(
            generatePlayerHTML(player,
                player.name === throwingPlayer,
                player.team === PlayerManager.ownTeam,
                strafbierB > 0));
    });

    spectators.forEach(player => {
        $('#spectatorarea').append(generateSpectatorHTML(player));
    });
}

async function subscribeTeamStreams() {

    while (!PlayerManager.external.ClientManager.metadata())
        await __delay__(1000);

    metadata = PlayerManager.external.ClientManager.metadata()

    // I would love to use the team specific streams but js continues to be js :/
    var playersStream = playerService.streamAllPlayers(new Empty(), metadata);
    playersStream.on('data', (response) => {
        players = response.getPlayersList().map(p => p.toObject());
        teamA = players.filter(p => p.team === EnumTeams.TEAM_A_TEAMS)
        teamB = players.filter(p => p.team === EnumTeams.TEAM_B_TEAMS)
        spectators = players.filter(p => p.team === EnumTeams.SPECTATOR_TEAMS)

        PlayerManager.refreshPlayers();
    })
    playersStream.on('error', (response) => {
        console.log('Error in players stream:');
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

PlayerManager.shuffleTeams = function () {
    const request = new ShuffleTeamsReq();
    request.setPlayername(PlayerManager.ownPlayerName);
    playerService.shuffleTeams(request, metadata, function (err, response) {
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

async function changePlayername(desiredPlayername) {
    if (desiredPlayername === '') {
        console.log("Warning: Cannot register empty player name");
        return;
    }

    while (!PlayerManager.external.ClientManager.metadata())
        await __delay__(1000);

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
                    PlayerManager.ownPlayerName = response.registeredname
                    $('#playername').text(PlayerManager.ownPlayerName);
                    $('#registerform').hide();
                    // Force re-evaluation of game state, e.g. do I need to throw
                    PlayerManager.external.processNewState();
                    break;
                case EnumLoginStatus.LOGIN_STATUS_PLAYER_TAKEN:
                    window.alert('Registrierung fehlgeschlagen! Aktive Sitzung für diesen Namen bereits vorhanden.')
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

function generateSpectatorHTML(player) {
    return generatePlayerHTML(player, false, false, false, true);
}

function generatePlayerHTML(player,
                            throwingPlayer = false,
                            isOwnTeam = false,
                            hasStrafbier = false,
                            isSpectator = false) {
    let name = player.name;
    let isHimself = name === PlayerManager.ownPlayerName;
    let turnClass = throwingPlayer ? ' btn-primary' : ' btn-default';
    let egoClass = isHimself ? ' egoplayer' : '';
    let hasAbgegeben = PlayerManager.external.hasAbgegeben(player.name);
    let hasAbgegebenClass = hasAbgegeben ? ' disabled' : '';

    // disabled for own team and not abgegeben
    let mayValidateAbgabeClass = !isOwnTeam ? "" : "disabled";
    let mayRejoinClass = hasStrafbier && isHimself ? "" : "disabled";

    let playerSpan = isHimself
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
        if (hasAbgegeben) {
            html.append($("<a href='#' data-toggle='tooltip' title='Strafbier übernehmen'>")
                .addClass("btn btn-default abgebenbutton " + mayRejoinClass)
                .click(((n) => function () {
                    PlayerManager.external.toggleAbgabe(n);
                    PlayerManager.external.reduceStrafbierCount(player.team);
                })(name))
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