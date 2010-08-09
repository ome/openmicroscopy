$(document).ready(function() {
    
    $(".controls").hide();
    
    var z = 0;
    var t = 0;
    var sizeZ = 1;
    var sizeT = 1;
    var pixelSize = 0;
    var json;
    
    // elements we need repeatedly
    var imageId = $("#imageId").text();
    var $imagePlane = $("#imagePlane");
    var $imageContainer = $("#imageContainer");
    var $zSlider = $("#zSlider");
    var $tSlider = $("#tSlider");
    var $infoIcon = $("#infoIcon");
    var $infoPanel = $("#infoPanel");
    
    // --- functions ---
    
    var showInfoPanel = function() {
        
        var iHtml = buildImageInfo(json);
        $infoPanel.empty();
        $infoPanel.append($(iHtml));
        
        var scrollX = window.pageXOffset; 
        var scrollY = window.pageYOffset; 
        var scrollW = window.innerWidth;
        var scrollH = window.innerHeight;
        
        var font = 150 * scrollW/480 + "%";
        var w = scrollW / 8;
        
        $infoPanel.css('top', scrollY).css('left', scrollX).css('font-size', font)
            .css('padding', scrollW/50);
            
        $infoPanel.show();
        hideControls();
    }
    
    var buildImageInfo = function(jsonData) {
        // html of image metadata. Start with name...
        var infoHtml = "<h2>"+ jsonData["meta"]["imageName"] + "</h2>";
        
        // table of metadata...
        infoHtml += "<table>";
        var labels = ["ID", "Owner", "Description", "Project", "Dataset"];
        var metaKeys = ["imageId", "imageAuthor", "imageDescription", "projectName", "datasetName"];
        for (var i=0; i<labels.length; i++) {
            infoHtml += "<tr><td>" + labels[i] + "</td><td>" + jsonData["meta"][metaKeys[i]] + "</td></tr>";
        }
        // ..pixel sizes...
        try {
            var x = parseFloat(jsonData["pixel_size"]["x"]).toFixed(2)
            var y = parseFloat(jsonData["pixel_size"]["y"]).toFixed(2)
            var z = parseFloat(jsonData["pixel_size"]["z"]).toFixed(2)
            infoHtml += "<tr><td>Pixel Sizes (x,y,z)</td><td>" + x + ", " + y + ", " + z + " &micro;m</td></tr>";
        }
        catch(err) {}
        
        // ..image dimensions...
        var width = jsonData["size"]["width"];
        var height = jsonData["size"]["height"];
        infoHtml += "<tr><td>Image size (x,y)</td><td>" + width + ", " + height + "</td></tr>";
        var z = jsonData["size"]["z"];
        var t = jsonData["size"]["t"];
        infoHtml += "<tr><td>Image size (z,time)</td><td>" + z + ", " + t + "</td></tr>";
        infoHtml += "<table>";
        
        // Channels table...
        infoHtml += "<table>";
        var clist = jsonData["channels"];
        for (var c=0; c<clist.length; c++) {
            var cdata = clist[c];
            var colour = cdata["color"];
            infoHtml += "<tr><td bgcolor='#" + colour + "'>&nbsp &nbsp&nbsp</td><td>" + cdata["label"] + "</td></tr>";
        }
        
        return infoHtml;
    };
    
    // update the image with the current z and t indexes
    var refreshImage = function() {
        var imgSrc = "/webgateway/render_image/"+ imageId + "/" + z + "/" + t + "/";
        $("#imagePlane").attr('src', imgSrc);
        // find the slider positions
        if (sizeZ > 1) {
            $(".zPoint").css('background', 'none');
            $("#z"+z).css('background', 'red');
        }
        if (sizeT > 1) {
            $(".tPoint").css('background', 'none');
            $("#t"+t).css('background', 'red');
        }
    };
    
    // call this periodiclly to check whether the viewport has moved. If so, hide 
    var checkHideControls = function() {
        var scrollX = window.pageXOffset; 
        var scrollY = window.pageYOffset;
        var x = parseFloat( $("#zControls").css('left'), 10 );
        var y = parseFloat( $("#zControls").css('top'), 10 );
        if ((x != scrollX) || (y != scrollY)){
            hideControls();
        }
    };
    
    // this positions the controls within the current viewport and shows them 
    var showControls = function() {
        
        var scrollX = window.pageXOffset; 
        var scrollY = window.pageYOffset; 
        var scrollW = window.innerWidth;
        var scrollH = window.innerHeight;
        
        var w = scrollW / 8;
        var zHeight = scrollH - w
        var tWidth = scrollW - w
        
        // sliders
        $("#zControls").css('top', scrollY).css('left', scrollX).css('width', w).css('height', zHeight);
        $("#zBg").css('width', w).css('height', zHeight);
        $("#tControls").css('top', scrollY+zHeight).css('left', scrollX+w).css('height', w).css('width', tWidth);
        $("#tBg").css('height', w).css('width', tWidth);
        $(".arrow").css('width', w).css('height', w);
        $zSlider.css('top', w).css('height',zHeight-w-w-2 ).css('width',w-2);
        $tSlider.css('height', w-2 ).css('width',tWidth-w-w-2).css('left',w).css('top',1);
        
        // info icon - top right corner
        var iconW = scrollW / 10;
        var margin = scrollW/70;
        $infoIcon.css('top', scrollY+margin).css('left', scrollX+scrollW-iconW-margin).css('width', iconW).css('height', iconW);
        
        // scalebar 
        if (pixelSize == 0) {
            $("#scalebar").css('opacity', 0.0); // hide so it won't be shown
        } else {
            var scaleW = scrollW / 3;
            var scaleMicrons = (scaleW * pixelSize);
            var barMicrons = parseInt(scaleMicrons / 5) * 5;
            if (barMicrons == 0) barMicrons = parseInt(scaleMicrons);
            scaleW = barMicrons /pixelSize;
            var scaleH = scrollH / 100;
            var indent = w/3;
            $("#scalebar").css('height', scaleH).css('width', scaleW)
            .css('top', scrollY+scrollH-w-scaleH-indent).css('left',scrollX+scrollW-scaleW-indent);
            
            var $scaleNumber = $("#scaleNumber");
            var numW = scaleW/4;
            var numH = scrollW/480 * 40;
            var font = 200 * scrollW/480 + "%";
            var top = scrollY+scrollH-w-scaleH-indent-numH;
            $("#scaleNumber").text(barMicrons)
                .css('width', numW).css('height', numH).css('font-size', font)
                .css('top', top).css('left', scrollX+scrollW-indent-numW);
        }
        $(".controls").show();
        $imagePlane.one('click', hideControls);
    };
    
    // hides the controls
    var hideControls = function() {
        $imagePlane.unbind('click', hideControls);  // in case hideControls wasn't called from imagePlane
        $(".controls").fadeOut();
        //$infoPanel.fadeOut(); 
        $imagePlane.one('click', showControls);
    };
    
    
    // -- bind various functions to controls --
    
    // Show info
    $infoIcon.click(showInfoPanel);
    
    // When a slider is clicked, try to identify the actual increment that was clicked, set z or t and refresh
    $zSlider.click(function(event) {
        var zId = event.target.id;      // E.g. 'z10'
        var zIndex = parseFloat(zId.replace("z",""), 10 );
        if (!isNaN(zIndex)) {
            z = zIndex;
            refreshImage();
        }
    });
    $tSlider.click(function(event) {
        var tId = event.target.id;      // E.g. 't10'
        var tIndex = parseFloat(tId.replace("t",""), 10 );
        if (!isNaN(tIndex)) {
            t = tIndex;
            refreshImage();
        }
    });
    
    // The Z arrows and T arrows increment Z or T and refresh the image
    $(".arrow").click(function(event) {
        if ((this.id == 'zUp') && (z < sizeZ-1)) z += 1;
        if ((this.id == 'zDown') && (z>0)) z -= 1;
        if ((this.id == 'tRight') && (t < sizeT-1)) t += 1;
        if ((this.id == 'tLeft') && (t>0)) t -= 1;
        refreshImage();
    });
    
    // Show controls when the iamge is clicked - next click will hide controls - etc 
    $imagePlane.one('click', showControls);
    
    // Hide info panel when it's clicked
    $infoPanel.click(function() {
        $infoPanel.hide();
        return false;
    });
    
    
    // -- stuff that happens when the page loads --
    
    // make the 'image container' bigger that the image according to viewport so that all image is within
    var scrollW = window.innerWidth;
    var scrollH = window.innerHeight;
    var portWH = scrollW / scrollH;
    var imgW = parseFloat( $imagePlane.css('width'), 10 );
    var imgH = parseFloat( $imagePlane.css('height'), 10 );
    var imageWH = imgW/imgH;
    
    // when the page loads, first need to get the default Z and T from imgData JSON, then load image
    $.getJSON("/webgateway/imgData/" + imageId + "/", function(data) {
        json = data;
        z = json['rdefs']['defaultZ'];
        t = json['rdefs']['defaultT'];
        pixelSize = json['pixel_size']['x'];

        // build z and t sliders
        sizeZ = json['size']['z'];
        sizeT = json['size']['t'];
        var html = "<table height='100%' width='100%' cellspacing='0' border='0'>";
        for (var zz=sizeZ-1; zz>=0; zz--) {
            html += "<tr><td id='z" + zz +"' class='zPoint'></td></tr>";    // each table row is a z-increment
        }
        html += "</table>";
        $zSlider.append($(html));
        
        html = "<table height='100%' width='100%' cellspacing='0' border='0'><tr>";
        for (var tt=0; tt<sizeT; tt++) {
            html += "<td id='t" + tt +"' class='tPoint'></td>";    // each table col is a t-increment
        }
        html += "</tr></table>";
        $tSlider.append($(html));
        
        // update sliders and image plane
        refreshImage();
    });
    
    // set opacity via jquery (browser consistency)
    $(".controlBg").css('opacity', 0.5);
    $(".controls").hide();
    
    // start a timer to check for viewport movement every half sec 
    setInterval(checkHideControls, 500);
    
    // if imageWH > portWH, image container is same width as image, height is bigger
    if (imageWH > portWH) {
        var containerW = imgW;
        var containerH = containerW / portWH;
        //alert(containerW + " " + containerH);
        //$imageContainer.css('height', containerH + "px").css('width', containerW + "px");
        //var cont = 'device-width = '+ imgW + ', width = '+ imgW + ', minimum-scale = 1, maximum-scale = 1, initial-scale = 1';
        //alert(cont);
        // $("#viewport").attr('content', cont);
        
        var topSpacer = (containerH - imgH) / 2;
        $imagePlane.css('top', topSpacer + "px");
    }
});