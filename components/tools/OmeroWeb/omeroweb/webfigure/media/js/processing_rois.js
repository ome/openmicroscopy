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
    
    // Simple way to attach js code to the canvas is by using a function  
    function draw_rois(processing) {  
        
        processing.setup = function(){
          // size( 256, 256 );
          processing.strokeWeight( 2 );
          processing.frameRate( 15 );
          //cx = processing.width / 2;
          //cy = processing.width / 2;    
        }
        
        var plot_rois = function() {
            
            processing.background(0,0); // clear bg
            
            processing.fill( 0, 0,0,0 );    // black, transparent
            processing.stroke(0,255,0);
            
            for (var r=0; r<json.length; r++) {
                var roi = json[r];
                var shapes = roi['shapes'];
                var shape = null;
                for (var s=0; s<shapes.length; s++) {
                    shape = shapes[s];
                    if ((shape['theT'] == theT) && (shape['theZ'] == theZ)) {
                        if (shape['type'] == 'Ellipse') {
                            processing.ellipse( shape['cx'], shape['cy'], shape['rx'], shape['ry'] );
                        }
                        else if (shape['type'] == 'Rectangle') {
                            processing.rect(shape['x'], shape['y'], shape['width'], shape['height']);
                        }
                        else if (shape['type'] == 'Point') {
                            processing.ellipse( shape['cx'], shape['cy'], 2, 2);
                        }
                    }
                }
            }
        }
        processing.plot_rois = plot_rois;
        
      // Override draw function, by default it will be called 60 times per second  
      processing.draw = function() {  
        
        //processing.loadPixels();
        //for (int i = 0; i < processing.width * processing.height; i++) {
        //    processing.pixels[i] = 0;
        //}
        //processing.updatePixels(); 
         
      };  
    
    }  
  
    var canvas = document.getElementById("canvas"); 
     
    // attaching the sketchProc function to the canvas  
    var p = new Processing(canvas, draw_rois);  
    //p.exit(); //to detach it
    
    // when the user clicks 'LOAD ROIs' we get the ROI data as json
    $("#load_rois").click(function() {
        
        var roi_url = $(this).attr('href')
        $.getJSON(roi_url, function(data) {
            json = data;
            
            // json is list of ROIs
            refresh_roi_table();
            
            // plot the rois using processing.js
            p.plot_rois();
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
            p.plot_rois();
        } else {
            theZ -= inc;
        }
    }
    
    var incrementZ = function(inc) {
        theZ += inc;
        if ((theZ < (sizeZ)) && (theZ > -1))  {
            refreshImage();
            p.plot_rois();
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
            p.plot_rois();
            $z.text(theZ);
            $t.text(theT);
            
        // if we clicked on a ROI row, toggle the shapes below. 
        } else if ($row.attr('class') == 'roi_row') {
            var $tbody = $row.parent().next();
            $tbody.toggle();
        }
    });
    
});