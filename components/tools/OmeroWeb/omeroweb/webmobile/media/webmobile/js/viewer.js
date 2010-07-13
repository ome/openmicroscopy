$(document).ready(function() {
    
    $(".controls").hide();
    
    var z = 0;
    var t = 0;
    
    // elements we need repeatedly
    var imageId = $("#imageId").text();
    var $imagePlane = $("#imagePlane");
    var $imageContainer = $("#imageContainer");
    
    // --- functions ---
    
    // update the image with the current z and t indexes
    var refreshImage = function() {
        var imgSrc = "/webgateway/render_image/"+ imageId + "/" + z + "/" + t + "/";
        $("#imagePlane").attr('src', imgSrc);
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
        var zHeight = scrollH - w - 2
        var tWidth = scrollW - w - 2
        
        $("#zControls").css('top', scrollY).css('left', scrollX).css('width', w).css('height', zHeight);
        $("#tControls").css('top', scrollY+zHeight+1).css('left', scrollX+w+1).css('height', w).css('width', tWidth);
        $(".arrow").css('width', w).css('height', w);
        $(".controls").show();
        $imagePlane.one('click', hideControls);
    };
    
    // hides the controls
    var hideControls = function() {
        $imagePlane.unbind('click', hideControls);  // in case hideControls wasn't called from imagePlane
        $(".controls").hide();
        $imagePlane.one('click', showControls);
    };
    
    
    // -- bind various functions to controls --
    
    // The Z arrows and T arrows increment Z or T and refresh the image
    $(".arrow").click(function(event) {
        if (this.id == 'zUp') z += 1;
        if (this.id == 'zDown') z -= 1;
        if (this.id == 'tRight') t += 1;
        if (this.id == 'tLeft') t -= 1;
        refreshImage();
    })
    
    // Show controls when the iamge is clicked - next click will hide controls - etc 
    $imagePlane.one('click', showControls);
    
    
    // -- stuff that happens when the page loads --
    
    // when the page loads, first need to get the default Z and T from imgData JSON, then load image
    var json;
    $.getJSON("/webgateway/imgData/" + imageId + "/", function(data) {
        json = data;
        z = json['rdefs']['defaultZ'];
        refreshImage();
    });
    
    // make the 'image container' bigger that the image according to viewport so that all image is within
    var scrollW = window.innerWidth;
    var scrollH = window.innerHeight;
    var portWH = scrollW / scrollH;
    var imgW = parseFloat( $imagePlane.css('width'), 10 );
    var imgH = parseFloat( $imagePlane.css('height'), 10 );
    var imageWH = imgW/imgH;
    
    // if imageWH > portWH, image container is same width as image, height is bigger
    if (imageWH > portWH) {
        var containerW = imgW;
        var containerH = containerW / portWH;
        //alert(containerW + " " + containerH);
        $imageContainer.css('height', containerH + "px").css('width', containerW + "px");
        var cont = 'device-width = '+ imgW + ', width = '+ imgW + ', minimum-scale = 1, maximum-scale = 5';
        //alert(cont);
        $("#viewport").attr('content', cont);
        
        var topSpacer = (containerH - imgH) / 2;
        $imagePlane.css('top', topSpacer + "px");
    }
    
    // start a timer to check for viewport movement every half sec 
    setInterval(checkHideControls, 500);
    
});