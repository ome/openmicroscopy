/*******************************************************************************
 Panoramic JavaScript Image Viewer (PanoJS) 2.0.0
 aka GSV 3.0 aka Giant-Ass Image Viewer 3

 Generates a draggable and zoomable viewer for images that would
 be otherwise too large for a browser window.  Examples would include
 maps or high resolution document scans.

 History:
   GSV 1.0 : Giant-Ass Image Viewer : http://mike.teczno.com/giant/pan/
   @author Michal Migurski <mike-gsv@teczno.com>

   GSV 2.0 : PanoJS : http://code.google.com/p/panojs/
   @author Dan Allen       <dan.allen@mojavelinux.com>
     
   GSV 3.0 : PanoJS3
   @author Dmitry Fedorov  <fedorov@ece.ucsb.edu> 

 Images must be precut into tiles: 
   a) tilemaker.py python library shipped with GSV 2.0
   b) Zoomify
   c) imagcnv 
   d) Bisque system
   e) dynamically served by websystems (requires writing TileProvider)

 
  var viewerBean = new PanoJS(element, 'tiles', 256, 3, 1);

 Copyright (c) 2005 Michal Migurski <mike-gsv@teczno.com>
                    Dan Allen <dan.allen@mojavelinux.com>
               2010 Dmitry Fedorov, Center for Bio-Image Informatics <fedorov@ece.ucsb.edu>
  
 Redistribution and use in source form, with or without modification,
 are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.
  
 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*******************************************************************************/
 
 
function PanoJS(viewer, options) {
    
  // listeners that are notified on a move (pan) event
  this.viewerMovedListeners = [];
  // listeners that are notified on a zoom event
  this.viewerZoomedListeners = [];
  // listeners that are notified on a resize event
  this.viewerResizedListeners = [];
    
    
  if (typeof viewer == 'string')
    this.viewer = document.getElementById(viewer);
  else
    this.viewer = viewer;
    
  if (typeof options == 'undefined') options = {};
    
  if (typeof options.tileUrlProvider != 'undefined' && (options.tileUrlProvider instanceof PanoJS.TileUrlProvider) )
    this.tileUrlProvider = options.tileUrlProvider;
  else
    this.tileUrlProvider = new PanoJS.TileUrlProvider( options.tileBaseUri ? options.tileBaseUri : PanoJS.TILE_BASE_URI,
                                                       options.tilePrefix ? options.tilePrefix : PanoJS.TILE_PREFIX,
                                                       options.tileExtension ? options.tileExtension : PanoJS.TILE_EXTENSION
                                                     );

  this.xTileSize = (options.xTileSize ? options.xTileSize : PanoJS.TILE_SIZE);
  this.yTileSize = (options.yTileSize ? options.yTileSize : PanoJS.TILE_SIZE);
  this.realXTileSize = this.xTileSize;
  this.realYTileSize = this.yTileSize;
  
  
  if (options.staticBaseURL) PanoJS.STATIC_BASE_URL = options.staticBaseURL;  
      
  // assign and do some validation on the zoom levels to ensure sanity
  this.zoomLevel = (typeof options.initialZoom == 'undefined' ? -1 : parseInt(options.initialZoom));
  this.maxZoomLevel = (typeof options.maxZoom == 'undefined' ? 0 : Math.abs(parseInt(options.maxZoom)));
  if (this.zoomLevel > this.maxZoomLevel) this.zoomLevel = this.maxZoomLevel;
    
  this.initialPan = (options.initialPan ? options.initialPan : PanoJS.INITIAL_PAN);
  // map Zoom Levels to Scales (only needed if each zoom level is not a factor of 2)
  this.zoomLevelScaling = options.zoomLevelScaling;   // 'undefined' is handled in this.currentScale()
    
  this.initialized = false;
  this.surface = null;
  this.well = null;
  this.width = 0;
  this.height = 0;
  this.top = 0;
  this.left = 0;
  this.x = 0;
  this.y = 0;
  this.mark = { 'x' : 0, 'y' : 0 };
  this.pressed = false;
  this.tiles = [];
  
  this.cache = {};
  this.blankTile = options.blankTile ? options.blankTile : PanoJS.BLANK_TILE_IMAGE;
  this.loadingTile = options.loadingTile ? options.loadingTile : PanoJS.LOADING_TILE_IMAGE;      
  this.resetCache();
  this.image_size = { width: options.imageWidth, height: options.imageHeight };
  
  this.delay_ms = options.delay ? options.delay : PanoJS.DELAY_MS; 
  
  // employed to throttle the number of redraws that
  // happen while the mouse is moving
  this.moveCount = 0;
  this.slideMonitor = 0;
  this.slideAcceleration = 0;
}

// project specific variables
PanoJS.PROJECT_NAME = 'PanoJS';
PanoJS.PROJECT_VERSION = '2.0.0';
PanoJS.REVISION_FLAG = '';

// CSS definition settings
PanoJS.SURFACE_STYLE_CLASS  = 'surface';
PanoJS.SURFACE_ID           = 'viewer_contorls_surface';
PanoJS.SURFACE_STYLE_ZINDEX = 20;
PanoJS.WELL_STYLE_CLASS     = 'well';
PanoJS.CONTROLS_STYLE_CLASS = 'controls'
PanoJS.TILE_STYLE_CLASS     = 'tile';

// language settings
PanoJS.MSG_BEYOND_MIN_ZOOM = 'Cannot zoom out past the current level.';
PanoJS.MSG_BEYOND_MAX_ZOOM = 'Cannot zoom in beyond the current level.';

// defaults if not provided as constructor options
PanoJS.TILE_BASE_URI = 'tiles';
PanoJS.TILE_PREFIX = 'tile-';
PanoJS.TILE_EXTENSION = 'jpg';
PanoJS.TILE_SIZE = 256;
PanoJS.BLANK_TILE_IMAGE = 'blank.gif';
PanoJS.LOADING_TILE_IMAGE = 'blank.gif';
PanoJS.INITIAL_PAN = { 'x' : .5, 'y' : .5 };
PanoJS.USE_LOADER_IMAGE = true;
PanoJS.USE_SLIDE = true;

// Delay before positioning tiles in the viewer, see positionTiles 
// or moving the viewer, see: ThumbnailControl
PanoJS.DELAY_MS = 500;

// dima
if (!PanoJS.STATIC_BASE_URL) PanoJS.STATIC_BASE_URL = '';
PanoJS.CREATE_CONTROL_ZOOMIN = true;
PanoJS.CREATE_CONTROL_ZOOM11 = true;
PanoJS.CREATE_CONTROL_ZOOMOUT = true;
PanoJS.CREATE_CONTROL_MAXIMIZE = true;
PanoJS.CREATE_INFO_CONTROLS = true;
PanoJS.CREATE_OSD_CONTROLS = true;
PanoJS.CREATE_THUMBNAIL_CONTROLS = (isClientPhone() ? false : true);

PanoJS.MAX_OVER_ZOOM = 2;
PanoJS.PRE_CACHE_AMOUNT = 3; // 1 - only visible, 2 - more, 3 - even more

// dima
// The dafault is to pan with wheel events on a mac and zoom on other systems
PanoJS.USE_WHEEL_FOR_ZOOM = (navigator.userAgent.indexOf("Mac OS X")>0 ? false: true);
// the deltas on Firefox and Chrome are 40 times smaller than on Safari or IE
PanoJS.WHEEL_SCALE = (navigator.userAgent.toLowerCase().indexOf('chrome')>-1 ? 1 : 40);

// dima: keys used by keyboard handlers
// right now event is attached to 'document', can't make sure which element is current, skip for now
PanoJS.USE_KEYBOARD = false;
PanoJS.KEY_MOVE_THROTTLE = 15;
PanoJS.KEY_UP    = 38;
PanoJS.KEY_DOWN  = 40;
PanoJS.KEY_RIGHT = 39;
PanoJS.KEY_LEFT  = 37;
PanoJS.KEY_MINUS = {109:0, 189:0};
PanoJS.KEY_PLUS  = {107:0, 187:0};

// performance tuning variables
PanoJS.MOVE_THROTTLE = 3;
PanoJS.SLIDE_DELAY = 40;
PanoJS.SLIDE_ACCELERATION_FACTOR = 5;

// the following are calculated settings
PanoJS.DOM_ONLOAD = (navigator.userAgent.indexOf('KHTML') >= 0 ? false : true);
PanoJS.GRAB_MOUSE_CURSOR = (navigator.userAgent.search(/KHTML|Opera/i) >= 0 ? 'pointer' : (document.attachEvent ? 'url(grab.cur)' : '-moz-grab'));
PanoJS.GRABBING_MOUSE_CURSOR = (navigator.userAgent.search(/KHTML|Opera/i) >= 0 ? 'move' : (document.attachEvent ? 'url(grabbing.cur)' : '-moz-grabbing'));



PanoJS.prototype.init = function() {

    if (document.attachEvent)
      document.body.ondragstart = function() { return false; }
 
    if (this.width == 0 && this.height == 0) {
      this.width = this.viewer.offsetWidth;
      this.height = this.viewer.offsetHeight;
    }
   
    // calculate the zoom level based on what fits best in window
    if (this.zoomLevel < 0 || this.zoomLevel > this.maxZoomLevel) {
            var new_level = 0;
            // here MAX defines partial fit and MIN would use full fit
            while ((Math.max(this.xTileSize, this.yTileSize)) * Math.pow(2, new_level) <= Math.max(this.width, this.height) && 
                   new_level<=this.maxZoomLevel) {
                this.zoomLevel = new_level;
                new_level += 1;   
            }
    }
      
    // move top level up and to the left so that the image is centered
    var fullWidth = this.xTileSize * Math.pow(2, this.zoomLevel);
    var fullHeight = this.yTileSize * Math.pow(2, this.zoomLevel);
    if (this.image_size) {
      var cur_size = this.currentImageSize();  
      fullWidth = cur_size.width;
      fullHeight = cur_size.height;    
    }
    this.x = Math.floor((fullWidth - this.width) * -this.initialPan.x);
    this.y = Math.floor((fullHeight - this.height) * -this.initialPan.y);

       
    // offset of viewer in the window
    for (var node = this.viewer; node; node = node.offsetParent) {
      this.top += node.offsetTop;
      this.left += node.offsetLeft;
    }
        
    // Create viewer elements
    if (!this.surface) {
      this.surface = document.createElement('div');
      this.surface.className = PanoJS.SURFACE_STYLE_CLASS;
      this.surface.id = PanoJS.SURFACE_ID;
      this.viewer.appendChild(this.surface); 
      this.surface.style.cursor = PanoJS.GRAB_MOUSE_CURSOR;
      this.surface.style.zIndex = PanoJS.SURFACE_STYLE_ZINDEX;
    }
     
    if (!this.well) {
      this.well = document.createElement('div');
      this.well.className = PanoJS.WELL_STYLE_CLASS;
      this.viewer.appendChild(this.well);
    }


    // set event handlers for controls buttons
    if ((PanoJS.CREATE_CONTROL_ZOOMIN
         || PanoJS.CREATE_CONTROL_ZOOM11
         || PanoJS.CREATE_CONTROL_ZOOMOUT
         || PanoJS.CREATE_CONTROL_MAXIMIZE) && !this.controls)
      this.controls = new PanoControls(this);
         
    if (PanoJS.CREATE_INFO_CONTROLS && !this.info_control) {
      this.info_control = new InfoControl(this);
    }          

    if (PanoJS.CREATE_OSD_CONTROLS && !this.osd_control) {
      this.osd_control = new OsdControl(this);
    }     
  
    if (PanoJS.CREATE_THUMBNAIL_CONTROLS && !this.thumbnail_control) {
      this.thumbnail_control = new ThumbnailControl(this);
    }     
        
    this.prepareTiles();
    this.initialized = true;

    // dima: Setup UI events
    this.ui_listener = this.surface;
    if (isIE()) this.ui_listener = this.viewer; // issues with IE, hack it
    
    this.ui_listener.onmousedown   = callback(this, this.mousePressedHandler);
    this.ui_listener.onmouseup     = callback(this, this.mouseReleasedHandler);
    this.ui_listener.onmouseout    = callback(this, this.mouseReleasedHandler);
    this.ui_listener.oncontextmenu = function() {return false;}; 
    this.ui_listener.ondblclick    = callback(this, this.doubleClickHandler);
    if (PanoJS.USE_KEYBOARD)
      document.onkeydown  = callback(this, this.keyboardHandler);

    this.ui_listener.onmousewheel = callback(this, this.mouseWheelHandler);
    // dima: Firefox standard
    if (!('onmousewheel' in document.documentElement))
      this.surface.addEventListener ("DOMMouseScroll", callback(this, this.mouseScrollHandler), false);
        
    // dima: support for HTML5 touch interfaces like iphone and android
    this.ui_listener.ontouchstart    = callback(this, this.touchStartHandler);
    this.ui_listener.ontouchmove     = callback(this, this.touchMoveHandler);
    this.ui_listener.ongesturestart  = callback(this, this.gestureStartHandler);
    this.ui_listener.ongesturechange = callback(this, this.gestureChangeHandler);
    this.ui_listener.ongestureend    = callback(this, this.gestureEndHandler);        
    
    // notify listners
    this.notifyViewerZoomed();    
    this.notifyViewerMoved();  
};

PanoJS.prototype.viewerDomElement = function() {    
    return this.viewer;
};

PanoJS.prototype.thumbnailURL = function() {       
    return this.tileUrlProvider.assembleUrl(0, 0, 0);
};

PanoJS.prototype.imageSize = function() {        
    return this.image_size;
};     

PanoJS.prototype.currentImageSize = function() {    
    var scale = this.currentScale();
    return { width: this.image_size.width * scale, height: this.image_size.height * scale };       
};    
    
PanoJS.prototype.prepareTiles = function() {        
    var rows = Math.ceil(this.height / this.yTileSize)+ PanoJS.PRE_CACHE_AMOUNT;
    var cols = Math.ceil(this.width / this.xTileSize)+ PanoJS.PRE_CACHE_AMOUNT;
           
    for (var c = 0; c < cols; c++) {
      var tileCol = [];
            
      for (var r = 0; r < rows; r++) {
        /**
         * element is the DOM element associated with this tile
         * posx/posy are the pixel offsets of the tile
         * xIndex/yIndex are the index numbers of the tile segment
         * qx/qy represents the quadrant location of the tile
         */
        /*
        var tile = {
          'element' : null,
          'posx' : 0,
          'posy' : 0,
          'xIndex' : c,
          'yIndex' : r,
          'qx' : c,
          'qy' : r
        };*/
        
        var tile = new Tile(this, c, r);
        
        tileCol.push(tile);
      }
      this.tiles.push(tileCol);
    }
        
    this.positionTiles();
};
    
/**
 * Position the tiles based on the x, y coordinates of the
 * viewer, taking into account the motion offsets, which
 * are calculated by a motion event handler.
 */
PanoJS.prototype.positionTiles = function(motion, reset) {       
    // default to no motion, just setup tiles
    if (typeof motion == 'undefined') {
      motion = { 'x' : 0, 'y' : 0 };
    }

    var cur_size = this.currentImageSize();
       
    for (var c = 0; c < this.tiles.length; c++) {
      for (var r = 0; r < this.tiles[c].length; r++) {
        var tile = this.tiles[c][r];
                
        tile.posx = (tile.xIndex * this.xTileSize) + this.x + motion.x;
        tile.posy = (tile.yIndex * this.yTileSize) + this.y + motion.y;
                
        var visible = true;
                
        if (tile.posx > this.width  +this.xTileSize ) {
          // tile moved out of view to the right
          // consider the tile coming into view from the left
          do {
            tile.xIndex -= this.tiles.length;
            tile.posx = (tile.xIndex * this.xTileSize) + this.x + motion.x;
          } while (tile.posx > this.width +this.xTileSize  );
                    
          if (tile.posx + this.xTileSize < 0) {
            visible = false;
          }
                    
        } else {
          // tile may have moved out of view from the left
          // if so, consider the tile coming into view from the right
          while (tile.posx < -this.xTileSize  *2) {
            tile.xIndex += this.tiles.length;
            tile.posx = (tile.xIndex * this.xTileSize) + this.x + motion.x;
          }
                    
          if (tile.posx > this.width  +this.xTileSize) {
            visible = false;
          }
        }
                
        if (tile.posy > this.height   +this.yTileSize) {
          // tile moved out of view to the bottom
          // consider the tile coming into view from the top
          do {
            tile.yIndex -= this.tiles[c].length;
            tile.posy = (tile.yIndex * this.yTileSize) + this.y + motion.y;
          } while (tile.posy > this.height   +this.yTileSize);
                    
          if (tile.posy + this.yTileSize < 0) {
            visible = false;
          }
                    
        } else {
          // tile may have moved out of view to the top
          // if so, consider the tile coming into view from the bottom
          while (tile.posy < -this.yTileSize  *2) {
            tile.yIndex += this.tiles[c].length;
            tile.posy = (tile.yIndex * this.yTileSize) + this.y + motion.y;
          }
                    
          if (tile.posy > this.height   +this.yTileSize) {
            visible = false;
          }
        }
                
        // additional constraint                
        if (tile.xIndex*this.xTileSize >= cur_size.width) visible = false;
        if (tile.yIndex*this.yTileSize >= cur_size.height) visible = false;                    
                
        // display the image if visible
        if (visible)
            this.assignTileImage(tile);
        else
            this.removeTileFromWell(tile);
      }
    }

    // reset the x, y coordinates of the viewer according to motion
    if (reset) {
      this.x += motion.x;
      this.y += motion.y;
    }
};
    
PanoJS.prototype.removeTileFromWell = function(tile) {        
    if (!tile || !tile.element || !tile.element.parentNode) return;
    this.well.removeChild(tile.element);   
    tile.element = null;      
};
    
   
/**
 * Determine the source image of the specified tile based
 * on the zoom level and position of the tile.  If forceBlankImage
 * is specified, the source should be automatically set to the
 * null tile image.  This method will also setup an onload
 * routine, delaying the appearance of the tile until it is fully
 * loaded, if configured to do so.
 */
PanoJS.prototype.assignTileImage = function(tile) {    
    var tileImgId, src;
    var useBlankImage = false;
        
    // check if image has been scrolled too far in any particular direction
    // and if so, use the null tile image
    if (!useBlankImage) {
      var left = tile.xIndex < 0;
      var high = tile.yIndex < 0;
      
      // dima: allow zooming in more than 100%
      var cur_size = this.currentImageSize();      
      var right = tile.xIndex*this.xTileSize >= cur_size.width;
      var low   = tile.yIndex*this.yTileSize >= cur_size.height;              
            
      if (high || left || low || right) {
        useBlankImage = true;
      }
    }

    if (useBlankImage) {
      tileImgId = 'blank';
      src = this.cache['blank'].src;
    }
    else {
      tileImgId = src = this.tileUrlProvider.assembleUrl(tile.xIndex, tile.yIndex, this.zoomLevel);
    }

    // only remove tile if identity is changing
    if (tile.element != null &&
      tile.element.parentNode != null &&
      tile.element.relativeSrc != null &&      
      tile.element.relativeSrc != src) {
      delete this.cache[tile.element.relativeSrc];
      this.well.removeChild(tile.element);
    }

    var scale = Math.max(this.xTileSize / this.realXTileSize, 1.0);
    var tileImg = this.cache[tileImgId];

    //window.localStorage (details)
    //var available = navigator.mozIsLocallyAvailable("my-image-file.png", true);

    // create cache if not exist
    if (tileImg == null)
      //tileImg = this.cache[tileImgId] = this.createPrototype('', src); // delayed loading
      tileImg = this.cache[tileImgId] = this.createPrototype(src);
    else
      tileImg.done = true;

    //if (tileImg.done)  
    if (tileImg.naturalWidth && tileImg.naturalHeight && tileImg.naturalWidth>0 && tileImg.naturalHeight>0) {
      tileImg.style.width = tileImg.naturalWidth*scale + 'px';
      tileImg.style.height = tileImg.naturalHeight*scale + 'px';   
    } else 
    if (isIE() && tileImg.offsetWidth>0 && tileImg.offsetHeight>0) { // damn IE does not have naturalWidth ...
      tileImg.style.width = tileImg.offsetWidth*scale + 'px';
      tileImg.style.height = tileImg.offsetHeight*scale + 'px';         
    }

    // error handling for tile image loading - simply try to reload image after timeout
    tileImg.onerror = function() {
        var $this = $(this);
        // only try to reload if this is the first failure
        if (!$this.hasClass('failed')) {
          $this.addClass('failed');
          setTimeout(function(){
            var s = tileImg.src;
            tileImg.src = s;    // no change, but is enough to trigger reload
          }, 1000); // try to reload src after timeout - 1 sec seems to work OK
        }
      };

    if ( tileImg.done || !tileImg.delayed_loading &&
         (useBlankImage || !PanoJS.USE_LOADER_IMAGE || tileImg.complete || (tileImg.image && tileImg.image.complete))  ) {
      tileImg.onload = null;
      // tileImg.onerror = null;  // seems we can't remove error handler here
      $(tileImg).removeClass('failed');
      if (tileImg.image) tileImg.image.onload = null;
            
      if (tileImg.parentNode == null) {
        tile.element = this.well.appendChild(tileImg);
      }  
      tileImg.done = true;      
    } else {
      var loadingImg = this.createPrototype(this.cache['loading'].src);
      loadingImg.targetSrc = tileImgId;
            
      var well = this.well;
      tile.element = well.appendChild(loadingImg);
      tileImg.onload = function() {
        // make sure our destination is still present
        if (loadingImg.parentNode && loadingImg.targetSrc == tileImgId) {
          tileImg.style.top = loadingImg.style.top;
          tileImg.style.left = loadingImg.style.left;
          if (tileImg.naturalWidth && tileImg.naturalHeight && tileImg.naturalWidth>0 && tileImg.naturalHeight>0) {
            tileImg.style.width = tileImg.naturalWidth*scale + 'px';
            tileImg.style.height = tileImg.naturalHeight*scale + 'px'; 
          } else 
          if (isIE() && tileImg.offsetWidth>0 && tileImg.offsetHeight>0) { // damn IE does not have naturalWidth ...
            tileImg.style.width = tileImg.offsetWidth*scale + 'px';
            tileImg.style.height = tileImg.offsetHeight*scale + 'px';         
          }          
          well.replaceChild(tileImg, loadingImg);
          tile.element = tileImg;
        } else {
          // delete a tile if the destination is not present anymore
          if (loadingImg.parentNode) {
            well.removeChild(loadingImg);   
            tile.element = null;      
          }           
        }

        // since we've loaded OK, I assume this frees up memory (not confirmed)
        tileImg.onerror = null;
                
        tileImg.onload = function() {};
        return false;
      }

      // dima, fetch image after onload method is set-up
      if (!tileImg.done) {// && tileImg.delayed_loading) {
        tileImg.src = tileImg.relativeSrc;
      }
            
      // konqueror only recognizes the onload event on an Image
      // javascript object, so we must handle that case here
      if (!PanoJS.DOM_ONLOAD) {
        tileImg.image = new Image();
        tileImg.image.onload = tileImg.onload;
        tileImg.image.src = tileImg.src;
      }
    }
    
    if (tile.element) {
      tile.element.style.top = tile.posy + 'px';
      tile.element.style.left = tile.posx + 'px';    
    }
    
};

PanoJS.prototype.createPrototype = function(src, src_to_load) {        
    var img = document.createElement('img');
    img.src = src;
    if (!src_to_load)
      img.relativeSrc = src;
    else {
      img.relativeSrc = src_to_load;
      img.delayed_loading = true;
    }
    img.className = PanoJS.TILE_STYLE_CLASS;
    //img.style.width = this.xTileSize + 'px';
    //img.style.height = this.yTileSize + 'px';
    return img;
};
    
PanoJS.prototype.currentScale = function() {      
    var scale = 1.0;
    if (this.zoomLevel<this.maxZoomLevel) {
      var zoomDiff = Math.abs(this.zoomLevel-this.maxZoomLevel);
      if (this.zoomLevelScaling && typeof this.zoomLevelScaling[zoomDiff] != "undefined") {
        scale = this.zoomLevelScaling[zoomDiff];
      } else {
        scale = 1.0 / Math.pow(2, Math.abs(this.zoomLevel-this.maxZoomLevel));
      }
    }
    else
    if (this.zoomLevel>this.maxZoomLevel)
      scale = Math.pow(2, Math.abs(this.zoomLevel-this.maxZoomLevel));
    return scale;
};
  
PanoJS.prototype.toImageFromViewer = function(p) {   
    var scale = this.currentScale();
    p.x = (p.x / scale);
    p.y = (p.y / scale);    
    return p;
};  
    
PanoJS.prototype.toViewerFromImage = function(p) {       
    var scale = this.currentScale();
    p.x = (p.x * scale);
    p.y = (p.y * scale);    
    return p;
};  

PanoJS.prototype.addViewerMovedListener = function(listener) {          
    this.viewerMovedListeners.push(listener);
};
    
PanoJS.prototype.addViewerZoomedListener = function(listener) {  
    this.viewerZoomedListeners.push(listener);
};

PanoJS.prototype.addViewerResizedListener = function(listener) {      
    this.viewerResizedListeners.push(listener);
};  
    
// Notify listeners of a zoom event on the viewer.
PanoJS.prototype.notifyViewerZoomed = function() {         
    var scale = this.currentScale();
    var w = this.surface.clientWidth / scale;
    var h = this.surface.clientHeight / scale;  
    
    for (var i = 0; i < this.viewerZoomedListeners.length; i++)
      this.viewerZoomedListeners[i].viewerZoomed( new PanoJS.ZoomEvent(this.x, this.y, this.zoomLevel, scale, w, h) );
};
  
// dima : Notify listeners of a zoom event on the viewer
PanoJS.prototype.notifyViewerResized = function() {      
    var scale = this.currentScale();
    var w = this.surface.clientWidth / scale;
    var h = this.surface.clientHeight / scale;  
    for (var i = 0; i < this.viewerResizedListeners.length; i++)
      this.viewerResizedListeners[i].viewerResized( new PanoJS.ResizeEvent(this.x, this.y, w, h) );
};
    
// Notify listeners of a move event on the viewer.
PanoJS.prototype.notifyViewerMoved = function(coords) {         
    if (typeof coords == 'undefined') {
      coords = { 'x' : 0, 'y' : 0 };
    }
        
    for (var i = 0; i < this.viewerMovedListeners.length; i++) {
      this.viewerMovedListeners[i].viewerMoved( new PanoJS.MoveEvent( this.x + (coords.x - this.mark.x),
                                                                      this.y + (coords.y - this.mark.y)
                                                                    )
                                              );
    }
};

PanoJS.prototype.queuePositionTiles = function (motion, reset) {
  if (this.positionTiles_timeout) clearTimeout (this.positionTiles_timeout);
  this.positionTiles_timeout = setTimeout(callback(this, 'positionTilesNow', motion, reset), this.delay_ms);
}

PanoJS.prototype.positionTilesNow = function (motion, reset) {
  if (this.positionTiles_timeout) clearTimeout (this.positionTiles_timeout);
  this.positionTiles_timeout = null;
  this.positionTiles(motion, reset);
}


PanoJS.prototype.zoom = function(direction) {       
    // ensure we are not zooming out of range
    if (this.zoomLevel + direction < 0) {
      if (PanoJS.MSG_BEYOND_MIN_ZOOM) {
        alert(PanoJS.MSG_BEYOND_MIN_ZOOM);
      }
      return;
    }
    if (this.zoomLevel+direction > this.maxZoomLevel+PanoJS.MAX_OVER_ZOOM) return;
    
    this.blank();
    this.resetCache();       
        
    if (this.zoomLevel+direction > this.maxZoomLevel) {
        //dima
        var scale_dif = (this.zoomLevel+direction - this.maxZoomLevel) * 2;
        this.xTileSize = this.realXTileSize*scale_dif;
        this.yTileSize = this.realYTileSize*scale_dif;
    } else {
        this.xTileSize = this.realXTileSize;
        this.yTileSize = this.realYTileSize;
    }
    var coords = { 'x' : Math.floor(this.width / 2), 'y' : Math.floor(this.height / 2) };
        
    var before = {
      'x' : (coords.x - this.x),
      'y' : (coords.y - this.y)
    };
        
    var scaleDiff = Math.pow(2, direction);

    // if we're zooming less than 100%, check for non-default scaling as specified by this.zoomLevelScaling
    if (this.zoomLevel<this.maxZoomLevel || (this.zoomLevel+direction)<this.maxZoomLevel) {
      var oldZoom = Math.abs(this.zoomLevel-this.maxZoomLevel),
        newZoom = Math.abs((this.zoomLevel + direction) -this.maxZoomLevel);
      if (this.zoomLevelScaling && (typeof this.zoomLevelScaling[oldZoom] != "undefined") && this.zoomLevelScaling[newZoom]) {
        scaleDiff = this.zoomLevelScaling[newZoom] / this.zoomLevelScaling[oldZoom]
      }
    }

    var after = {
      'x' : Math.floor(before.x * scaleDiff),
      'y' : Math.floor(before.y * scaleDiff)
    };
        
    this.x = coords.x - after.x;
    this.y = coords.y - after.y;
    this.zoomLevel += direction;
        
    if (this.delay_ms<=0)
      this.positionTiles();
    else
      this.queuePositionTiles();
    this.notifyViewerZoomed();
};

PanoJS.prototype.update = function() {        
    this.blank();
    this.resetCache();
    this.positionTiles();
    if (this.thumbnail_control) this.thumbnail_control.update();
};    
    
// Clear all the tiles from the well for a complete reinitialization of the
// viewer. At this point the viewer is not considered to be initialized.
PanoJS.prototype.clear = function() {         
    this.blank();
    this.initialized = false;
    this.tiles = [];
    this.resetCache();
};
    
PanoJS.prototype.resetCache = function() {        
    this.cache = {};
    this.cache['blank'] = new Image();
    this.cache['blank'].src = this.blankTile;
    if (this.blankTile != this.loadingTile) {
      this.cache['loading'] = new Image();
      this.cache['loading'].src = this.loadingTile;
    } else {
      this.cache['loading'] = this.cache['blank'];
    }    
};    
    
// Remove all tiles from the well, which effectively "hides"
// them for a repaint.
PanoJS.prototype.blank = function() {      
    for (imgId in this.cache) {
      var img = this.cache[imgId];
      if (!img) continue;
      img.onload = function() {};
      if (img.image) {
        img.image.onload = function() {};
      }
            
      if (img.parentNode != null) {
        this.well.removeChild(img);
      }
    }
    this.resetCache();
};
    
// Method specifically for handling a mouse move event.  A direct
// movement of the viewer can be achieved by calling positionTiles() directly.
PanoJS.prototype.moveViewer = function(coords) {
  if (coords.x == this.x && coords.y == this.y) return;
  this.positionTiles({ 'x' : (coords.x - this.mark.x), 'y' : (coords.y - this.mark.y) });
  this.notifyViewerMoved(coords);
};
    
// dima: Event that works for any input, expects DeltaX and DeltaY
PanoJS.prototype.moveViewerBy = function(coords) {     
      this.positionTiles(coords, true);
      //this.notifyViewerMoved(coords);
      this.notifyViewerMoved();
},
  
  
/**
 * Make the specified coords the new center of the image placement.
 * This method is typically triggered as the result of a double-click
 * event.  The calculation considers the distance between the center
 * of the viewable area and the specified (viewer-relative) coordinates.
 * If absolute is specified, treat the point as relative to the entire
 * image, rather than only the viewable portion.
 */
PanoJS.prototype.recenter = function(coords, absolute, skip_motion) {   
  skip_motion = typeof(skip_motion) != 'undefined' ? skip_motion : false; 
  if (absolute) {
    coords.x += this.x;
    coords.y += this.y;
  }
  if (coords.x == this.x && coords.y == this.y) return;      
      
  var motion = {
    'x' : Math.floor((this.width / 2) - coords.x),
    'y' : Math.floor((this.height / 2) - coords.y)
  };
      
  if (motion.x == 0 && motion.y == 0) {
    return;
  }
      
  if (PanoJS.USE_SLIDE && !skip_motion) {
    var target = motion;
    var x, y;
    // handle special case of vertical movement
    if (target.x == 0) {
      x = 0;
      y = this.slideAcceleration;
    }
    else {
      var slope = Math.abs(target.y / target.x);
      x = Math.round(Math.pow(Math.pow(this.slideAcceleration, 2) / (1 + Math.pow(slope, 2)), .5));
      y = Math.round(slope * x);
    }
    
    motion = {
      'x' : Math.min(x, Math.abs(target.x)) * (target.x < 0 ? -1 : 1),
      'y' : Math.min(y, Math.abs(target.y)) * (target.y < 0 ? -1 : 1)
    }
  }
      
  this.positionTiles(motion, true);
  this.notifyViewerMoved();
      
  if (!PanoJS.USE_SLIDE && !skip_motion) {
    return;
  }
      
  var newcoords = {
    'x' : coords.x + motion.x,
    'y' : coords.y + motion.y
  };
      
  var self = this;
  // TODO: use an exponential growth rather than linear (should also depend on how far we are going)
  // FIXME: this could be optimized by calling positionTiles directly perhaps
  this.slideAcceleration += PanoJS.SLIDE_ACCELERATION_FACTOR;
  this.slideMonitor = setTimeout(function() { self.recenter(newcoords); }, PanoJS.SLIDE_DELAY );
};

PanoJS.prototype.resize = function() {     
  // IE fires a premature resize event
  if (!this.initialized) return;
  if (this.width == this.viewer.offsetWidth && this.height == this.viewer.offsetHeight) return;
      
  var newWidth = this.viewer.offsetWidth;
  var newHeight = this.viewer.offsetHeight;
  this.viewer.style.display = 'none';
  this.clear();
  this.width = newWidth;
  this.height = newHeight;
      
  this.prepareTiles();
  this.positionTiles();
  this.viewer.style.display = '';
  this.initialized = true;
  this.notifyViewerMoved();
  this.notifyViewerResized();
};

PanoJS.prototype.toggleMaximize = function() {     
  if (!this.maximized) this.maximized = false;
  this.maximized = !this.maximized;
  
  var vd = this.viewer;
  if (this.maximized) {
      this.viewer_style = { 'width': vd.style.width, 'height': vd.style.height,
          'position': vd.style.position, 'zIndex': vd.style.zIndex,
          'left': vd.style.left, 'top': vd.style.top };
      this.document_style = { 'padding': document.body.style.padding, 'overflow': document.body.style.overflow };
      
      vd.style.position = 'fixed';
      //vd.style.position = 'absolute';            
      vd.style.zIndex   = '14999';
      //vd.style.left     = window.scrollX + 'px';
      //vd.style.top      = window.scrollY + 'px';
      vd.style.left     = '0px';
      vd.style.top      = '0px';
      vd.style.width    = '100%';
      vd.style.height   = '100%'; 
      document.body.style.overflow = 'hidden';
      document.body.style.padding = '0';
      if (isMobileSafari()) {
        vd.style.left = window.scrollX + 'px';
        vd.style.top  = window.scrollY + 'px';
        vd.style.width    = window.innerWidth + 'px';
        vd.style.height   = window.innerHeight + 'px';        
      }
  } else {
      document.body.style.padding = this.document_style.padding;
      document.body.style.overflow = this.document_style.overflow;          
      vd.style.width    = this.viewer_style.width;
      vd.style.height   = this.viewer_style.height;
      vd.style.position = this.viewer_style.position;
      vd.style.zIndex   = this.viewer_style.zIndex;
      vd.style.left     = this.viewer_style.left;
      vd.style.top      = this.viewer_style.top;
  }
  
  this.resize();
};
  
/**
 * Resolve the coordinates from this mouse event by subtracting the
 * offset of the viewer in the browser window (or frame).  This does
 * take into account the scroll offset of the page.
 */
PanoJS.prototype.resolveCoordinates = function(e) {    
  if (this.maximized)
    return { 'x' : e.clientX, 'y' : e.clientY };

  return {
    'x' : (e.pageX || (e.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft))) - this.left,
    'y' : (e.pageY || (e.clientY + (document.documentElement.scrollTop || document.body.scrollTop))) - this.top
  };
};

PanoJS.prototype.press = function(coords) {     
  this.activate(true);
  this.mark = coords;
  this.mouse_have_moved = false;
};

PanoJS.prototype.release = function(coords) {  
  this.activate(false);
  var motion = {
    'x' : (coords.x - this.mark.x),
    'y' : (coords.y - this.mark.y)
  };
      
  this.x += motion.x;
  this.y += motion.y;
  this.mark = { 'x' : 0, 'y' : 0 };
  this.mouse_have_moved = false;        
};
  
/**
 * Activate the viewer into motion depending on whether the mouse is pressed or
 * not pressed.  This method localizes the changes that must be made to the
 * layers.
 */
PanoJS.prototype.activate = function(pressed) {   
  this.pressed = pressed;
  this.surface.style.cursor = (pressed ? PanoJS.GRABBING_MOUSE_CURSOR : PanoJS.GRAB_MOUSE_CURSOR);
  this.ui_listener.onmousemove = (pressed ? callback(this, this.mouseMovedHandler) : function() {});
};
  
/**
 * Check whether the specified point exceeds the boundaries of
 * the viewer's primary image.
 */
PanoJS.prototype.pointExceedsBoundaries = function(coords) {     
  return (coords.x < this.x ||
          coords.y < this.y ||
          coords.x > (this.xTileSize * Math.pow(2, this.zoomLevel) + this.x) ||
          coords.y > (this.xTileSize * Math.pow(2, this.zoomLevel) + this.y));
};
  
// QUESTION: where is the best place for this method to be invoked?
PanoJS.prototype.resetSlideMotion = function() {     
  // QUESTION: should this be > 0 ? 
  if (this.slideMonitor != 0) {
    clearTimeout(this.slideMonitor);
    this.slideMonitor = 0;
  }
      
  this.slideAcceleration = 0;
};



//-------------------------------------------------------
// Mouse Events
//-------------------------------------------------------

PanoJS.prototype.blockPropagation = function (e) {
    if (e.stopPropagation) e.stopPropagation(); // DOM Level 2
    else e.cancelBubble = true;                 // IE    
    if (e.preventDefault) e.preventDefault(); // prevent image dragging
    else e.returnValue=false;    
}

PanoJS.prototype.mousePressedHandler = function(e) {
  e = e ? e : window.event;
  this.blockPropagation(e);
    
  // only grab on left-click
  var coords = this.resolveCoordinates(e);
  //if (this.pointExceedsBoundaries(coords))
    this.press(coords);
    
  // NOTE: MANDATORY! must return false so event does not propagate to well!
  return false;
};

PanoJS.prototype.mouseReleasedHandler = function(e) {
  e = e ? e : window.event;
  if (!this.pressed) return false;
  var coords = this.resolveCoordinates(e);    
  var motion = {
        'x' : (coords.x - this.mark.x),
        'y' : (coords.y - this.mark.y)
  };        
  var moved = this.mouse_have_moved;
  this.release(coords);
    
  // only if there was little movement
  if (moved || motion.x>5 || motion.y>5) return false;
    
  if (e.button == 2) {
    this.blockPropagation(e);      
    this.zoom(-1);    
  } else
  // move on one click
  if (e.button < 2) {
    //if (!this.pointExceedsBoundaries(coords)) {
         this.resetSlideMotion();
         this.recenter(coords);
    //}        
  }
    
  return false;    
};

PanoJS.prototype.mouseMovedHandler = function(e) {
  e = e ? e : window.event;
    
  // only move on left-click
  if (e.button < 2) {
    this.mouse_have_moved = true;
    this.moveCount++;
    if (this.moveCount % PanoJS.MOVE_THROTTLE == 0)
      this.moveViewer(this.resolveCoordinates(e));
  }
  return false;        
};

PanoJS.prototype.doubleClickHandler = function(e) {
  e = e ? e : window.event;
  //var coords = this.resolveCoordinates(e);
  //if (!this.pointExceedsBoundaries(coords)) {
    //this.resetSlideMotion();
    //this.recenter(coords);        
    this.zoom(1);
  //}
  return false;  
};

PanoJS.prototype.mouseWheelHandler = function(e) {
  e = e ? e : window.event;
  this.blockPropagation(e);     
  
  if (PanoJS.USE_WHEEL_FOR_ZOOM) {
      if (e.wheelDelta<0) this.zoom(-1);
      else                
      if (e.wheelDelta>0) this.zoom(1);  
  } else {
      var dx = e.wheelDeltaX/PanoJS.WHEEL_SCALE;
      var dy = e.wheelDeltaY/PanoJS.WHEEL_SCALE;
      this.moveViewerBy({'x': dx,'y': dy});
  }  
  return false;      
};

PanoJS.prototype.mouseScrollHandler = function(e) {
  e = e ? e : window.event;
  this.blockPropagation(e); 
  
  // Here we only have delta Y, so for firefox only Zoom will be implemented
  //var wheelData = e.detail * -1 * PanoJS.WHEEL_SCALE; // adjust delta value in sync with Webkit    
  if (e.detail<0) this.zoom(1);
  else                
  if (e.detail>0) this.zoom(-1);
  
  return false;  
};

//----------------------------------------------------------------------
// keyboard events
//----------------------------------------------------------------------

PanoJS.prototype.keyboardHandler = function(e) {
  if (!PanoJS.USE_KEYBOARD) return;  
  e = e ? e : window.event;
  var key = e.keyCode ? e.keyCode : e.which;
  
  if (key in PanoJS.KEY_MINUS) {
      this.blockPropagation(e); 
      this.zoom(-1);
      return false;      
  } else 
  if (key in PanoJS.KEY_PLUS) {
      this.blockPropagation(e); 
      this.zoom(1);
      return false;
  } else
  if (key == PanoJS.KEY_UP) {
      this.blockPropagation(e); 
      this.moveViewerBy({'x': 0,'y': -PanoJS.KEY_MOVE_THROTTLE});
      return false;      
  } else 
  if (key == PanoJS.KEY_RIGHT) {
      this.blockPropagation(e); 
      this.moveViewerBy({'x': PanoJS.KEY_MOVE_THROTTLE,'y': 0});      
      return false;      
  } else 
  if (key == PanoJS.KEY_DOWN) {
      this.blockPropagation(e); 
      this.moveViewerBy({'x': 0,'y': PanoJS.KEY_MOVE_THROTTLE});      
      return false;      
  } else 
  if (key == PanoJS.KEY_LEFT) {
      this.blockPropagation(e); 
       this.moveViewerBy({'x': -PanoJS.KEY_MOVE_THROTTLE,'y': 0});      
      return false;
  }  
  
}

//----------------------------------------------------------------------
// touch events
//----------------------------------------------------------------------

PanoJS.prototype.touchStartHandler = function(e) {
  e = e ? e : window.event;
  if (e == null) return false;
    
  if (e.touches.length == 1) { // Only deal with one finger
      // prevent anything else happening for this event further
      this.blockPropagation(e);   
      
      // actully store the initial touch move position
      var touch = e.touches[0]; // Get the information for finger #1
      this.touch_start = {'x': touch.clientX,'y': touch.clientY}; 
  }
  return false;       
}

PanoJS.prototype.touchMoveHandler = function(e) {
  e = e ? e : window.event;
  if (e == null) return false;
  
  if (e.touches.length==1 && this.touch_start) { // Only deal with one finger
      // prevent anything else happening for this event further
      this.blockPropagation(e);          
      
      // move
      var touch = e.touches[0]; // Get the information for finger #1    
      var p = {'x': touch.clientX-this.touch_start.x,'y': touch.clientY-this.touch_start.y};
      this.moveViewerBy(p); 
      this.touch_start = {'x': touch.clientX,'y': touch.clientY}; 
  }
  return false;       
}


//----------------------------------------------------------------------
// gesture events
//----------------------------------------------------------------------

PanoJS.prototype.gestureStartHandler = function(e) {
  e = e ? e : window.event;
  if (e == null) return false;  
  this.blockPropagation(e);
  this.gesture_current_scale = 1.0;
  this.gesture_image_scale = this.currentScale();  
  return false;              
}

PanoJS.prototype.gestureChangeHandler = function(e) {
  e = e ? e : window.event;
  if (e == null) return false;  
  this.blockPropagation(e);      
  
  if (e.scale/this.gesture_current_scale>2.0) {
    this.gesture_current_scale = e.scale;
    this.zoom(1);
  } else 
  if (e.scale/this.gesture_current_scale<0.5) {
    this.gesture_current_scale = e.scale;
    this.zoom(-1);
  }
  
  if (this.osd_control) {
    e.image_scale = this.gesture_image_scale;
    e.gesture_current_scale = this.gesture_current_scale;
    this.osd_control.viewerZooming(e); 
  }
  
  return false;       
}

PanoJS.prototype.gestureEndHandler = function(e) {
  e = e ? e : window.event;
  if (e == null) return false;  
  this.blockPropagation(e);      
  if (this.osd_control) this.osd_control.show(false);
  
  // e.scale e.rotation
  //if (e.scale>1) this.zoom(1);
  //else
  //if (e.scale<1) this.zoom(-1);  
  return false;       
}


//-------------------------------------------------------
// Control Events
//-------------------------------------------------------

PanoJS.prototype.zoomInHandler = function(e) {
  this.zoom(1);
};

PanoJS.prototype.zoomOutHandler = function(e) {
  this.zoom(-1);
};

PanoJS.prototype.zoom11Handler = function(e) {
  this.zoom(this.maxZoomLevel-this.zoomLevel);
};

PanoJS.prototype.maximizeHandler = function(e) {
  this.toggleMaximize();  
};


//-------------------------------------------------------
// PanoJS Events
//-------------------------------------------------------

PanoJS.MoveEvent = function(x, y) {
  this.x = x;
  this.y = y;
};

PanoJS.ZoomEvent = function(x, y, level, scale, width, height) {
  this.x = x;
  this.y = y;
  this.level = level;
  this.scale = scale;
  this.width = width;
  this.height = height;   
};

PanoJS.ResizeEvent = function(x, y, width, height) {
  this.x = x;
  this.y = y;
  this.width = width;
  this.height = height;
};




//-------------------------------------------------------
// Tile
//-------------------------------------------------------
    
function Tile(viewer, x, y) {
    this.viewer = viewer;  
    this.element = null;
    this.posx = 0;
    this.posy = 0;
    this.xIndex = x;
    this.yIndex = y;
    this.qx = x;
    this.qy = y;
};

Tile.prototype.createDOMElements = function() {
    //this.dom_info.innerHTML = "";
};

//-------------------------------------------------------
// TileUrlProvider
//-------------------------------------------------------

PanoJS.TileUrlProvider = function(baseUri, prefix, extension) {
  this.baseUri = baseUri;
  this.prefix = prefix;
  this.extension = extension;
}

PanoJS.TileUrlProvider.prototype = {
assembleUrl: function(xIndex, yIndex, zoom) {
    return this.baseUri + '/' +
    this.prefix + zoom + '-' + xIndex + '-' + yIndex + '.' + this.extension +
    (PanoJS.REVISION_FLAG ? '?r=' + PanoJS.REVISION_FLAG : '');
}
}

