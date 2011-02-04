/**
*  plugin for displaying ROIs over an image canvas *
*  Requires Raphael      http://raphaeljs.com/
*  and scale.raphael.js  http://shapevent.com/scaleraphael/
*/

$.fn.roi_display = function(options) {
    return this.each(function(){

        if (options != null) {
            var orig_width = options.width;
            var orig_height = options.height;
            var json_url = options.json_url;
        }

        var $viewportimg = $(this);
        var width = $viewportimg.attr('width');   // 0 initially
        var height = $viewportimg.attr('height');

        // add our ROI canvas as a sibling to the image plane. Parent is the 'draggable' div
        var $dragdiv = $viewportimg.parent();
        var $canvas =   $('<div id="roi_canvas" class="roi_canvas">').appendTo($dragdiv);

        var roi_json = null;          // load ROI data as json when needed
        var rois_displayed = false;   // flag to toggle visability.

        // alert("orig sizes: " + orig_width + " " + orig_height);
        // Creates Raphael canvas. Uses scale.raphael.js to provide paper.scaleAll(ratio);
        var paper = new ScaleRaphael('roi_canvas', orig_width, orig_height);


        // called when user clicks on ROI
        handle_shape_click = function(event) {
            var shape = this;
            var shape_id = parseInt(shape.id);
            shape_data = {'id':shape_id, 'cx':shape.attr('cx'), 'cy':shape.attr('cy')}
            $viewportimg.trigger("shape_click", [shape_id]);
        }

        // load the ROIs from json call and display
        load_rois = function(theZ, theT, display_rois) {
            if (json_url == undefined) return;
            
            $.getJSON(json_url, function(data) {
                roi_json = data;

                // plot the rois using processing.js
                if (display_rois) {
                  rois_displayed = true;
                  refresh_rois(theZ, theT);
                }
            });
        }


        // returns the ROI data as json. May be null if not yet loaded! 
        this.get_roi_json = function() {
            return roi_json;
        }

        // clears paper and draws ROIs (if rois_displayed) for the given T and Z. NB: indexes are 1-based. 
        this.refresh_rois = function(theZ, theT) {

            paper.clear();
            if (!rois_displayed) return;
            if (roi_json == null) return;

            for (var r=0; r<roi_json.length; r++) {
                var roi = roi_json[r];
                var shapes = roi['shapes'];
                var shape = null;
                for (var s=0; s<shapes.length; s++) {
                    shape = shapes[s];
                    if ((shape['theT'] == theT-1) && (shape['theZ'] == theZ-1)) {
                        var newShape = null;
                        var toolTip = ""
                        if (shape['type'] == 'Ellipse') {
                          newShape = paper.ellipse(shape['cx'], shape['cy'], shape['rx'], shape['ry']);
                          newShape['type'] = "Ellipse";
                          toolTip = "cx:"+ shape['cx'] +" cy:"+ shape['cy'] +" rx:"+ shape['rx'] + " ry: "+  shape['ry'];
                        }
                        else if (shape['type'] == 'Rectangle') {
                          newShape = paper.rect(shape['x'], shape['y'], shape['width'], shape['height']);
                          toolTip = "x:"+ shape['x'] +" y:"+ shape['y'] +
                            " width:"+ shape['width'] + " height: "+  shape['height'];
                        }
                        else if (shape['type'] == 'Point') {
                          newShape = paper.ellipse( shape['cx'], shape['cy'], 2, 2);
                          toolTip = "cx:"+ shape['cx'] +" cy:"+ shape['cy'];
                        }
                        else if (shape['type'] == 'PolyLine') {
                          newShape = paper.path( shape['points'] );
                        }
                        else if (shape['type'] == 'Polygon') {
                          newShape = paper.path( shape['points'] );
                        }
                        // rect.drag(moveRect, start, up);
                        // point.drag(move, start, up);
                        // roi_objects.push(rect);  // NOT used at the moment
                        if (newShape != null) {
                            newShape.attr({ fill: "#000", stroke: "#fff", opacity: 0.7 });
                            if ((shape['textValue'] != null) && (shape['textValue'].length > 0)) {
                                toolTip = shape['textValue'] + "  " + toolTip;
                                newShape.click(handle_shape_click);
                            }
                            newShape.attr({ title: toolTip });
                            newShape.id = shape['id'] + "_shape";
                        }

                    }
                }
            }
        }


        // loads the ROIs if needed and displays them
        this.show_rois = function(theZ, theT) {
          if (roi_json == null) {
              load_rois(theZ, theT, true);      // load and display
          } else {
              rois_displayed = true;
              this.refresh_rois(theZ, theT);
          }
        }

        // hides the ROIs from display
        this.hide_rois = function() {
            rois_displayed = false;
            this.refresh_rois();
        }

        // sets the Zoom of the ROI paper (canvas)
        this.setRoiZoom = function(percent) {
            paper.scaleAll(percent/100);
        }

    });

}