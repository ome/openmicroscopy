/**
*  plugin for displaying ROIs over an image canvas *
*  Requires Raphael      http://raphaeljs.com/
*  and scale.raphael.js  http://shapevent.com/scaleraphael/
*/

$.fn.roi_display = function(options) {
  return this.each(function(){
      
      var $viewportimg = $(this);
      var width = $viewportimg.attr('width');   // 0 initially
      var height = $viewportimg.attr('height');
      
      var $dragdiv = $viewportimg.parent();
      var $canvas =   $('<div id="roi_canvas" class="roi_canvas">').appendTo($dragdiv);
      
      var roi_json = null;
      
      if (options != null) {
          var orig_width = options.width;
          var orig_height = options.height;
      }
      
      // alert("orig sizes: " + orig_width + " " + orig_height);
      // Creates Raphael canvas. Uses scale.raphael.js to provide paper.scaleAll(ratio);
      var paper = new ScaleRaphael('roi_canvas', orig_width, orig_height);
      
      this.load_rois = function(url, display_rois) {
          
          $.getJSON(url, function(data) {
              roi_json = data;

              // plot the rois using processing.js
              if (display_rois) {
                  plot_rois();
              }
          });
      }
      
      // clears paper and draws ROIs for the given T and Z. NB: indexes are 1-based. 
      this.plot_rois = function(theT, theZ) {
          
          paper.clear();
          
          if (roi_json == null) return;
          
          for (var r=0; r<roi_json.length; r++) {
              var roi = roi_json[r];
              var shapes = roi['shapes'];
              var shape = null;
              for (var s=0; s<shapes.length; s++) {
                  shape = shapes[s];
                  if ((shape['theT'] == theT-1) && (shape['theZ'] == theZ-1)) {
                      var newShape = null;
                      if (shape['type'] == 'Ellipse') {
                          newShape = paper.ellipse(shape['cx'], shape['cy'], shape['rx'], shape['ry']);
                      }
                      else if (shape['type'] == 'Rectangle') {
                          newShape = paper.rect(shape['x'], shape['y'], shape['width'], shape['height']);
                      }
                      else if (shape['type'] == 'Point') {
                          newShape = paper.ellipse( shape['cx'], shape['cy'], 2, 2);
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
                              newShape.attr({ title:shape['textValue'] });
                          }
                      }
                      
                  }
              }
          }
      }
      
      this.setRoiZoom = function(percent) {
          
          //width = $viewportimg.attr('width');
          //height = $viewportimg.attr('height');
          
          paper.scaleAll(percent/100);
          
          
      }
      
  });
  
}