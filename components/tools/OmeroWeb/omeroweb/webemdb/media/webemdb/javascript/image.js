$(document).ready(function() {
    
    $(".eman").click(function() {
        
        var href = $(this).attr('href');
        
        var $image = $("#processed");
        
        $image.attr('src', href);
        
        return false;
    })
    
});