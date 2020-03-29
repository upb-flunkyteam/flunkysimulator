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
    playvideo('preperation');
}

function throwing(){
    if(Math.random()<0.25){
        playvideo('hit');
        runningtime = Math.random() * 4 + 2 + 2.5;
        setTimeout(() => { playvideo('stop'); }, runningtime * 1000);
    }else{
        playvideo('nohit');
    }
};

function playvideo(videofolder){
    // Abort all previously playing videos
    stopvideos();
    
    switch(videofolder){
        case 'preperation':
            video = $('#aufbauvideo');
            break;
        case 'hit':
            video = $('#treffervideo');
            break;
        case 'nohit':
            video = $('#keintreffervideo');
            break;
        case 'stop':
            video = $('#keintreffervideo');
            break;
        default:
            video = null;
            break;
    }
    video.show().trigger('play');
    return video;
}

function stopvideos(){
    $('.video').trigger('pause');
    $('.video').attr('currentTime', 0);
    $('.video').hide();
}