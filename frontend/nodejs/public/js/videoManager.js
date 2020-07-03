var env = {};
$.get({
    url: "env",
    async: false,
    success: function (data) {
        env = data;
    }
});

const {
    EnumThrowStrength, EnumRoundPhase, EnumTeams, EnumVideoType, EnumLoginStatus,
    GameState, ThrowReq, ThrowResp, RegisterPlayerReq, RegisterPlayerResp,
    StreamStateReq, StreamStateResp, LogReq, LogResp,
    SendMessageReq, SendMessageResp, KickPlayerReq, KickPlayerResp,
    ResetGameReq, ResetGameResp, SwitchTeamReq, SwitchTeamResp,
    ModifyStrafbierCountReq, ModifyStrafbierCountResp, AbgegebenReq,
    AbgegebenResp, SelectThrowingPlayerReq, SelectThrowingPlayerResp,
    StreamVideoEventsReq, StreamVideoEventsResp
} = require('./generated/video_service_pb');
const {VideoServiceClient} = require('./generated/video_service_grpc_web_pb');

var videoService = null;
export const VideoManager = {};

jQuery(window).load(function () {
    videoService = new VideoServiceClient(env['BACKEND_URL']);
    subscribeVideoStream()

    let desktop = window.matchMedia("(min-width: 992px)").matches;
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

});

VideoManager.lowBandwidth = false;
var preparedVideos = {};

function subscribeVideoStream(){

    var videoEventsRequest = new StreamVideoEventsReq();
    var videoEventStream = videoService.streamVideoEvents(videoEventsRequest, {});
    videoEventStream.on('data', (response) => {
        processNewVideoEvent(response.getEvent().toObject());
    });
    videoEventStream.on('error', (response) => {
        console.log('Error in video event stream:');
        console.log(response);
    });
}

VideoManager.changeLowBandwidthMode = function() {
    if (VideoManager.lowBandwidth) {
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

function processNewVideoEvent(videoEvent) {
    if (typeof videoEvent.preparevideo !== 'undefined') {
        console.log('Got prepare video event');
        console.log(videoEvent.preparevideo);
        preparedVideos[videoEvent.preparevideo.videotype] = videoEvent.preparevideo.url;
        if (!VideoManager.lowBandwidth) {
            prepareVideo(videoEvent.preparevideo.url, videoEvent.preparevideo.videotype);
        }
    }
    if (typeof videoEvent.playvideos !== 'undefined') {
        console.log('Got play video event');
        console.log(videoEvent.playvideos);
        if (VideoManager.lowBandwidth) {
            videoEvent.playvideos.videosList.forEach(function (video, index) {
                var type = video.videotype;
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
            let videolist = videoEvent.playvideos.videosList;
            let first = videolist[0];
            if (videolist.length === 1) {
                setTimeout(() => {
                    playVideo(first.videotype, first.mirrored);
                }, first.delay);
            }
            if (videolist.length === 2) {
                setTimeout(() => {
                    playVideo(first.videotype, first.mirrored);
                }, first.delay);
                let second = videolist[1];
                scheduleSecondVideo(first, second);
            }
        }
    }
}


function prepareVideo(url, videotype) {
    let video = getVideoByType(videotype);
    video.attr('src', url);
    video[0].load();
    // Force loading of the video by starting to play it muted and hidden
    video.prop('muted', true).trigger('play');
}

function playVideo(videotype, mirrored) {
    // Abort all previously playing videos
    stopVideos();
    $('.logoposter').hide();
    let video = getVideoByType(videotype);
    if (mirrored) {
        video.addClass('mirroredvideo');
    } else {
        video.removeClass('mirroredvideo');
    }
    video.show().prop('muted', false).trigger('play');
    return video;
}

function scheduleSecondVideo(first, second) {
    let firstVideo = getVideoByType(first.videotype)[0];
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
    let poster = getPosterByType(videotype);
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
        case EnumVideoType.STRAFBIER_VIDEOTYPE:
            return $('.poster.strafbier');
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
        case EnumVideoType.STRAFBIER_VIDEOTYPE:
            return $('.video.strafbier');
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

