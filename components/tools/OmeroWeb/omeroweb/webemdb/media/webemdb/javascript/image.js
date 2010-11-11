
$(document).ready(function() {
    
    // All the links to eman-processed images - simply display the image in the page
    $(".eman").click(function() {
        
        var href = $(this).attr('href');
        var $image = $("#processed");
        $image.attr('src', href);
        
        return false;
    });
    
    
    // when the form is displayed, we bind the runScript function to the form submission. 
    var displayForm = function() {
        var href = $(this).attr('href');
        var $scriptForm = $("#scriptForm");
        $.get(href, function(scriptPage) {  
          $(scriptPage).appendTo($scriptForm);  // seems to be OK to put page contents: simply <title> and <body> content.
          $('#scriptForm form').submit(function() {
              $(this).runScript();
              $('#scriptForm').remove();
              return false;
          });
          $('#scriptForm .number').keypress(function(event) {
              // prevent-default unless key pressed is a number, or backspace
              if (event.which && event.which < 48 || event.which > 57) {
                  if (event.which != 8)   event.preventDefault();
              }
          });
          $('#scriptForm .float').keypress(function(event) {
                //alert(event.which);
                if (event.which && event.which < 48 || event.which > 57) {
                    if (event.which != 8 && event.which != 46)   event.preventDefault();
                }
            });
        });
        return false;
    };
      
    // The links to scripts get the form and put it in the page...
    $(".script").click(displayForm);
});