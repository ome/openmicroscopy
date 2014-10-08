/**
*  plugin for displaying Scalebar over an image *
*/

$.fn.scalebar_display = function(options) {
    return this.each(function(){

        var self = this;
        var scalebar_name = (options.scalebar_name ? options.scalebar_name : 'scalebar');
        var tiles =  (options.tiles ? options.tiles : false);
        var pixSizeX =  (options.pixSizeX ? options.pixSizeX : 0);
        var imageWidth =  (options.imageWidth ? options.imageWidth : 0);
        var $viewportimg = $(this);

        if (!tiles) {
            var $dragdiv = $viewportimg.parent().parent();
            var $scalebar = $('<div id="weblitz-viewport-'+scalebar_name+'" class="weblitz-viewport-'+scalebar_name+'">').appendTo($dragdiv);
        } else {
            var $scalebar = $('#'+scalebar_name);
        }
        
        // loads the ROIs if needed and displays them
        this.show_scalebar = function(theZ, theT) {
            scalebar_displayed = true;
            $scalebar.show()
        }

        // hides the ROIs from display
        this.hide_scalebar = function() {
            scalebar_displayed = false;
            $scalebar.hide()
        }

        this.setScalebarZoom = function(zoom) {
            var width = 200;
            // scalebar shouldn't be bigger then 1/5 of the viewport
            if (imageWidth>0 && imageWidth < 5*width) {
                width = Math.floor(imageWidth/5)
            }
            //find the nearest value round to power of 10
            if (tiles) {
                var num = Math.floor(width * pixSizeX);
            } else {
                var num = Math.floor(width * pixSizeX * zoom);
            }

            var factor = Math.pow(10, Math.floor(Math.log(num) / Math.LN10));
            var unit = factor * Math.ceil(num/factor);
            var scalebar_width = unit/pixSizeX;

            if (factor > 0 && scalebar_width > 49 ) {
                $scalebar.width();
                $scalebar.html((unit/zoom).lengthformat(0));
            } else {
                $scalebar.width(200);
                $scalebar.html("Error: zoom in to see the value");
            }

        }

    });

}
