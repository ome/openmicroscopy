$(document).ready(function() {
    
    $(".eman").click(function() {
        
        var href = $(this).attr('href');
        
        var $image = $("#processed");
        
        $image.attr('src', href);
        
        return false;
    })
    
    $(".script").click(function() {
        
        var href = $(this).attr('href');
        
        var $scriptForm = $("#scriptForm");
        
        $scriptForm.load(href);
        
        return false;
    })
    
});