$(document).ready(function() {
    
    var $t = $("#theT")
    var $z = $("#theZ")
    
    var theT = parseInt( $t.text() );
    var theZ = parseInt( $z.text() );
    var sizeZ = parseInt( $("#sizeZ").text() );
    var sizeT = parseInt( $("#sizeT").text() );
    
    var zoom = 100;
    
    var $roi_table = $('#roi_table');
    var $t = $("#theT")
    var $z = $("#theZ")
    
    var json;   // json ROI data for this image
    
    // 3 levels of panels that make up the image plane
    var $img_panel = $("#img_panel");   // lowest level - has the image itself
    var $canvas = $("#canvas");         // contains the 'paper' for ROIs
    var $imgOverlay = $("#imgOverlay").css('border', 'solid red 1px').hide(); // div for imgAreaSelect (only shown when ROI is selected)
    
    var width = parseInt($img_panel.attr('width'));
    var height = parseInt($img_panel.attr('height'));
    
    var $imgAreaSelect = null;  // TODO: remove - unless I can find how to get imgAreaSelect instance. 
    
    // Creates Raphael canvas. Uses scale.raphael.js to provide paper.scaleAll(ratio);
    var paper = new ScaleRaphael('canvas', width, height);
    
    // for keeping track of objects - NOT used yet.
    var roi_objects = new Array();
    
    // this is how to add functions to Raphael objects - NOT used now. 
    Raphael.el.zoomRoi = function (zoomF) {
        var zm = zoomF/100
        this.scale(zm, zm, zm, zm);
    };
    
    // handle zoom
    var handleZoom = function(increment) {
        zoom += increment;
        var newWidth = parseInt((zoom*width)/100);
        var newHeight = parseInt((zoom*height)/100);
        $img_panel.attr({width: newWidth, height: newHeight});
        
        // resize canvas
        $canvas.css({'width':newWidth, 'height':newHeight});
        // sclae the paper
        paper.scaleAll(zoom/100);
        // resize the overlay used for imgAreaSelect
        $imgOverlay.css({'width':newWidth, 'height':newHeight});
    }
    
    // when shape is selected, add imgAreaSelect to allow resize
    var select_shape = function(event) {
        var shape = this;
        var bb = shape.getBBox();
        x = bb.x;
        y = bb.y
        X2 = x + bb.width
        Y2 = y + bb.height
        
        // resize method (bound to end of image area selection)
        var resize = function(img, sel) {
            if ((sel.x1 == 0) && (sel.y1 == 0) && (sel.width == 0) && (sel.height == 0)) {
                //$imgOverlay.hide();   // Doesn't have desired effect of hiding selection box. 
            } else {
                var selx1 = sel.x1*100/zoom;
                var sely1 = sel.y1*100/zoom;
                var selh = sel.height*100/zoom;
                var selw = sel.width*100/zoom;
                if (shape.attr('cx') != null) {
                    // our shape is a circle - resize to bounding box
                    shape.attr({
                        cx: selx1 + (selw/2),
                        cy: sely1 + (selh/2),
                        rx: selw/2,
                        ry: selh/2
                    });
                } else {
                    // handle resize of rectangle
                    shape.attr({
                        x: selx1,
                        y: sely1,
                        width: selw,
                        height: selh
                    });
                }
            }
        }
        
        $imgOverlay.show();
        // launch selection tool, calling resize when done. 
        x = x*zoom/100
        y = y*zoom/100
        X2 = X2*zoom/100
        Y2 = Y2*zoom/100
        $imgOverlay.imgAreaSelect({ x1:x, y1:y, x2:X2, y2:Y2, 
            handles:true,
            onSelectEnd: resize}); 
    }
    
    var plot_rois = function() {
        
        // hide the layer that does ROI selection: imgAreaSelect
        $imgOverlay.hide();
        $imgOverlay.click();
        // clear the paper and the list of ROIs we have. 
        paper.clear();
        roi_objects.length = 0;
        
        // The following functions are for handling drag n drop. May use imgAreaSelect instead? 
        var start = function () {
            // storing original coordinates
            this.ox = this.attr("cx");
            this.oy = this.attr("cy");
            this.rectx = this.attr("x");
            this.recty = this.attr("y");
            this.attr({opacity: 0.5});
        },
        move = function (dx, dy) {
            // move will be called with dx and dy
            this.attr({cx: this.ox + dx, cy: this.oy + dy});
        },
        up = function () {
            // restoring state
            this.attr({opacity: .3});
        },
        moveRect = function (dx, dy) {
            // move will be called with dx and dy
            this.attr({x: this.rectx + dx, y: this.recty + dy});
        }
        
        for (var r=0; r<json.length; r++) {
            var roi = json[r];
            var shapes = roi['shapes'];
            var shape = null;
            for (var s=0; s<shapes.length; s++) {
                shape = shapes[s];
                if ((shape['theT'] == theT) && (shape['theZ'] == theZ)) {
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
                        newShape.click(select_shape);
                        if ((shape['textValue'] != null) && (shape['textValue'].length > 0)) {
                            newShape.attr({ title:shape['textValue'] });
                        }
                    }
                }
            }
        }
    }
    
    // when the user clicks 'LOAD ROIs' we get the ROI data as json
    $("#load_rois").click(function() {
        
        var roi_url = $(this).attr('href')
        $.getJSON(roi_url, function(data) {
            json = data;
            
            // json is list of ROIs
            refresh_roi_table();
            
            // plot the rois using processing.js
            plot_rois();
        });
        
        return false;   // don't follow ROI json link
    });
    
    var refresh_roi_table = function() {
        
        $roi_table.find('tbody').remove();
        
        // populate table. Cols are: ID, T, Z, Shape
        for (var r=0; r<json.length; r++) {
            var roi = json[r];
            var shapes = roi['shapes'];
            
            // process the shapes first - note first shape
            var firstShape = null;
            var shapesHtml = '<tbody>';
            var shape = null;
            for (var s=0; s<shapes.length; s++) {
                shape = shapes[s];
                shapesHtml += "<tr class='shape_row'><td>" + shape['id'] + "</td>";
                shapesHtml += "<td>" + shape['theT'] + "</td>";
                shapesHtml += "<td>" + shape['theZ'] + "</td>";
                shapesHtml += "<td>" + shape['type'] + "</td></tr>";
                if (firstShape == null) firstShape = shape['type'];
            }
            shapesHtml += '</tbody>';
            
            // new tbody for each ROI
            var roi_html = "<thead><tr class='roi_row'>";
            roi_html += "<th>" + roi['id'] + "</th>";
            roi_html += "<th>T</th>"; // no T for ROI
            roi_html += "<th>Z</th>"; // no Z for ROI
            roi_html += "<th>" + firstShape + " (" + shapes.length + ")</th>"; // first shape (count)
            roi_html += "</tr></thead>";
            
            $roi_table.append($(roi_html));             // add the roi thead
            $roi_table.append($(shapesHtml).hide());    // add the tbody, hiding it initially. 
        }
        
    }
    
    // functions for incrementing time or Z
    var incrementTime = function(inc) {
        theT += inc;
        if ((theT < (sizeT)) && (theT > -1))  {
            $t.text(theT);
            refreshImage();
            plot_rois();
        } else {
            theZ -= inc;
        }
    }
    
    var incrementZ = function(inc) {
        theZ += inc;
        if ((theZ < (sizeZ)) && (theZ > -1))  {
            refreshImage();
            plot_rois();
            $z.text(theZ);
        } else {
            theZ -= inc;
        }
    }
    
    var refreshImage = function() {
        var imageId = $("#imageId").text();
        var imgSrc = "/webgateway/render_image/"+ imageId + "/" + theZ + "/" + theT + "/";
        $img_panel.attr('src', imgSrc);
    }
    
    // bind controls for incrementing T and Z
    $("#incTime").click(function() {
        incrementTime(1);
    });
    $("#decTime").click(function() {
        incrementTime(-1);
    });
    
    $("#incZ").click(function() {
        incrementZ(1);
    });
    $("#decZ").click(function() {
        incrementZ(-1);
    });
    
    // clicking on a shape in the roi_table moves the image to that plane and plots ROIs. 
    $roi_table.click(function(event) {
        var $target = $(event.target);
        var $row = $target.parent();
        if ($row.attr('class') == 'shape_row') {
            var shapeT = $row.find('td:nth-child(2)').text();
            var shapeZ = $row.find('td:nth-child(3)').text();
            theT = parseInt( shapeT );
            theZ = parseInt( shapeZ );
            refreshImage();
            plot_rois();
            $z.text(theZ);
            $t.text(theT);
            
        // if we clicked on a ROI row, toggle the shapes below. 
        } else if ($row.attr('class') == 'roi_row') {
            var $tbody = $row.parent().next();
            $tbody.toggle();
        }
    });
    
    // bind mouse-wheel over the canvas to zoom
    /**
     * Handle Zoom by mousewheel (FF)
     */
    if ($canvas.get(0).addEventListener) {
      // Respond to mouse wheel in Firefox - get(0) the underlying DOM object.
      $canvas.get(0).addEventListener('DOMMouseScroll', function(e) {
        if (e.detail > 0)
          handleZoom(-1, true);
        else if (e.detail < 0)
          handleZoom(1, true);
        
        e.preventDefault();
      }, false);
    } else {
        alert("can't add eventlistener");
    }
    
});