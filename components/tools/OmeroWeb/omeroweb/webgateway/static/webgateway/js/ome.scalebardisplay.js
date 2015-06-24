/**
*  plugin for displaying Scalebar over an image *
*/

$.fn.scalebar_display = function(options) {
    return this.each(function(){

        var viewerId = this.id;
        var $viewportimg = $(this);

        var tiles =  (options.tiles ? options.tiles : false);
        var pixSizeX =  (options.pixSizeX ? options.pixSizeX : 0);
        var imageWidth =  (options.imageWidth ? options.imageWidth : 0);

        var scalebar_displayed = false;   // flag to toggle visability.

        var scalebar_class = (options.scalebar_class ? options.scalebar_class : 'weblitz-viewport-scalebar');

        if (!tiles) {
            // add our ROI canvas as a sibling to the image plane. Parent is the 'draggable' div
            var $dragdiv = $viewportimg.parent().parent();
            var scalebar_name = (options.scalebar_name ? options.scalebar_name : viewerId + '-scalebar');
            var $scalebar =   $('<div id="'+scalebar_name+'" class="'+scalebar_class+'">').appendTo($dragdiv);
        } else {
            var scalebar_name = (options.scalebar_name ? options.scalebar_name : viewerId + '-tiles-scalebar');
            var $scalebar = $('#'+viewerId + '-tiles-scalebar')
        }
        
        // loads the Scalebar if needed and displays them
        this.show_scalebar = function(theZ, theT) {
            scalebar_displayed = true;
            $scalebar.show()
        }

        // hides the Scalebar from display
        this.hide_scalebar = function() {
            scalebar_displayed = false;
            $scalebar.hide()
        }

        this.setScalebarZoom = function(zoom) {
            var width = 100;

            function round(x) {
                if (x > 10) {
                    var num = Math.floor(x);
                    var factor = Math.pow(10, Math.floor(Math.log(num) / Math.LN10));
                    var unit = factor * Math.ceil(num/factor);
                    return unit;
                } else {

                    function power(n) {
                        p = 1;
                        while(n < 10) {
                            n = n*1000;
                            p++;
                        }
                        return p;
                    }
                    p = Math.pow(1000, power(x))

                    var num = Math.floor(x* p);
                    var factor = Math.pow(10, Math.floor(Math.log(num) / Math.LN10));
                    var unit = factor * Math.ceil(num/factor) / p;
                    return unit;

                }
            }

            unit = round(width * pixSizeX / zoom);
            scalebar_width = Math.round(unit/pixSizeX * zoom)
            $scalebar.width(scalebar_width);
            $scalebar.html((unit).lengthformat(0));

        }

    });

}