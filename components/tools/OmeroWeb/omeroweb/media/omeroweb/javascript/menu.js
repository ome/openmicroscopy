$(document).ready(function() 
    {
        /* Navigation */
        if ($('#navigation')) {
            var nav_resting_width = "-90px";
            var nav_hover_width = "0px";
            var body_class_name = document.body.id
            var delay = 400;

            $('#navigation > li').each(function(e) {
                if (this.className.indexOf(body_class_name) >= 0) {
                    $('a', this).css('marginTop', nav_hover_width);
                } else {
                    $(this).hover(function() {
                        $('a', this).animate({marginTop:nav_hover_width}, 200);
                    }, function () {
                        $('a', this).animate({marginTop:nav_resting_width}, 200);
                    });
                }
            });
        }

        if (document.body.id == 'start') {
            $('#navigation > li').each(function(e) {
                $('a', this)
                    .fadeTo(delay, 0.7)
                    .animate({marginTop:nav_hover_width}, 300)
                    .animate({marginTop:nav_resting_width}, 300);
                    delay += 100;
                });
        }
        
});
