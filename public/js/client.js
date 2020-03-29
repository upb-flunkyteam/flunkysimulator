/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

console.log("Starte Flunkyball-Simulator");
jQuery(window).load(function () {
    $('#aufbaubutton').click(function() {
        preparing();
    }); 
    $('#wurfbutton').click(function() {
        throwing();
    }); 
    $('.video').on('ended',function(){
        stopvideos();
    });
    $('.video').hide();
});

function preparing(){
    playvideo('preparation');
}

function throwing(){
    if(Math.random()<0.25){
        playvideo('hit');
        runningtime = Math.random() * 4 + 2 + 2.5;
        setTimeout(() => { playvideo('stop'); }, runningtime * 1000);
    }else{
        if(Math.random()<0.15){
            playvideo('close');
        }else{
            playvideo('nohit');
        }
        
    }
};

function playvideo(videofolder){
    // Abort all previously playing videos
    stopvideos();
    
    switch(videofolder){
        case 'preparation':
            videos = $('.preparation');
            break;
        case 'hit':
            videos = $('.hit');
            break;
        case 'nohit':
            videos = $('.nohit');
            break;
        case 'close':
            videos = $('.close');
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

function stopvideos(){
    $('.video').trigger('pause');
    $('.video').attr('currentTime', 0);
    $('.video').hide();
}