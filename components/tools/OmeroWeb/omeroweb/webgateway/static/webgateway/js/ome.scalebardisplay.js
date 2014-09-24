/**
*  plugin for displaying Scalebar over an image *
*/

$.fn.scalebar_display = function(options) {
    return this.each(function(){

        var self = this;
        var scalebar_name = (options.scalebar_name ? options.scalebar_name : 'scalebar');
        var tiles =  (options.tiles ? options.tiles : false);

        var $viewportimg = $(this);

        if (!tiles) {
            // add our ROI canvas as a sibling to the image plane. Parent is the 'draggable' div
            var $dragdiv = $viewportimg.parent();
            var $scalebar = $('<div id="weblitz-viewport-'+scalebar_name+'" class="weblitz-viewport-'+scalebar_name+'">').appendTo($dragdiv);
            image_width = $viewportimg.attr('width');
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

        this.setScalebarZoom = function(pixSizeX, zoom, image_width) {
            var width = 100;
            if (image_width>0 && image_width < 5*width) {
                width = Math.floor(image_width/5)
            }

            zoom = (zoom/100)
            var num = Math.floor(width * pixSizeX);
            var factor = Math.pow(10, Math.floor(Math.log(num) / Math.LN10));
            var unit = factor * Math.ceil(num/factor);

            $scalebar.width(unit/pixSizeX);
            $scalebar.html((unit/zoom).lengthformat(0));

        }

    });

}
