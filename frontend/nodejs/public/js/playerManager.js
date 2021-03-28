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
    EnumTeams, EnumConnectionStatus,  KickPlayerReq, KickPlayerResp, Player,
    PlayerListResp, ShuffleTeamsReq,
    ShuffleTeamsResp, SwitchTeamReq, SwitchTeamResp
} = require('./generated/player_service_pb');
const {PlayerServiceClient} = require('./generated/player_service_grpc_web_pb');

var playerService = null;
export const PlayerManager = {};

PlayerManager.ownPlayerName = null;
PlayerManager.ownTeam = EnumTeams.SPECTATOR_TEAMS;

//external functions
PlayerManager.external = {}
PlayerManager.external.toggleAbgabe = null;
PlayerManager.external.reduceStrafbierCount = null;
PlayerManager.external.selectThrowingPlayer = null;
PlayerManager.external.getThrowingPlayerName = () => "";
PlayerManager.external.getStrafbierteamA = () => 0;
PlayerManager.external.getStrafbierteamB = () => 0;
PlayerManager.external.hasAbgegeben = (name) => false;

PlayerManager.external.ClientManager = null;
PlayerManager.external.MessageManager = null;

var playersStream = null;
var metadata = {}
var players = []
var teamA = []
var teamB = []
var spectators = []


jQuery(window).load(function () {
    playerService = new PlayerServiceClient(env['BACKEND_URL']);
    subscribeTeamStreams()

    /* TODO does not work currently
    It looks like it is not possible to send a request this early in startup.
    // Try to re-register if the username field is not empty
    // This happens when the page is reloaded
    // Browsers will preserve the form input, thus the username remains set
    let playerNameFormValue = $('#playername').val();
    if (playerNameFormValue) {
        if (confirm('Möchtest du mit dem Namen ' + playerNameFormValue + ' beitreten?')) {
            changePlayername(playerNameFormValue);
        }
    }*/

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

PlayerManager.reconnect = function (){
    subscribeTeamStreams();
}

async function subscribeTeamStreams() {

    while (!PlayerManager.external.ClientManager.metadata())
        await __delay__(1000);

    metadata = PlayerManager.external.ClientManager.metadata()

    // I would love to use the team specific streams but js continues to be js :/
    playersStream = playerService.streamAllPlayers(new Empty(), metadata);
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
    let hasAbgegeben = PlayerManager.external.hasAbgegeben(player.name);

    let nameButton = $('<a href="javascript:;">').addClass('btn namebutton');
    if (player.connectionstatus === EnumConnectionStatus.CONNECTION_DISCONNECTED){
        nameHtml = nameHtml.append($(' <span class="glyphicon glyphicon-flash">'))
    }
    nameButton.addClass(throwingPlayer ? ' btn-primary' : ' btn-default');
    nameButton.addClass(isHimself ? ' egoplayer' : '');
    nameButton.addClass(hasAbgegeben ? ' disabled' : '');
    // Highlight the ego player with arrows
    isHimself
        ? nameButton.html($('<span>')
            .append($('<span class="glyphicon glyphicon-chevron-right smaller-font">'))
            .append(name)
            .append($('<span class="glyphicon glyphicon-chevron-left smaller-font">')));
        : nameButton.html($('<span>')
            .append(name+' '));
    if (!isSpectator) {
        nameButton
            .click(((n) => () => PlayerManager.external.selectThrowingPlayer(n))(name))
            .attr({
                "data-toggle": "tooltip",
                "title": "Zum Werfer machen"
            });
    }

    let switchButton = $('<div role="group">').addClass("btn-group")
            .append($("<a href='javascript:;'>").addClass("btn btn-default dropdown-toggle").attr({
                "type": "button",
                "data-toggle": "dropdown",
                "data-toggle-second": "tooltip",
                "aria-haspopup": "true",
                "aria-expanded": "false",
                "title": "Spieler in ein anderes Team verschieben",
            }).append($("<span>").addClass("glyphicon glyphicon-transfer")))
            .append($("<ul>").addClass("dropdown-menu")
                .append($("<li>")
                    .append($("<a href='javascript:;'>").addClass("switchteamabutton").text("Linkes Team"))
                    .click(((n) => () => PlayerManager.switchTeam(EnumTeams.TEAM_A_TEAMS, n))(name)))
                .append($("<li>")
                    .append($("<a href='javascript:;'>").addClass("switchteambbutton").text("Rechtes Team"))
                    .click(((n) => () => PlayerManager.switchTeam(EnumTeams.TEAM_B_TEAMS, n))(name)))
                .append($("<li>")
                    .append($("<a href='javascript:;'>").addClass("switchspectatorbutton").text("Vestibül"))
                    .click(((n) => () => PlayerManager.switchTeam(EnumTeams.SPECTATOR_TEAMS, n))(name)))));

    let abgabeButton = $('');
    if (!isSpectator) {
        if (hasAbgegeben) {
            abgabeButton = $("<a href='javascript:;' data-toggle='tooltip' title='Strafbier übernehmen'>")
                .addClass("btn btn-default abgebenbutton")
                .click(((n) => () => PlayerManager.external.toggleAbgabe(n))(name))
                .append($("<span>").addClass("glyphicon glyphicon-refresh"))
            );
        } else {
            abgabeButton = ($("<a href='javascript:;' data-toggle='tooltip' title='Abgabe abnehmen'>")
                .addClass("btn btn-default abgebenbutton")
                .click(((n) => () => PlayerManager.external.toggleAbgabe(n))(name))
                .append($("<span>").addClass("glyphicon glyphicon-ok-circle"))
            );
        }
    }

   let kickButton = $("<a href='#' data-toggle='tooltip' title='Spieler kicken'>")
        .addClass("btn btn-default kickbutton")
        .click(((n) => function () {
            if (confirm(`Möchtest du "${n}" wirklich kicken?`)) {
                PlayerManager.kickPlayer(n);
            }
        })(name))
        .append($("<span>").addClass("glyphicon glyphicon-trash-alt"))
    );

    return $('<div role="group">').addClass("btn-group btn-group-justified vspace-small playerbuttongroup")
        .append(playerButton)
        .append(switchButton)
        .append(abgabeButton)
        .append(kickButton);
}