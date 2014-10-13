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
            // scalebar shouldn't be bigger then 1/5 of the regular viewport
            if (tiles) {
                var width = 100;
            } else {
                var width = 0;
                var viewport_width = $scalebar.parent().width();
                if (viewport_width > imageWidth*zoom) {
                    var width = Math.ceil(imageWidth/5);
                } else {
                    var width = Math.ceil(viewport_width/50);
                }
            }
            //find the nearest value round to power of 10
            // workaround for units smaller then micrometer
            if (pixSizeX < 1) {
                var num = Math.floor(width * pixSizeX * Math.pow(10,12));
                var factor = Math.pow(10, Math.floor(Math.log(num) / Math.LN10));
                var unit = factor * Math.ceil(num/factor) / Math.pow(10,12);
            } else {
                var num = Math.floor(width * pixSizeX);
                var factor = Math.pow(10, Math.floor(Math.log(num) / Math.LN10));
                var unit = factor * Math.ceil(num/factor);
            }
            if (tiles) {
                var scalebar_width = Math.round(unit/pixSizeX);
                $scalebar.width(scalebar_width);
                $scalebar.html((unit/zoom).lengthformat(0));
            } else {
                var scalebar_width = Math.round(unit/pixSizeX*zoom);
                $scalebar.width(scalebar_width);
                $scalebar.html(unit.lengthformat(0));
            }
        }

    });

}
