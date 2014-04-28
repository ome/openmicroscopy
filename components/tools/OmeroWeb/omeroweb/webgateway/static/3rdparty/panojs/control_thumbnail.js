/*******************************************************************************
  ThumbnailControl - creates thumbnail navigator and listens to the viewer for
                     view transformations
  
  GSV 3.0 : PanoJS3
  @author Dmitry Fedorov  <fedorov@ece.ucsb.edu>   
  
  Copyright (c) 2010 Dmitry Fedorov, Center for Bio-Image Informatics
  
  using: isClientTouch() and isClientPhone() from utils.js

*******************************************************************************/

PanoJS.CONTROL_THUMBNAIL_SHOW_MINIMIZE = true;
PanoJS.CONTROL_THUMBNAIL_STYLE = "position: absolute; z-index: 60; opacity:0.5; filter:alpha(opacity=50); ";
PanoJS.CONTROL_IMAGE_PLUS      = "16px_plus.png";
PanoJS.CONTROL_IMAGE_MINUS     = "16px_minus.png";
PanoJS.CONTROL_IMAGE_PROGRESS  = "progress_128.gif";

function trim(v, l, h) {
  if (v<l) return l;
  else
  if (v>h) return h;
  else
  return v;    
}  

function ThumbnailControl(viewer) {
  this.move_delay_ms = viewer.delay_ms;  // Delay before moving the viewer
  
  this.viewer = viewer;
  this.initControls();   
  this.createDOMElements();

  this.scale = 1;
  this.x = 0;
  this.y = 0;  
  this.width = 128;  
  this.height = 128; 
  
  this.viewer.addViewerMovedListener(this);
  this.viewer.addViewerZoomedListener(this);
  this.viewer.addViewerResizedListener(this);    
    
  // load thumbnail image
  this.update();
}

ThumbnailControl.prototype.initControls = function() {
  if (PanoJS.CONTROL_THUMBNAIL_UPDATED_URLS) return;
  PanoJS.CONTROL_IMAGE_PLUS     = PanoJS.STATIC_BASE_URL + PanoJS.CONTROL_IMAGE_PLUS;
  PanoJS.CONTROL_IMAGE_MINUS    = PanoJS.STATIC_BASE_URL + PanoJS.CONTROL_IMAGE_MINUS;
  PanoJS.CONTROL_IMAGE_PROGRESS = PanoJS.STATIC_BASE_URL + PanoJS.CONTROL_IMAGE_PROGRESS;
  PanoJS.CONTROL_THUMBNAIL_UPDATED_URLS = true;
}

ThumbnailControl.prototype.update = function() {
  this.dom_image.onload = callback(this, this.init);
  this.dom_image.src = this.viewer.thumbnailURL();
}

ThumbnailControl.prototype.init = function() {
  this.dom_image.onload = null;  
  if (this.dom_image_progress 
      && this.dom_image_progress.parentNode)
    this.dom_element.removeChild(this.dom_image_progress);   
  
  // the thumbnail image may be larger that the space allocated for the thumbnail view
  // resize control accordingly to the larger side
  if (this.dom_image.width >= this.dom_image.height) {
    this.dom_image.width = PanoJS.CONTROL_THUMBNAIL_SIZE;
    this.dom_element.style.height = this.dom_image.height+'px';
  } else { // if (this.dom_image.width < this.dom_image.height)
    this.dom_image.height = PanoJS.CONTROL_THUMBNAIL_SIZE;    
    this.dom_element.style.width = this.dom_image.width+'px';    
  }
  
  // store thumbnmail control size for maximizing
  this.dom_width = this.dom_element.style.width;
  this.dom_height = this.dom_element.style.height;  
  
  this.tw = this.dom_image.width;
  this.th = this.dom_image.height;  
  this.thumbscale = this.tw / this.viewer.imageSize().width;

  this.viewer.notifyViewerZoomed();
  this.viewer.notifyViewerMoved();
}

ThumbnailControl.prototype.createDOMElements = function() {
    var de = this.viewer.viewerDomElement();

    this.dom_element = document.createElement('div');
    this.dom_element.className = 'thumbnail';
    de.appendChild(this.dom_element); 
    PanoJS.CONTROL_THUMBNAIL_SIZE = Math.max(this.dom_element.clientWidth, this.dom_element.clientHeight);
    PanoJS.CONTROL_THUMBNAIL_BORDER = (this.dom_element.offsetWidth - this.dom_element.clientWidth) / 2;
      
    this.dom_surface = document.createElement('div');
    this.dom_surface.className = 'thumbnail_surface';
    this.dom_element.appendChild(this.dom_surface); 

    this.dom_roi = document.createElement('div');
    this.dom_roi.className = 'thumbnail_roi';
    this.dom_element.appendChild(this.dom_roi); 

    this.dom_roi_prev = document.createElement('div');
    this.dom_roi_prev.className = 'thumbnail_roi_preview';
    this.dom_element.appendChild(this.dom_roi_prev); 
    
    this.dom_scale = document.createElement('span');
    this.dom_scale.className = 'thumbnail_scale';
    this.dom_element.appendChild(this.dom_scale); 

    this.dom_image_progress = document.createElement('img');
    this.dom_element.appendChild(this.dom_image_progress); 
    this.dom_image_progress.width = '128';
    this.dom_image_progress.height = '128';    
    this.dom_image_progress.src = PanoJS.CONTROL_IMAGE_PROGRESS;
    
    this.dom_image = document.createElement('img');
    this.dom_element.appendChild(this.dom_image); 

    if (isIE()) {
        // Using dom_element instead of dom_surface as IE gets the mouse events on the
        // dom_image element and bubbles up from there, making dom_surface never
        // see them.
        this.dom_element.onmousedown = callback (this, this.onmousedown );
        this.dom_element.onmouseup   = callback (this, this.onmouseup );
        this.dom_element.onmousemove = callback (this, this.onmousemove );
        this.dom_element.onmouseout  = callback (this, this.onmouseout );
    } else {
        // Using dom_element instead of dom_surface as IE gets the mouse events on the
        // dom_image element and bubbles up from there, making dom_surface never
        // see them.
        this.dom_surface.onmousedown = callback (this, this.onmousedown );
        this.dom_surface.onmouseup   = callback (this, this.onmouseup );
        this.dom_surface.onmousemove = callback (this, this.onmousemove );
        this.dom_surface.onmouseout  = callback (this, this.onmouseout );
    }
    
    if (PanoJS.CONTROL_THUMBNAIL_SHOW_MINIMIZE) {
        var style = PanoJS.CONTROL_THUMBNAIL_STYLE + " bottom: 16px; right: 1px; width: 16px;"
        this.btn = document.createElement('span');
        this.dom_element.appendChild(this.btn);
        this.btn.setAttribute("style", style);
        this.btn.style.cssText = style;   
        
        this.img = document.createElement('img');
        this.img.src = PanoJS.CONTROL_IMAGE_MINUS;
        if (this.btn.style.width) this.img.style.width = this.btn.style.width;
        this.btn.appendChild(this.img);    
        
        this.btn.onclick = callback(this, this.toggleMinimize); 
    }
}

ThumbnailControl.prototype.toggleMinimize = function(e) {
    if (!this.minimized) this.minimized=false;
    this.minimized = !this.minimized;

    if (this.minimized) {
        this.img.src = PanoJS.CONTROL_IMAGE_PLUS;
        this.dom_element.style.width = '17px';
        this.dom_element.style.height = '17px'; 

        this.dom_surface.style.display  = 'none';
        this.dom_roi.style.display      = 'none';
        this.dom_roi_prev.style.display = 'none';        
        this.dom_scale.style.display    = 'none';
        this.dom_image.style.display    = 'none';
        
    } else {
        this.img.src = PanoJS.CONTROL_IMAGE_MINUS;
        this.dom_element.style.width = this.dom_width;
        this.dom_element.style.height = this.dom_height;
        
        this.dom_surface.style.display  = '';
        this.dom_roi.style.display      = '';
        this.dom_roi_prev.style.display = '';               
        this.dom_scale.style.display    = '';
        this.dom_image.style.display    = '';
    }
}

ThumbnailControl.prototype.viewerMoved = function(e) {
    if (this.dom_image.onload) return
    if (!this.dom_roi || typeof this.dom_roi == 'undefined') return;
    var img_x = -1.0 * (e.x / this.scale);
    var img_y = -1.0 * (e.y / this.scale);  
    var tx = trim( img_x * this.thumbscale, 0, this.tw);
    var ty = trim( img_y * this.thumbscale, 0, this.th);
    var w = trim(this.width, 0, this.viewer.imageSize().width-img_x);
    var h = trim(this.height, 0, this.viewer.imageSize().height-img_y);
    if (img_x<0) w += img_x;
    if (img_y<0) h += img_y;  

    this.dom_roi.style.left = tx + 'px';
    this.dom_roi.style.top  = ty + 'px';   
    this.dom_roi.style.width = trim(1, w*this.thumbscale-2, this.tw-tx-PanoJS.CONTROL_THUMBNAIL_BORDER) + 'px';
    this.dom_roi.style.height = trim(1, h*this.thumbscale-2, this.th-ty-PanoJS.CONTROL_THUMBNAIL_BORDER) + 'px';
    
    this.dom_roi_prev.style.left = tx + 'px';
    this.dom_roi_prev.style.top  = ty + 'px';   
    this.dom_roi_prev.style.width = trim(1, w*this.thumbscale-2, this.tw-tx-PanoJS.CONTROL_THUMBNAIL_BORDER) + 'px';
    this.dom_roi_prev.style.height = trim(1, h*this.thumbscale-2, this.th-ty-PanoJS.CONTROL_THUMBNAIL_BORDER) + 'px';
}

ThumbnailControl.prototype.viewerZoomed = function(e) {
    this.scale  = e.scale;
    this.width  = e.width;
    this.height = e.height;
    //if (this.dom_scale) this.dom_scale.innerHTML = this.scale*100 + '%';   
    this.viewerMoved(e);
}

ThumbnailControl.prototype.viewerResized = function(e) {
    this.width  = e.width;
    this.height = e.height;
    this.viewerMoved(e);
}

ThumbnailControl.prototype.moveViewer = function (e) {
    if (!this.viewer) return;
    var mx = e.offsetX != undefined ? e.offsetX : e.layerX;
    var my = e.offsetY != undefined ? e.offsetY : e.layerY; 
    var x = (mx / this.thumbscale);
    var y = (my / this.thumbscale);   
    
    this.viewer.resetSlideMotion();
    PanoJS.USE_SLIDE = false;
    this.viewer.recenter( this.viewer.toViewerFromImage({'x': x, 'y': y}), true, true );
    PanoJS.USE_SLIDE = true;    
}

ThumbnailControl.prototype.movePreview = function (e) {
    var mx = e.offsetX != undefined ? e.offsetX : e.layerX;
    var my = e.offsetY != undefined ? e.offsetY : e.layerY; 
    mx -= this.dom_roi_prev.offsetWidth/2;
    my -= this.dom_roi_prev.offsetHeight/2;
    this.dom_roi_prev.style.left = mx + 'px';
    this.dom_roi_prev.style.top  = my + 'px';
}

ThumbnailControl.prototype.moveViewerNow = function (e) {
    if (this.move_timeout) clearTimeout (this.move_timeout);
    this.move_timeout = null;
    this.moveViewer(e);
}

ThumbnailControl.prototype.queueMove = function (e) {
  this.movePreview(e);
  if (this.move_timeout) clearTimeout (this.move_timeout);
  // IE8 releases references in the event object before the timer ticks
  // leading to "member not found" errors. Making a shallow copy of
  // members we are interested in.
  if (isIE()) {
      var x = {};
      x.offsetX = e.offsetX;
      x.offsetY = e.offsetY;
      this.move_timeout = setTimeout(callback(this, 'moveViewerNow', x), this.move_delay_ms);
  } else {
      this.move_timeout = setTimeout(callback(this, 'moveViewerNow', e), this.move_delay_ms);
  }
}

ThumbnailControl.prototype.blockPropagation = function (e) {
  if (e.stopPropagation) e.stopPropagation(); // DOM Level 2
  else e.cancelBubble = true;                 // IE    
  if (e.preventDefault) e.preventDefault(); // prevent image dragging
  else e.returnValue = false;        
}

ThumbnailControl.prototype.onmousedown = function (e) {
    if (!e) e = window.event;  // IE event model
    if (e == null) return false;
    this.blockPropagation(e);

    this.mouse_pressed = true;  
    if (this.dom_surface) this.dom_surface.style.cursor = 'move';
    //this.moveViewer(e);
    return false;
}

ThumbnailControl.prototype.onmouseup = function (e) {
    if (!e) e = window.event;  // IE event model  
    if (e == null) return false; 
    this.blockPropagation(e);

    // UE sends mouseup events even after mouseout
    if (!this.mouse_pressed) return false;

    this.mouse_pressed = false; 
    if (this.dom_surface) this.dom_surface.style.cursor = 'default';
    this.moveViewer(e);
    return false;    
}

ThumbnailControl.prototype.onmousemove = function (e) {
    if (!e) e = window.event;  // IE event model  
    if (e == null) return false;
    this.blockPropagation(e);        

    if (!this.mouse_pressed) return false;
    if(isIE()) {
        if ((e.offsetX + ',' + e.offsetY) == this.mousemoveoffset) {
            // IE keeps firing mousemove events after a mouse button is pressed
            // resulting in weird errors
            return false;
        }
        this.mousemoveoffset = e.offsetX + ',' + e.offsetY;
    }
    if (this.move_delay_ms<=0)
      this.moveViewer(e);
    else      
      this.queueMove(e);
    return false;      
}

ThumbnailControl.prototype.onmouseout = function (e) {
    if (!e) e = window.event;  // IE event model  
    if (e == null) return false; 
    
    if(isIE()) {
        // IE triggers mouseout events on both the image and the wrapping div,
        // as well as on the ROI indicators, so we must filter out the unwanted
        // ones.
        if (e.offsetX >= 0 && e.offsetX < this.dom_image.width &&
            e.offsetY >= 0 && e.offsetY < this.dom_image.height) {
            return false;
        }
    }
    this.blockPropagation(e);    
 
    this.mouse_pressed = false;   
    if (this.dom_surface) this.dom_surface.style.cursor = 'default';
    return false;    
}
