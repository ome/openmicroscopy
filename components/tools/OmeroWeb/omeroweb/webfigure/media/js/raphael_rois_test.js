$(document).ready(function() {
    
    var $t = $("#theT")
    var $z = $("#theZ")
    
    var theT = parseInt( $t.text() );
    var theZ = parseInt( $z.text() );
    var sizeZ = parseInt( $("#sizeZ").text() );
    var sizeT = parseInt( $("#sizeT").text() );
    
    var $roi_table = $('#roi_table');
    var $img_panel = $("#img_panel");
    var $t = $("#theT")
    var $z = $("#theZ")
    
    var json;   // json ROI data for this image
    
    var $canvas = $("#canvas");
    var width = parseInt($img_panel.attr('width'));
    var height = parseInt($img_panel.attr('height'));
    
    var $imgAreaSelect = null
    
    // Creates Raphael canvas 320 × 200 at 10, 50
    var paper = Raphael('canvas', width, height);
    //$canvas.append(paper);
    
    // when shape is selected, add imgAreaSelect to allow resize
    var select_shape = function(event) {
        
        var shape = this;
        var x = parseInt(this.attr('x'));
        var y = parseInt(this.attr('y'));
        var X2 = null;
        var Y2 = null;
        // if no 'x' attribute, we are not dealing with Rect, try Ellipse
        if (isNaN(x)) {
            var cx = parseInt(this.attr('cx'));
            var cy = parseInt(this.attr('cy'));
            var rx = parseInt(this.attr('rx'));
            var ry = parseInt(this.attr('ry'));
            x = cx - rx;
            y = cy - ry;
            X2 = cx + rx;
            Y2 = cy + ry;
        } else {
            X2 = x + parseInt(this.attr('width'));
            Y2 = y + parseInt(this.attr('height'));
        }
        
        var resize = function(img, sel) {
            shape.attr({
                cx: sel.x1 + (sel.width/2),
                cy: sel.y1 + (sel.height/2),
                rx: sel.width/2,
                ry: sel.height/2
            })
        }
        
        // launch selection tool, calling resize when done. 
        $imgAreaSelect = $canvas.imgAreaSelect({ x1:x, y1:y, x2:X2, y2:Y2, 
            handles:true,
            instance: true,     // return instance to save ref
            onSelectEnd: resize}); 
    }
    
    var plot_rois = function() {
        
        if ($imgAreaSelect != null) {
            //$imgAreaSelect.setOptions({ show: false });
            $imgAreaSelect.hide();
        }
        paper.clear();
        
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
                    if (shape['type'] == 'Ellipse') {
                        var circle = paper.ellipse(shape['cx'], shape['cy'], shape['rx'], shape['ry']);
                        //circle.attr("stroke", "#fff");
                        circle.attr({
                            fill: "#000",
                            stroke: "#fff",
                            opacity: 0.3
                        });
                        circle.drag(move, start, up);
                        circle.click(select_shape);
                    }
                    else if (shape['type'] == 'Rectangle') {
                        var rect = paper.rect(shape['x'], shape['y'], shape['width'], shape['height']);
                        rect.attr({
                            fill: "#000",
                            stroke: "#fff",
                            opacity: 0.3
                        });
                        rect.drag(moveRect, start, up);
                        rect.click(select_shape);
                    }
                    else if (shape['type'] == 'Point') {
                        var point = paper.ellipse( shape['cx'], shape['cy'], 2, 2);
                        point.attr({
                            fill: "#000",
                            stroke: "#fff",
                            opacity: 0.3
                        });
                        point.drag(move, start, up);
                        point.click(select_shape);
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
        
        //alert("update table");
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
    
});