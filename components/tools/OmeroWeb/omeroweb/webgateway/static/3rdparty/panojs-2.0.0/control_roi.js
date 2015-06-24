PanoJS.CONTROL_ROI_STYLE = "position: absolute;";

function ROIControl(viewer) {
  this.viewer = viewer; 
  this.createDOMElements();

  this.scale = 1;
  this.x = 0;
  this.y = 0;  
  this.width = 0;
  this.height = 0; 
  
  this.viewer.addViewerMovedListener(this);
  this.viewer.addViewerZoomedListener(this);
  this.viewer.addViewerResizedListener(this);
  
  // load thumbnail image
  this.init();
}


ROIControl.prototype.init = function() {
  this.updateDOMElements();
  this.viewer.notifyViewerZoomed();
}

ROIControl.prototype.createDOMElements = function() {
    var de = this.viewer.surface;

    var canvas_id = this.viewer.viewer.id+'-roi';

    this.dom_element = document.createElement('div');
    this.dom_element.id = canvas_id;
    this.dom_element.className = 'weblitz-viewport-roi';
    this.dom_element.setAttribute("style", PanoJS.CONTROL_ROI_STYLE);
    de.appendChild(this.dom_element); 
    
}

ROIControl.prototype.updateDOMElements = function() {
  var cur_size = this.viewer.currentImageSize();
  this.width = cur_size.width;
  this.height = cur_size.height;

  this.dom_element.style.width = this.width + 'px';
  this.dom_element.style.height = this.height + 'px';
  this.dom_element.style.left = this.x + 'px';
  this.dom_element.style.top = this.y + 'px';
}

ROIControl.prototype.viewerMoved = function(e) {
    if (!this.dom_element || typeof this.dom_element == 'undefined') return;
    this.x = e.x;
    this.y = e.y;
    this.updateDOMElements();

    //notify the viewer
    var vp = $.WeblitzViewport($(this.viewer.viewer).parent().parent().parent());

    var theT = vp.getTPos();
    var theZ = vp.getZPos();
    
    if (vp.viewportimg.get(0).setRoiZoom) {
        vp.viewportimg.get(0).setRoiZoom(this.scale*100);
    }
    if (vp.viewportimg.get(0).refresh_rois) {
        if ((vp.viewportimg.get(0).theT != theT) || (vp.viewportimg.get(0).theZ != theZ)) {
            vp.viewportimg.get(0).refresh_rois(theZ, theT);
        }
    }
}

ROIControl.prototype.viewerZoomed = function(e) {
    this.scale  = e.scale;
    this.viewerMoved(e);
}

ROIControl.prototype.viewerResized = function(e) {
    this.viewerMoved(e);
}


