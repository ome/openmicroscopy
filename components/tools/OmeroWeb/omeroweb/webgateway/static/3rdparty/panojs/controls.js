/*******************************************************************************
  Controls - creates buttons for zooming and, full screen 
  
  GSV 3.0 : PanoJS3
  @author Dmitry Fedorov  <fedorov@ece.ucsb.edu>   
  
  Copyright (c) 2010 Dmitry Fedorov, Center for Bio-Image Informatics
  
  using: isClientTouch() and isClientPhone() from utils.js

*******************************************************************************/

PanoJS.CONTROL_IMAGE_ZOOMIN   = "32px_plus.png";
PanoJS.CONTROL_IMAGE_ZOOM11   = "32px_11.png";
PanoJS.CONTROL_IMAGE_ZOOMOUT  = "32px_minus.png";
PanoJS.CONTROL_IMAGE_MAXIMIZE = "32px_show.png";

PanoJS.CONTROL_IMAGE_ZOOMIN_TOUCH   = "64px_plus.png";
PanoJS.CONTROL_IMAGE_ZOOM11_TOUCH   = "64px_11.png";
PanoJS.CONTROL_IMAGE_ZOOMOUT_TOUCH  = "64px_minus.png";
PanoJS.CONTROL_IMAGE_MAXIMIZE_TOUCH = "64px_show.png";

PanoJS.CONTROL_STYLE = "position: absolute; z-index: 30; "; //opacity:0.5; filter:alpha(opacity=50); ";

PanoJS.CONTROL_ZOOMIN = {
    className : "zoomIn",
    image : (isClientTouch() ? PanoJS.CONTROL_IMAGE_ZOOMIN_TOUCH : PanoJS.CONTROL_IMAGE_ZOOMIN),
    title : "Zoom in",
    style : PanoJS.CONTROL_STYLE + " top: 10px; left: 10px; width: 20px;",
};

PanoJS.CONTROL_ZOOM11 = {
    className : "zoom11",
    image : (isClientTouch() ? PanoJS.CONTROL_IMAGE_ZOOM11_TOUCH : PanoJS.CONTROL_IMAGE_ZOOM11),
    title : "Zoom 1:1",
    style : PanoJS.CONTROL_STYLE + " top: 40px; left: 10px; width: 20px;",
};

PanoJS.CONTROL_ZOOMOUT = {
    className : "zoomOut",
    image : (isClientTouch() ? PanoJS.CONTROL_IMAGE_ZOOMOUT_TOUCH : PanoJS.CONTROL_IMAGE_ZOOMOUT),
    title : "Zoom out",
    style : PanoJS.CONTROL_STYLE + " top: 70px; left: 10px; width: 20px;",
};

PanoJS.CONTROL_MAXIMIZE = {
    className : "maximize",
    image : (isClientTouch() ? PanoJS.CONTROL_IMAGE_MAXIMIZE_TOUCH : PanoJS.CONTROL_IMAGE_MAXIMIZE),
    title : "Maximize",
    style : PanoJS.CONTROL_STYLE + " top: 10px; right: 10px; width: 20px;",
};

if (isClientTouch()) {
  PanoJS.CONTROL_ZOOMIN.style   = PanoJS.CONTROL_STYLE + " top: 15px;  left: 15px;  width: 36px;";
  PanoJS.CONTROL_ZOOM11.style   = PanoJS.CONTROL_STYLE + " top: 75px;  left: 15px;  width: 36px;";
  PanoJS.CONTROL_ZOOMOUT.style  = PanoJS.CONTROL_STYLE + " top: 135px; left: 15px;  width: 36px;";
  PanoJS.CONTROL_MAXIMIZE.style = PanoJS.CONTROL_STYLE + " top: 15px;  right: 15px; width: 36px;";
}

if (isClientPhone()) {
  PanoJS.CONTROL_ZOOMIN.style   = PanoJS.CONTROL_STYLE + " top: 30px;  left: 30px;  width: 96px;";
  PanoJS.CONTROL_ZOOM11.style   = PanoJS.CONTROL_STYLE + " top: 180px; left: 30px;  width: 96px;";
  PanoJS.CONTROL_ZOOMOUT.style  = PanoJS.CONTROL_STYLE + " top: 320px; left: 30px;  width: 96px;";
  PanoJS.CONTROL_MAXIMIZE.style = PanoJS.CONTROL_STYLE + " top: 30px;  right: 30px; width: 96px;";
}



function PanoControls(viewer) {
    this.viewer = viewer;  
    this.initControls();
    this.createDOMElements();
}

PanoControls.prototype.initControls = function() {
  if (PanoJS.CONTROL_UPDATED_URLS) return;
  PanoJS.CONTROL_ZOOMIN.image   = PanoJS.STATIC_BASE_URL+PanoJS.CONTROL_ZOOMIN.image;
  PanoJS.CONTROL_ZOOM11.image   = PanoJS.STATIC_BASE_URL+PanoJS.CONTROL_ZOOM11.image;
  PanoJS.CONTROL_ZOOMOUT.image  = PanoJS.STATIC_BASE_URL+PanoJS.CONTROL_ZOOMOUT.image;
  PanoJS.CONTROL_MAXIMIZE.image = PanoJS.STATIC_BASE_URL+PanoJS.CONTROL_MAXIMIZE.image;
  PanoJS.CONTROL_UPDATED_URLS   = true;
}

PanoControls.prototype.createDOMElements = function() {
    this.dom_element = this.viewer.viewerDomElement();
      
    if (PanoJS.CREATE_CONTROL_ZOOMIN) {
      this.createButton (PanoJS.CONTROL_ZOOMIN);
    }
    if (PanoJS.CREATE_CONTROL_ZOOM11) {
      this.createButton (PanoJS.CONTROL_ZOOM11);
    }
    if (PanoJS.CREATE_CONTROL_ZOOMOUT) {
      this.createButton (PanoJS.CONTROL_ZOOMOUT);
    }
    if (PanoJS.CREATE_CONTROL_MAXIMIZE) {
      this.createButton (PanoJS.CONTROL_MAXIMIZE);
    }
}

PanoControls.prototype.createButton = function(control) {
      
    var className = control.className;
    var src = control.image;
    var title = control.title;
    var style = control.style;
    
    var btn = document.createElement('span');
    btn.className = className;
    this.dom_element.appendChild(btn); 

    if (style) {
      btn.setAttribute("style", style);
      btn.style.cssText = style;   
    }
    
    var img = document.createElement('img');
    img.src = src;
    if (title) img.title = title;
    if (btn.style.width) img.style.width = btn.style.width;
    btn.appendChild(img);    
    
    btn.onclick = callback(this.viewer, this.viewer[btn.className + 'Handler']); 
                
    return btn;
}

