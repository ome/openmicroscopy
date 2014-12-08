PanoJS.CONTROL_SCALEBAR_STYLE = "position: absolute;";

function ScaleBarControl(viewer) {
  this.viewer = viewer; 
  this.createDOMElements();

  this.scale = 1;
  this.x = 0;
  this.y = 0;  
  this.width = 0;
  this.height = 0; 
  
  this.viewer.addViewerZoomedListener(this);
  
  // load scalebar
  this.init();
}


ScaleBarControl.prototype.init = function() {
  this.viewer.notifyViewerZoomed();
}

ScaleBarControl.prototype.createDOMElements = function() {
  var de = this.viewer.surface;

  var scalebar_id = this.viewer.viewer.id+'-scalebar';

  this.dom_element = document.createElement('div');
  this.dom_element.id = scalebar_id;
  this.dom_element.className = 'weblitz-viewport-scalebar';
  this.dom_element.setAttribute("style", PanoJS.CONTROL_SCALEBAR_STYLE);
  de.appendChild(this.dom_element); 
}



ScaleBarControl.prototype.viewerZoomed = function(e) {
  if (!this.dom_element || typeof this.dom_element == 'undefined') return;
  
  this.scale  = e.scale;
  var vp = $.WeblitzViewport($(this.viewer.viewer).parent().parent().parent());

  if (vp.viewportimg.get(0).setScalebarZoom) {
    vp.viewportimg.get(0).setScalebarZoom(this.scale);
  }
}


