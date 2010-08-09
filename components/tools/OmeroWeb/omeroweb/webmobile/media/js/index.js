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
        .attr('src', '/webmobile/appmedia/img/wait.gif')
        .addClass('control')
        .css('z-index', 4)
        .hide();
    var $linkButton = $('<div>View</div>')
        .addClass('linkClass')
        .css('opacity', 0.6)
        .css('display', 'none')
        .appendTo('body');
    
    var followLink = function(event) {
        
    }
    
    $(".thumbs").hide();
    $(".dataset").click(function(){
        $(this).next(".thumbs").slideToggle();
    });
   $(".dataset").one('click', function() {  // only load thumbs once
       link = $(this).attr("href");
       $(this).next(".thumbs").load(link, function() {
           
           $(this).find(".thumbnail").click(function(event) {
               
               var imgId = event.target.id;     // target is the image clicked (not <a>)
               
               var finalW = 310;
               var finalH = 310;    // TODO: - calculate for non-square images. 
               var startPos = $(this).offset();
               startPos.width = $(this).width();
               startPos.height = $(this).height();
               var endPos = {};
               var w = $('body').width();
               endPos.width = Math.min(w, finalW);
               endPos.height = Math.min(w, finalH);
               endPos.top = startPos.top - 78;
               endPos.left = (w-endPos.width)/2;    // align middle
               
               $enlargedCover.attr('src',$(this).children().filter('img').attr('src'))
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
                     $linkButton.hide();
                     $linkButton.unbind('click');
                   });
                   $linkButton
                     .css({
                       'right': endPos.left,    // left and right are equal
                       'top' : endPos.top
                     })
                     .show()
                     .click(function() {
                         theUrl = "viewer/" + imgId + "/";
                       // follow link to viewer....
                        location.href = theUrl ;
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