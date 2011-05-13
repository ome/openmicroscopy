/*******************************************************************************
  InfoControl - creates text about an image at the bottom
  
  GSV 3.0 : PanoJS3
  @author Dmitry Fedorov  <fedorov@ece.ucsb.edu>   
  
  Copyright (c) 2010 Dmitry Fedorov, Center for Bio-Image Informatics
  
  using: isClientTouch() and isClientPhone() from utils.js

*******************************************************************************/

function InfoControl(viewer) {
    this.viewer = viewer;  
    this.createDOMElements();

    this.viewer.addViewerZoomedListener(this);    
}

PanoJS.INFO_CONTROL_STYLE = "padding: 5px; text-shadow: 1px 1px 1px #000000; font-size: 12px;";

if (isClientTouch())
  PanoJS.INFO_CONTROL_STYLE = "padding: 10px; text-shadow: 2px 2px 2px #000000; font-size: 18px;";

if (isClientPhone())
  PanoJS.INFO_CONTROL_STYLE   = "padding: 10px; text-shadow: 6px 6px 6px #000000; font-size: 40px;";



InfoControl.prototype.createDOMElements = function() {
    this.dom_element = this.viewer.viewerDomElement();
      
    this.dom_info = document.createElement('span');
    this.dom_info.className = 'info';
    this.dom_element.appendChild(this.dom_info);       

    this.dom_info.setAttribute("style", PanoJS.INFO_CONTROL_STYLE );
    this.dom_info.style.cssText = PanoJS.INFO_CONTROL_STYLE;   
    
    //this.dom_info.innerHTML = "";
}

InfoControl.prototype.viewerZoomed = function(e) {
    var sz = this.viewer.imageSize();
    if (this.dom_info) 
        this.dom_info.innerHTML = 'Scale: '+ e.scale*100 +'%';
}
