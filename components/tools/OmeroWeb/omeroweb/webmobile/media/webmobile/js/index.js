$(document).ready(function() {
    $(".datasets").hide();
    $(".project").click(function() {
        $(this).next(".datasets").slideToggle();
    });
    
    var $enlargedCover = $('<img/>')
           .addClass('enlarged')
           .hide()
           .appendTo('body');
    var $waitThrobber = $('<img/>')
        .attr('src', 'images/wait.gif')
        .addClass('control')
        .css('z-index', 4)
        .hide();
    
    $(".thumbs").hide();
    $(".dataset").click(function(){
        $(this).next(".thumbs").slideToggle();
    });
   $(".dataset").one('click', function() {  // only load thumbs once
       link = $(this).attr("href");
       $(this).next(".thumbs").load(link, function() {
           
           $(this).find(".thumbnail").click(function(event) {
               
               var startPos = $(this).offset();
               startPos.width = $(this).width();
               startPos.height = $(this).height();
               var endPos = {};
               var w = $('body').width();
               endPos.width = Math.min(w, 310);
               endPos.height = Math.min(w, 310);
               endPos.top = startPos.top - 78;
               endPos.left = 5;
               
               $enlargedCover.attr('src', $(this).attr('href'))
                    .hide()
                    .css(startPos);
                 //.show();
               var performAnimation = function() {
                   //alert("performAnimation");
                 $waitThrobber.hide();
                 $enlargedCover.show()
                    .unbind('load')
                    .animate(endPos, 'normal',
                     function() {
                   $enlargedCover.one('click', function() {
                     $enlargedCover.fadeOut();
                   });
                   
                 });
               };
               //if ($enlargedCover[0].complete) {
               //    alert("complete");
               //  performAnimation();
               //}
               //else {
                 $enlargedCover.bind('load', performAnimation);
               //}

               event.preventDefault();
           });
       });
     return false   // stops link 
   });
   
 });