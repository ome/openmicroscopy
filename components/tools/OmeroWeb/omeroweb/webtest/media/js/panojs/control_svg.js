/*******************************************************************************
  SVG listner - listens to the viewer and translates SVG
  
  GSV 3.0 : PanoJS3
  @author Dmitry Fedorov  <fedorov@ece.ucsb.edu>   
  
  Copyright (c) 2010 Dmitry Fedorov, Center for Bio-Image Informatics

*******************************************************************************/

function SvgControl(viewer, element) {
  this.viewer = viewer;  
    
  if (typeof element == 'string')
    this.svg_element = document.getElementById(element);
  else
    this.svg_element = element;    

  this.viewer.addViewerMovedListener(this);
  this.viewer.addViewerZoomedListener(this);    
}

SvgControl.prototype.viewerMoved = function(e) {
    this.svg_element.style.left = e.x + 'px';
    this.svg_element.style.top  = e.y + 'px';            
}

SvgControl.prototype.viewerZoomed = function(e) {
    this.svg_element.style.left = e.x + 'px';
    this.svg_element.style.top  = e.y + 'px';  
  
    var current_size = this.viewer.currentImageSize();
    this.svg_element.style.width  = current_size.width + 'px';
    this.svg_element.style.height = current_size.height + 'px';           
  
    //var svgembed = document.getElementById( 'svgembed' ); 
    //svgembed.style.width = level.width + 'px';
    //svgembed.style.height  = level.height + 'px';       
}
