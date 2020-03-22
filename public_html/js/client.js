/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

console.log("Starte Flunkyball-Simulator");
jQuery(window).load(function () {
    $("#wurfbutton").click(function() {
        throwing();
    }); 
    $("#treffervideo").on('ended',function(){
        throwingend();
    });
    $("#keintreffervideo").on('ended',function(){
        throwingend();
    });
    hidevideos();
});

function throwing(){
    console.log("Werfen!");
    if(Math.random()>0.5){
        playtreffer();
    }else{
        playkeintreffer();
    }
};

function throwingend(){
    hidevideos();
    showbutton();
}

function playtreffer(){
    $("#treffervideo").show();
    $("#treffervideo").trigger('play');
}

function playkeintreffer(){
    $("#keintreffervideo").show();
    $("#keintreffervideo").trigger('play');
}

function stoptreffer(){
    
}

function hidevideos(){
    $("#treffervideo").hide();
    $("#keintreffervideo").hide();
}

console.log("Flunkyball-Simulator geladen");