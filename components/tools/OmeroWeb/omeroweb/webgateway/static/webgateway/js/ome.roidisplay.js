/**
*  plugin for displaying ROIs over an image canvas *
*  Requires Raphael      http://raphaeljs.com/
*  and scale.raphael.js  http://shapevent.com/scaleraphael/
*/

$.fn.roi_display = function(options) {
    return this.each(function(){

        var self = this;
        var canvas_name = (options.canvas_name ? options.canvas_name : 'roi_canvas');
        var tiles =  (options.tiles ? options.tiles : false);
        
        if (options != null) {
            var orig_width = options.width;
            var orig_height = options.height;
            var json_url = options.json_url;
        }

        var $viewportimg = $(this);
        var width = $viewportimg.attr('width');   // 0 initially
        var height = $viewportimg.attr('height');

        if (!tiles) {
            // add our ROI canvas as a sibling to the image plane. Parent is the 'draggable' div
            var $dragdiv = $viewportimg.parent();
            var $canvas =   $('<div id="'+canvas_name+'" class="'+canvas_name+'">').appendTo($dragdiv);
        } else {
            var $canvas = $('#'+canvas_name)
        }

        var roi_json = null;          // load ROI data as json when needed
        this.theZ = null;
        this.theT = null;
        var rois_displayed = false;   // flag to toggle visability.
        var roi_label_displayed = true;     // show/hide labels within shapes
        
        var selected_shape_id = null;  // html page is kept in sync with this
        var selectedClone = null;      // a highlighted shape cloned from currently selected shape
        
        // for keeping track of objects - E.g. de-select all. 
        var shape_objects = new Array();
        
        // Creates Raphael canvas. Uses scale.raphael.js to provide paper.scaleAll(ratio);
        var paper = new ScaleRaphael(canvas_name, orig_width, orig_height);
        
        // break long labels into multiple lines
        var formatShapeText = function(text_string) {
            var rows = parseInt(Math.sqrt(text_string.length / 6));     // rough ratio: cols = rows * 6
            var cols = parseInt(text_string.length/rows) + 1;
            if (text_string.length > cols) {
                var lines = [];
                var full_words = text_string.split(" ");
                var words = [];
                // first handle any words that are too long
                for (var w=0; w<full_words.length; w++) {
                    var full_word = full_words[w];
                    while (full_word.length > cols) {
                        words.push(full_word.substring(0, cols));
                        full_word = full_word.substring(cols);
                    }
                    words.push(full_word);
                }
                // now stitch words back into lines
                var line = "";
                for (var w=0; w<words.length; w++) {
                    var word = words[w];
                    if (line.length == 0) {
                        line = word;
                    }
                    else if (word.length + line.length > cols) {
                        lines.push(line);
                        line = word;
                    }
                    else {
                        line += (" " + word);
                    }
                }
                // handle the tail end
                if (line.length > 0)
                    lines.push(line);
                return lines.join("\n");
            }
            return text_string;
        }
        
        var draw_shape = function(shape) {
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
            else if (shape['type'] == 'Line') {
              // define line as 'path': Move then Line: E.g. "M10 10L90 90"
              newShape = paper.path("M"+ shape['x1'] +" "+ shape['y1'] +"L"+ shape['x2'] +" "+ shape['y2'] );
            }
            else if (shape['type'] == 'PolyLine') {
              newShape = paper.path( shape['points'] );
            }
            else if (shape['type'] == 'Polygon') {
              newShape = paper.path( shape['points'] );
            }
            else if (shape['type'] == 'Label') {
              if (shape['textValue']) {
                  newShape = paper.text(shape['x'], shape['y'], shape['textValue']).attr({'text-anchor':'start'});
              }
            }
            // handle transforms. Insight supports: translate(354.05 83.01) and rotate(0 407.0 79.0)
            if (shape['transform']) {
                if (shape['transform'].substr(0, 'translate'.length) === 'translate'){
                    var tt = shape['transform'].replace('translate(', '').replace(')', '').split(" ");
                    var tx = parseInt(tt[0]);   // only int is supported by Raphael
                    var ty = parseInt(tt[1]);
                    newShape.translate(tx,ty);
                }
                else if (shape['transform'].substr(0, 'rotate'.length) === 'rotate'){
                    var tt = shape['transform'].replace('rotate(', '').replace(')', '').split(" ");
                    var deg = parseFloat(tt[0]);
                    var rotx = parseFloat(tt[1]);
                    var roty = parseFloat(tt[2]);
                    newShape.rotate(deg, rotx, roty);
                }
                else if (shape['transform'].substr(0, 'matrix'.length) === 'matrix'){
                    var tt = shape['transform'].replace('matrix(', '').replace(')', '').split(" ");
                    var a1 = parseFloat(tt[0]);
                    var a2 = parseFloat(tt[1]);
                    var b1 = parseFloat(tt[2]);
                    var b2 = parseFloat(tt[3]);
                    var c1 = parseFloat(tt[4]);
                    var c2 = parseFloat(tt[5]);
                    var tmatrix = "m"+a1+","+a2+","+b1+","+b2+","+c1+","+c2;
                    newShape.transform(tmatrix);
                }
            }
            return newShape;
        }
        
        var get_tool_tip = function(shape) {
            var toolTip = "";
            if (shape['type'] == 'Ellipse') {
              toolTip = "cx:"+ shape['cx'] +" cy:"+ shape['cy'] +" rx:"+ shape['rx'] + " ry: "+  shape['ry'];
            }
            else if (shape['type'] == 'Rectangle') {
              toolTip = "x:"+ shape['x'] +" y:"+ shape['y'] +
                " width:"+ shape['width'] + " height: "+  shape['height'];
            }
            else if (shape['type'] == 'Point') {
              toolTip = "cx:"+ shape['cx'] +" cy:"+ shape['cy'];
            }
            else if (shape['type'] == 'Line') {
              toolTip = "x1:"+ shape['x1'] +" y1:"+ shape['y1'] +" x2:"+ shape['x2'] +" y2:"+ shape['y2'];
            }
            else if (shape['type'] == 'PolyLine') {
            }
            else if (shape['type'] == 'Polygon') {
            }
            else if (shape['type'] == 'Label') {
            }
            return toolTip;
        }
        
        // if the currently selected shape is visible - highlight it
        display_selected = function() {
            // *NB: For some reason, can't overlay text with selectedClone.
            // So, for text shapes, we highlight by editing attributes instead.
            if ((selectedClone != null) && (selectedClone.type != 'text')) {
                if (selectedClone.node.parentNode.parentNode) selectedClone.remove();
            }
            if (selected_shape_id == null) return;
            
            selectedClone = null;
            for (var i=0; i<shape_objects.length; i++) {
                var s = shape_objects[i];
                var shape_id = parseInt(s.id);
                
                if (shape_id == selected_shape_id) {
                    if (s.type == 'text') {
                        selectedClone = null;
                        s.attr({'stroke': '#00a8ff'});
                    } else {
                        selectedClone = s.clone();
                        selectedClone.attr({'stroke': '#00a8ff', 'fill': null});
                    }
                } else {
                    if (s.type == 'text') {
                        s.attr({'stroke': null});   // remove stroke
                    }
                }
            }
            return selectedClone;
        }
        
        this.set_selected_shape = function(shape_id) {
            selected_shape_id = shape_id;
            $viewportimg.trigger("shape_click", [shape_id]);
            var sel_shape = display_selected(); 
            var sel_x;
            var sel_y;
            // we will only get the shape if currently displayed (current Z/T section)
            if (sel_shape===null) {
                // otherwise we have to work it out by drawing it
                var bb = null;
                for (var r=0; r<roi_json.length; r++) {
                    if (bb != null)   break;
                    var roi = roi_json[r];
                    var shapes = roi['shapes'];
                    var shape = null;
                    for (var s=0; s<shapes.length; s++) {
                        shape = shapes[s];
                        if (shape['id'] == selected_shape_id) {
                            var newShape = draw_shape(shape);
                            bb = newShape.getBBox();
                            newShape.remove();
                            if (shape['type'] == 'Label'){
                                // bug in BBox for text
                                sel_x = shape['x'] + (bb.width/2);
                                sel_y = shape['y'] + (bb.height/2);
                            } else {
                                sel_x = bb.x + (bb.width/2);
                                sel_y = bb.y + (bb.height/2);
                            }
                        }
                    }
                }
            } else {
                var bb = sel_shape.getBBox();
                sel_x = bb.x + (bb.width/2);
                sel_y = bb.y + (bb.height/2);
            }
            return {'x':sel_x, 'y':sel_y};
        }
        
        // called when user clicks on ROI
        handle_shape_click = function(event) {
            var shape = this;
            var shape_id = parseInt(shape.id);
            self.set_selected_shape(shape_id);
        }

        // load the ROIs from json call and display
        load_rois = function(display_rois) {
            if (json_url == undefined) return;
            
            $.getJSON(json_url, function(data) {
                roi_json = data;

                // plot the rois
                if (display_rois) {
                  rois_displayed = true;
                  refresh_rois();
                }
                $viewportimg.trigger("rois_loaded");
            });
        }

        // returns the ROI data as json. May be null if not yet loaded! 
        this.get_roi_json = function() {
            return roi_json;
        }

        // clears paper and draws ROIs (if rois_displayed) for the given T and Z. NB: indexes are 1-based. 
        this.refresh_rois = function(theZ, theT) {

            if (typeof theZ != 'undefined') this.theZ = theZ;
            if (typeof theT != 'undefined') this.theT = theT;

            paper.clear();
            shape_objects.length = 0;
            if (!rois_displayed) return;
            if (roi_json == null) return;
            

            for (var r=0; r<roi_json.length; r++) {
                var roi = roi_json[r];
                var shapes = roi['shapes'];
                var shape = null;
                for (var s=0; s<shapes.length; s++) {
                    shape = shapes[s];
                    if (((shape.theT == this.theT-1)
                         || typeof shape.theT === "undefined")
                        && ((shape.theZ == this.theZ-1)
                            || typeof shape.theZ === "undefined")) {
                        var newShape = draw_shape(shape);
                        var toolTip = get_tool_tip(shape);
                        // Add text - NB: text is not 'attached' to shape in any way. 
                        if (newShape != null) {
                            if (shape['type'] == 'PolyLine') {
                                newShape.attr({'fill-opacity': 0});
                            }
                            if ((shape['textValue'] != null) && (shape['textValue'].length > 0)) {
                                // Show text 
                                if (shape['type'] == 'Label') {
                                    var txt = newShape; // if shape is label itself, use it
                                    if (shape['strokeColor']) txt.attr({'fill': shape['strokeColor']}); // this is Insight's behavior
                                    txt.attr({'stroke': null });
                                } else if (roi_label_displayed) {
                                    // otherwise, add a new label in the centre of the shape.
                                    var bb = newShape.getBBox();
                                    var textx = bb.x + (bb.width/2);
                                    var texty = bb.y + (bb.height/2);
                                    var text_string = formatShapeText(shape['textValue'])
                                    var txt = paper.text(textx, texty, text_string);    // draw a 'dummy' paragraph to work out it's dimensions
                                    var newY = (texty-txt.getBBox().height/2)+9;
                                    // moving the existing text to newY doesn't seem to work - instead, remove and draw a new one
                                    txt.remove();
                                    txt = paper.text(textx, newY, formatShapeText(shape['textValue'])).attr({'cursor':'default', 'fill': '#000'});
                                    txt_box = txt.getBBox();
                                    var txt_w = txt_box.width*1.3;
                                    var txt_h = txt_box.height*1.3;
                                    var txt_bg = paper.rect(textx-txt_w/2, texty-txt_h/2, txt_w, txt_h);
                                    txt_bg.attr({'cursor':'default', 'fill': '#FFFCB7', 'fill-opacity': 0.78, 'stroke': null});
                                    txt.toFront();
                                    // clicking the text (or text background) should do the same as clicking the shape
                                    txt_bg.id = shape['id'] + "_text_bg";
                                    txt_bg.click(handle_shape_click);
                                    txt.id = shape['id'] + "_shape_text";
                                    txt.click(handle_shape_click);
                                    
                                }
                                
                                // handle other text-specific attributes...
                                var txtAttr = {};
                                if (shape['fontFamily']) {  // model: serif, sans-serif, cursive, fantasy, monospace. #5072
                                    // raphael supports all these exactly - so we can pass directly.
                                    txtAttr['font-family'] = shape['fontFamily'];
                                }
                                if (shape['fontSize']) {
                                    txtAttr['font-size'] = shape['fontSize'];
                                }
                                if (shape['fontStyle']) { // model: Normal, Italic, Bold, Bolditalic
                                    var fs = shape['fontStyle'];
                                    if ((fs == 'Bold') || (fs == 'BoldItalic')) {
                                        txtAttr['font-weight'] = 'bold';
                                    }
                                    if ((fs == 'Italic') || (fs == 'BoldItalic')) {
                                        txtAttr['font-style'] = 'italic';
                                    }
                                }
                                if (txt) txt.attr(txtAttr);
                            }
                            if (shape['type'] != 'Label') {
                                // these shape attributes are not applied to text
                                if (shape['fillColor'] && shape['type'] != 'PolyLine') {
                                    // don't show fills on PolyLines
                                    newShape.attr({'fill': shape['fillColor']});
                                    if (shape['fillAlpha']) { newShape.attr({'fill-opacity': shape['fillAlpha']})}
                                }
                                else {
                                    // need *some* fill so that shape is clickable
                                    newShape.attr({'fill':'#000', 'fill-opacity': 0.01 });
                                }
                                if (shape['strokeAlpha']) { newShape.attr({'opacity': shape['strokeAlpha']}); }
                                if (shape['strokeColor']) { newShape.attr({'stroke': shape['strokeColor']}); }
                                else { newShape.attr({'stroke': '#ffffff'}); }  // white is default
                            }
                            newShape.attr({'cursor':'default'});
                            if (shape['strokeWidth']) { newShape.attr({'stroke-width': shape['strokeWidth']}); }
                            newShape.click(handle_shape_click);
                            newShape.attr({ title: toolTip });
                            newShape.id = shape['id'] + "_shape";
                            shape_objects.push(newShape);
                        }

                    }
                }
            }
            // if the new display includes selected-shape - show it
            display_selected();
        }


        // loads the ROIs if needed and displays them
        this.show_rois = function(theZ, theT) {
            this.theZ = theZ
            this.theT = theT
          if (roi_json == null) {
              load_rois(true);      // load and display
          } else {
              rois_displayed = true;
              this.refresh_rois();
          }
        }
        

        // hides the ROIs from display
        this.hide_rois = function() {
            rois_displayed = false;
            this.refresh_rois();
        }
        
        this.show_labels = function(visible) {
            roi_label_displayed = visible;
            this.refresh_rois();
        }

        // sets the Zoom of the ROI paper (canvas)
        this.setRoiZoom = function(percent) {
            paper.scaleAll(percent/100);
        }

    });

}
