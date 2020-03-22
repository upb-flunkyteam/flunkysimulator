/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

console.log("Starte Flunkyball-Simulator");
jQuery(window).load(function () {
    $("#aufbaubutton").click(function() {
        preparing();
    }); 
    $("#wurfbutton").click(function() {
        throwing();
    }); 
    $("#keintreffervideo").on('ended',function(){
        nohitend();
    });
    $("#aufbauvideo").on('ended',function(){
        preparingend();
    });
    hidevideos();
});

function preparing(){
    playpreperation();
}

function preparingend(){
    hidevideos();
}

function throwing(){
    console.log("Werfen!");
    if(Math.random()>0.5){
        playhit();
        runningtime = Math.random() * 4 + 2 + 2.8;
        setTimeout(() => { shoutstop(); }, runningtime * 1000);
    }else{
        playnohit();
    }
};

function shoutstop(){
    $("#treffervideo").trigger('stop');
    $("#treffervideo").attr('currentTime', 0);
    hidevideos();
    playstop();
    setTimeout(() => { hidevideos(); }, 5000);
}

function nohitend(){
    hidevideos();
}

function playhit(){
    $("#treffervideo").show().trigger('play');
}

function playnohit(){
    $("#keintreffervideo").show().trigger('play');
}

function playpreperation(){
    $("#aufbauvideo").show().trigger('play');
}

function playstop(){
    $("#stopvideo").show().trigger('play');
}

function hidevideos(){
    $("#treffervideo").hide();
    $("#keintreffervideo").hide();
    $("#aufbauvideo").hide();
    $("#stopvideo").hide();
}

console.log("Flunkyball-Simulator geladen");